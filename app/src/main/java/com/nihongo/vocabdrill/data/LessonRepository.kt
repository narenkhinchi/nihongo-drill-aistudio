package com.nihongo.vocabdrill.data

import android.content.Context
import android.util.AtomicFile
import com.nihongo.vocabdrill.model.DrillDirection
import com.nihongo.vocabdrill.model.LessonInfo
import com.nihongo.vocabdrill.model.VocabItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class RemoteLesson(
    val number: Int, val title: String, val version: Int, val wordCount: Int,
    val path: String, val sha256: String, val sizeBytes: Int
) {
    fun json() = JSONObject().put("lessonNumber", number).put("title", title)
        .put("version", version).put("wordCount", wordCount).put("path", path)
        .put("sha256", sha256).put("sizeBytes", sizeBytes)
}

data class InstalledLesson(val metadata: RemoteLesson, val words: List<VocabItem>)

data class LessonLibrary(
    val catalog: List<RemoteLesson>,
    val installed: Map<Int, InstalledLesson>,
    val refreshing: Boolean = false,
    val downloading: Set<Int> = emptySet(),
    val errors: Map<Int, String> = emptyMap(),
    val refreshError: String? = null
) {
    val lessons: List<LessonInfo> get() = catalog.map { remote ->
        val local = installed[remote.number]
        LessonInfo(remote.number, local?.metadata?.title ?: remote.title, "",
            "${local?.words?.size ?: remote.wordCount} words from your book",
            local?.words?.size ?: remote.wordCount)
    }
    fun isDownloaded(number: Int) = installed.containsKey(number)
    fun hasUpdate(number: Int): Boolean = installed[number]?.let { local ->
        catalog.any { it.number == number && it.version > local.metadata.version }
    } ?: false
}

/** Hosting only serves public book vocabulary. Scores and stars never leave this device. */
class LessonRepository(
    context: Context,
    private val fetch: (String) -> ByteArray = ::fetchHostedFile
) {
    private val directory = File(context.filesDir, "downloaded_lessons").apply { mkdirs() }
    private val manifestFile = AtomicFile(File(directory, "manifest.json"))
    private val refreshMutex = Mutex()
    private var lastRefreshMillis = 0L
    private val _library = MutableStateFlow(load(context))
    val library = _library.asStateFlow()

    private fun load(context: Context): LessonLibrary {
        val bundled = LessonJson.manifest(context.assets.open("lesson-manifest.json").use { it.readBytes() })
        val cached = runCatching { LessonJson.manifest(manifestFile.readFully()) }.getOrDefault(emptyList())
        val installed = bundled.filter { it.number in 1..3 }.associate { remote ->
            remote.number to InstalledLesson(remote, LessonsDataProvider.getVocabForLesson(remote.number))
        }.toMutableMap()
        directory.listFiles()?.map { it.name.removeSuffix(".bak").removeSuffix(".new") }
            ?.distinct()?.filter { it.matches(Regex("lesson-[0-9]+\\.json")) }?.forEach { name ->
            runCatching {
                val file = File(directory, name)
                val envelope = JSONObject(AtomicFile(file).readFully().toString(Charsets.UTF_8))
                val metadata = LessonJson.metadata(envelope.getJSONObject("metadata"))
                require(file.name == "lesson-${metadata.number}.json")
                val words = LessonJson.words(envelope.getString("payload").toByteArray(Charsets.UTF_8), metadata)
                if (metadata.version >= (installed[metadata.number]?.metadata?.version ?: 0)) {
                    installed[metadata.number] = InstalledLesson(metadata, words)
                }
            } // A broken local download never prevents the built-in lessons from opening.
        }
        return LessonLibrary(mergeCatalog(bundled + cached + installed.values.map { it.metadata }), installed)
    }

    suspend fun refresh(force: Boolean = false) {
        if (!refreshMutex.tryLock()) return
        try {
            val now = android.os.SystemClock.elapsedRealtime()
            if (!force && lastRefreshMillis != 0L && now - lastRefreshMillis < 60_000) return
            _library.update { it.copy(refreshing = true, refreshError = null) }
            val catalog = withContext(Dispatchers.IO) {
                val bytes = fetch("manifest.json")
                val parsed = LessonJson.manifest(bytes)
                writeAtomically(manifestFile, bytes)
                parsed
            }
            _library.update { it.copy(catalog = mergeCatalog(it.catalog + catalog), refreshing = false) }
            lastRefreshMillis = now
        } catch (cancelled: CancellationException) {
            _library.update { it.copy(refreshing = false) }
            throw cancelled
        } catch (_: Exception) {
            _library.update { it.copy(refreshing = false,
                refreshError = "Couldn’t check for new lessons. Saved lessons are ready offline.") }
        } finally {
            refreshMutex.unlock()
        }
    }

    suspend fun download(number: Int) {
        val metadata = _library.value.catalog.find { it.number == number } ?: return
        // Called from the main dispatcher: reserve the button before suspending for IO.
        if (number in _library.value.downloading) return
        if (_library.value.isDownloaded(number) && !_library.value.hasUpdate(number)) return
        _library.update { it.copy(downloading = it.downloading + number, errors = it.errors - number) }
        try {
            val installed = withContext(Dispatchers.IO) {
                val bytes = fetch(metadata.path)
                val words = LessonJson.words(bytes, metadata)
                val envelope = JSONObject().put("metadata", metadata.json())
                    .put("payload", bytes.toString(Charsets.UTF_8)).toString().toByteArray(Charsets.UTF_8)
                writeAtomically(AtomicFile(File(directory, "lesson-$number.json")), envelope)
                InstalledLesson(metadata, words)
            }
            _library.update { it.copy(installed = it.installed + (number to installed), downloading = it.downloading - number) }
        } catch (cancelled: CancellationException) {
            _library.update { it.copy(downloading = it.downloading - number) }
            throw cancelled
        } catch (_: Exception) {
            _library.update { it.copy(downloading = it.downloading - number,
                errors = it.errors + (number to "Couldn’t download. Check your connection and try again.")) }
        }
    }

    fun getAllLessons() = _library.value.lessons
    fun getLessonInfo(number: Int) = getAllLessons().find { it.lessonNumber == number }
    fun getVocabForLesson(number: Int) = _library.value.installed[number]?.words.orEmpty()
    fun getAllVocab() = _library.value.installed.toSortedMap().values.flatMap { it.words }
    fun getDistractors(correct: VocabItem, count: Int, direction: DrillDirection): List<String> {
        fun answer(item: VocabItem) = if (direction == DrillDirection.ENGLISH_TO_HIRAGANA ||
            direction == DrillDirection.KANJI_TO_KANA) item.kana else item.english
        return getAllVocab().filter { it.id != correct.id }
            .sortedBy { if (it.lessonNumber == correct.lessonNumber) 0 else 1 }
            .map(::answer).filter { it != answer(correct) }.distinct().take(20).shuffled().take(count)
    }

    private fun mergeCatalog(items: List<RemoteLesson>) = items.groupBy { it.number }.values
        .map { versions -> versions.maxBy { it.version } }.sortedBy { it.number }

    private fun writeAtomically(file: AtomicFile, bytes: ByteArray) {
        val stream = file.startWrite()
        try { stream.write(bytes); file.finishWrite(stream) }
        catch (error: Throwable) { file.failWrite(stream); throw error }
    }
}

/** Validate the complete response before replacing any usable offline data. */
object LessonJson {
    private const val MAX_BYTES = 2 * 1024 * 1024
    fun metadata(json: JSONObject): RemoteLesson {
        val item = RemoteLesson(json.getInt("lessonNumber"), json.getString("title"),
            json.getInt("version"), json.getInt("wordCount"), json.getString("path"),
            json.getString("sha256"), json.getInt("sizeBytes"))
        require(item.number in 1..10000 && item.version > 0 && item.title.isNotBlank())
        require(item.wordCount in 1..5000 && item.sizeBytes in 1..MAX_BYTES)
        require(item.path.matches(Regex("lessons/lesson-[0-9]+\\.json")))
        require(item.sha256.matches(Regex("[a-f0-9]{64}")))
        return item
    }
    fun manifest(bytes: ByteArray): List<RemoteLesson> {
        require(bytes.size in 1..MAX_BYTES)
        val json = JSONObject(bytes.toString(Charsets.UTF_8))
        require(json.getInt("schemaVersion") == 1 && json.getInt("contentVersion") > 0)
        val array = json.getJSONArray("lessons")
        require(array.length() in 1..10000)
        val items = (0 until array.length()).map { metadata(array.getJSONObject(it)) }
        require(items.distinctBy { it.number }.size == items.size)
        return items
    }
    fun words(bytes: ByteArray, expected: RemoteLesson): List<VocabItem> {
        require(bytes.size == expected.sizeBytes)
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        require(hash == expected.sha256)
        val json = JSONObject(bytes.toString(Charsets.UTF_8))
        require(json.getInt("schemaVersion") == 1 && json.getInt("lessonNumber") == expected.number)
        require(json.getInt("version") == expected.version && json.getString("title") == expected.title)
        val array = json.getJSONArray("words")
        require(array.length() == expected.wordCount)
        val words = (0 until array.length()).map { index ->
            val word = array.getJSONObject(index)
            val id = word.getString("id")
            require(id.matches(Regex("${expected.number}_[A-Za-z0-9_-]+")))
            val japanese = word.getString("japanese")
            val romaji = word.getString("romaji")
            val meaning = word.getString("meaning")
            require(listOf(japanese, romaji, meaning).all { it.isNotBlank() && it.length <= 10000 })
            VocabItem(id, expected.number, "", japanese, romaji, meaning, "")
        }
        require(words.distinctBy { it.id }.size == words.size)
        return words
    }
}

private fun fetchHostedFile(path: String): ByteArray {
    val connection = URL("https://nihongo-drill-1479e.web.app/$path").openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 15_000
        connection.readTimeout = 20_000
        connection.instanceFollowRedirects = false
        connection.useCaches = false
        require(connection.responseCode == HttpURLConnection.HTTP_OK)
        return connection.inputStream.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var count: Int
            while (input.read(buffer).also { count = it } != -1) {
                require(output.size() + count <= 2 * 1024 * 1024)
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    } finally { connection.disconnect() }
}
