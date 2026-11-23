package com.example.data

import android.content.Context
import android.util.AtomicFile
import androidx.compose.ui.geometry.Offset
import com.example.ui.components.KanjiGuidance
import com.example.ui.components.StrokeDataProvider
import com.example.ui.components.StrokeSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class KanjiRemoteSnapshot(
  val lessons: List<KanjiLesson> = emptyList(),
  val vocabulary: List<Vocabulary> = emptyList(),
  val stories: List<KanjiStory>? = null,
  val readingExercises: List<KanjiReadingExercise>? = null,
  val strokesByCharacter: Map<String, List<StrokeSegment>> = emptyMap(),
  val guidanceByCharacter: Map<String, KanjiGuidance> = emptyMap(),
  val syncedLessonCount: Int = 0
)

data class KanjiSyncResult(
  val success: Boolean,
  val snapshot: KanjiRemoteSnapshot,
  val message: String
)

/**
 * Fetches Kanji lessons, vocabulary, reading stories, and reading exercises
 * from Firebase Hosting (https://nihongo-drill-1479e.web.app/kanji/) and caches
 * validated JSON files atomically for 100% offline use.
 */
object KanjiRemoteSync {

  private const val BASE_URL = "https://nihongo-drill-1479e.web.app/"
  private const val MANIFEST_PATH = "kanji/manifest.json"
  private const val MAX_BYTES = 2 * 1024 * 1024

  private fun cacheDir(context: Context): File =
    File(context.filesDir, "kanji_remote_cache").apply { mkdirs() }

  private fun manifestCacheFile(context: Context): AtomicFile =
    AtomicFile(File(cacheDir(context), "manifest.json"))

  private fun lessonCacheFile(context: Context, lessonId: String): AtomicFile {
    val safeId = lessonId.replace(Regex("[^A-Za-z0-9_-]"), "_")
    return AtomicFile(File(cacheDir(context), "lesson_$safeId.json"))
  }

  private fun storiesCacheFile(context: Context): AtomicFile =
    AtomicFile(File(cacheDir(context), "stories.json"))

  private fun exercisesCacheFile(context: Context): AtomicFile =
    AtomicFile(File(cacheDir(context), "reading_exercises.json"))

  fun loadCached(context: Context): KanjiRemoteSnapshot {
    val manifestJson = runCatching {
      val text = manifestCacheFile(context).readFully().toString(Charsets.UTF_8).trim()
      if (!text.startsWith("{")) return KanjiRemoteSnapshot()
      JSONObject(text)
    }.getOrNull() ?: return KanjiRemoteSnapshot()

    val lessons = mutableListOf<KanjiLesson>()
    val vocab = mutableListOf<Vocabulary>()
    val strokesMap = mutableMapOf<String, List<StrokeSegment>>()
    val guidanceMap = mutableMapOf<String, KanjiGuidance>()

    val lessonsArr = manifestJson.optJSONArray("lessons")
    if (lessonsArr != null) {
      for (i in 0 until lessonsArr.length()) {
        val meta = lessonsArr.optJSONObject(i) ?: continue
        val id = meta.optString("id", "").ifBlank {
          val lvl = meta.optString("level", "N5")
          val num = meta.optInt("lessonNumber", 0)
          "${lvl}_L$num"
        }
        runCatching {
          val lessonBytes = lessonCacheFile(context, id).readFully()
          val parsed = parseLessonJson(lessonBytes, meta, strokesMap, guidanceMap)
          lessons.add(parsed.first)
          vocab.addAll(parsed.second)
        }
      }
    }

    val stories = runCatching {
      val bytes = storiesCacheFile(context).readFully()
      parseStoriesJson(bytes)
    }.getOrNull()

    val exercises = runCatching {
      val bytes = exercisesCacheFile(context).readFully()
      parseExercisesJson(bytes)
    }.getOrNull()

    return KanjiRemoteSnapshot(
      lessons = lessons,
      vocabulary = vocab,
      stories = stories,
      readingExercises = exercises,
      strokesByCharacter = strokesMap,
      guidanceByCharacter = guidanceMap,
      syncedLessonCount = lessons.size
    )
  }

  suspend fun syncFromServer(context: Context): KanjiSyncResult = withContext(Dispatchers.IO) {
    try {
      val manifestBytes = fetchJsonBytes(MANIFEST_PATH)
      val manifestJson = JSONObject(manifestBytes.toString(Charsets.UTF_8))
      val lessonsArr = manifestJson.optJSONArray("lessons")
        ?: throw IllegalArgumentException("Invalid Kanji manifest")

      val cachedManifest = runCatching {
        JSONObject(manifestCacheFile(context).readFully().toString(Charsets.UTF_8))
      }.getOrNull()
      val cachedVersions = mutableMapOf<String, Int>()
      cachedManifest?.optJSONArray("lessons")?.let { arr ->
        for (i in 0 until arr.length()) {
          val item = arr.optJSONObject(i) ?: continue
          val id = item.optString("id", "")
          if (id.isNotBlank()) cachedVersions[id] = item.optInt("version", 0)
        }
      }

      val lessons = mutableListOf<KanjiLesson>()
      val vocab = mutableListOf<Vocabulary>()
      val strokesMap = mutableMapOf<String, List<StrokeSegment>>()
      val guidanceMap = mutableMapOf<String, KanjiGuidance>()

      for (i in 0 until lessonsArr.length()) {
        val meta = lessonsArr.optJSONObject(i) ?: continue
        val level = meta.optString("level", "N5").ifBlank { "N5" }
        val lessonNum = meta.optInt("lessonNumber", 0)
        if (lessonNum <= 0) continue
        val id = meta.optString("id", "").ifBlank { "${level}_L$lessonNum" }
        val version = meta.optInt("version", 1)
        val path = meta.optString("path", "").trimStart('/')
        if (path.isBlank()) continue

        val cacheFile = lessonCacheFile(context, id)
        val cachedBytes = if (cachedVersions[id] == version && cacheFile.baseFile.exists()) {
          runCatching { cacheFile.readFully() }.getOrNull()
        } else {
          null
        }

        val lessonBytes = cachedBytes ?: run {
          val fresh = fetchJsonBytes(path)
          // Validate before saving atomically
          parseLessonJson(fresh, meta, mutableMapOf(), mutableMapOf())
          writeAtomically(cacheFile, fresh)
          fresh
        }

        val parsed = parseLessonJson(lessonBytes, meta, strokesMap, guidanceMap)
        lessons.add(parsed.first)
        vocab.addAll(parsed.second)
      }

      val storiesPath = manifestJson.optString("storiesPath", "").trimStart('/')
      val stories: List<KanjiStory>? = if (storiesPath.isNotBlank()) {
        runCatching {
          val bytes = fetchJsonBytes(storiesPath)
          val parsedStories = parseStoriesJson(bytes)
          writeAtomically(storiesCacheFile(context), bytes)
          parsedStories
        }.getOrNull()
      } else null

      val exercisesPath = manifestJson.optString("readingExercisesPath", "").trimStart('/')
      val exercises: List<KanjiReadingExercise>? = if (exercisesPath.isNotBlank()) {
        runCatching {
          val bytes = fetchJsonBytes(exercisesPath)
          val parsedExercises = parseExercisesJson(bytes)
          writeAtomically(exercisesCacheFile(context), bytes)
          parsedExercises
        }.getOrNull()
      } else null

      writeAtomically(manifestCacheFile(context), manifestBytes)

      val snapshot = KanjiRemoteSnapshot(
        lessons = lessons,
        vocabulary = vocab,
        stories = stories,
        readingExercises = exercises,
        strokesByCharacter = strokesMap,
        guidanceByCharacter = guidanceMap,
        syncedLessonCount = lessons.size
      )

      val sentenceCount = exercises?.size ?: 0
      val storyCount = stories?.size ?: 0
      val summaryParts = buildList {
        add("${lessons.size} lessons")
        if (storyCount > 0) add(if (storyCount == 1) "1 story" else "$storyCount stories")
        if (sentenceCount > 0) add("$sentenceCount sentences")
      }

      KanjiSyncResult(
        success = true,
        snapshot = snapshot,
        message = "Synced ${summaryParts.joinToString(", ")} from server"
      )
    } catch (_: Exception) {
      val cached = loadCached(context)
      KanjiSyncResult(
        success = false,
        snapshot = cached,
        message = "Offline or server not updated yet — using saved lessons"
      )
    }
  }

  private fun fetchJsonBytes(relativePath: String): ByteArray {
    val cleanPath = relativePath.trimStart('/')
    val connection = URL("$BASE_URL$cleanPath").openConnection() as HttpURLConnection
    try {
      connection.connectTimeout = 10_000
      connection.readTimeout = 15_000
      connection.instanceFollowRedirects = false
      connection.useCaches = false
      connection.setRequestProperty("Cache-Control", "no-cache")
      connection.setRequestProperty("Pragma", "no-cache")
      require(connection.responseCode == HttpURLConnection.HTTP_OK) {
        "HTTP ${connection.responseCode}"
      }
      val bytes = connection.inputStream.use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var count: Int
        while (input.read(buffer).also { count = it } != -1) {
          require(output.size() + count <= MAX_BYTES) { "Response too large" }
          output.write(buffer, 0, count)
        }
        output.toByteArray()
      }
      // Reject Firebase Hosting SPA HTML fallback cleanly
      val trimmedStart = bytes.toString(Charsets.UTF_8).trimStart()
      require(trimmedStart.startsWith("{") || trimmedStart.startsWith("[")) {
        "Non-JSON response (HTML fallback)"
      }
      return bytes
    } finally {
      connection.disconnect()
    }
  }

  private fun parseLessonJson(
    bytes: ByteArray,
    meta: JSONObject,
    strokesOut: MutableMap<String, List<StrokeSegment>>,
    guidanceOut: MutableMap<String, KanjiGuidance>
  ): Pair<KanjiLesson, List<Vocabulary>> {
    val root = JSONObject(bytes.toString(Charsets.UTF_8))
    val level = root.optString("level", meta.optString("level", "N5")).ifBlank { "N5" }
    val lessonNumber = root.optInt("lesson", meta.optInt("lessonNumber", 1))
    val id = meta.optString("id", "").ifBlank { "${level}_L$lessonNumber" }
    val title = meta.optString("title", "").ifBlank { "Lesson $lessonNumber" }
    val subtitle = meta.optString("subtitle", root.optString("subtitle", "")).ifBlank {
      root.optString("title", "Basic Kanji").substringAfter("—", "Kanji Lesson").trim()
    }

    val kanjiArr = root.getJSONArray("kanji")
    val kanjiList = mutableListOf<Kanji>()
    val vocabList = mutableListOf<Vocabulary>()

    for (i in 0 until kanjiArr.length()) {
      val item = kanjiArr.getJSONObject(i)
      val number = item.getInt("number")
      val character = item.optString("kanji", item.optString("character", "")).trim()
      if (character.isBlank()) continue
      val rawMeaning = item.optString("meaning", "").trim()
      val formattedMeaning = rawMeaning
        .split(";", ",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" · ") { part ->
          part.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
        }
        .ifBlank { rawMeaning }

      val kunList = item.optJSONArray("kun_readings")?.toStringList()
        ?: item.optJSONArray("kunReadings")?.toStringList()
        ?: emptyList()
      val onList = item.optJSONArray("on_readings")?.toStringList()
        ?: item.optJSONArray("onReadings")?.toStringList()
        ?: emptyList()
      val onDisplay = item.optJSONArray("on_readings_display")?.toStringList()
        ?: item.optJSONArray("onReadingsDisplay")?.toStringList()
        ?: onList.map { katakanaToHiragana(it).replace("（", "").replace("）", "") }

      val strokeCount = item.optInt("stroke_count", item.optInt("strokeCount", 1))
      val strokeDescriptions = item.optJSONArray("stroke_descriptions")?.toStringList()
        ?: item.optJSONArray("strokeDescriptions")?.toStringList()
        ?: emptyList()

      kanjiList.add(
        Kanji(
          number = number,
          character = character,
          meaning = formattedMeaning,
          level = level,
          kunReadings = kunList,
          onReadings = onList,
          onReadingsDisplay = onDisplay,
          strokeCount = strokeCount,
          strokeDescriptions = strokeDescriptions
        )
      )

      val examplesArr = item.optJSONArray("examples")
      if (examplesArr != null) {
        for (j in 0 until examplesArr.length()) {
          val ex = examplesArr.optJSONObject(j) ?: continue
          val word = ex.optString("word", "").trim()
          val reading = ex.optString("reading", "").trim()
          val exMeaning = ex.optString("meaning", "").trim()
          if (word.isNotBlank()) {
            vocabList.add(Vocabulary(number, word, reading, exMeaning))
          }
        }
      }

      // Optional animated stroke segments for Writing Practice (normalized 0..100 coordinates)
      val strokesArr = item.optJSONArray("strokes")
      if (strokesArr != null && strokesArr.length() > 0) {
        val segments = mutableListOf<StrokeSegment>()
        for (sIdx in 0 until strokesArr.length()) {
          val sObj = strokesArr.optJSONObject(sIdx) ?: continue
          val ptsArr = sObj.optJSONArray("points") ?: continue
          val offsets = mutableListOf<Offset>()
          for (pIdx in 0 until ptsArr.length()) {
            val pt = ptsArr.optJSONArray(pIdx) ?: continue
            if (pt.length() >= 2) {
              offsets.add(Offset(pt.optDouble(0, 0.0).toFloat(), pt.optDouble(1, 0.0).toFloat()))
            }
          }
          if (offsets.size >= 2) {
            segments.add(
              StrokeSegment(
                points = offsets,
                description = sObj.optString("description", ""),
                directionArrow = sObj.optString("arrow", sObj.optString("directionArrow", "↓")),
                stepNumber = sObj.optInt("step", sObj.optInt("stepNumber", sIdx + 1))
              )
            )
          }
        }
        if (segments.isNotEmpty()) {
          strokesOut[character] = segments
        }
      }

      // Optional writing guidance rule & tips
      val guidanceObj = item.optJSONObject("guidance")
      if (guidanceObj != null) {
        val rule = guidanceObj.optString("rule", "").trim()
        val tips = guidanceObj.optJSONArray("tips")?.toStringList().orEmpty()
        if (rule.isNotBlank() || tips.isNotEmpty()) {
          guidanceOut[character] = KanjiGuidance(
            rule = rule.ifBlank { "Top to bottom, left to right" },
            tips = tips
          )
        }
      }
    }

    require(kanjiList.isNotEmpty()) { "Lesson $id has no Kanji" }
    val lesson = KanjiLesson(
      id = id,
      lessonNumber = lessonNumber,
      title = title,
      subtitle = subtitle,
      level = level,
      kanjiList = kanjiList
    )
    return lesson to vocabList
  }

  private fun parseStoriesJson(bytes: ByteArray): List<KanjiStory> {
    val root = JSONObject(bytes.toString(Charsets.UTF_8))
    val arr = root.optJSONArray("stories") ?: return emptyList()
    val result = mutableListOf<KanjiStory>()
    for (i in 0 until arr.length()) {
      val s = arr.optJSONObject(i) ?: continue
      val id = s.getString("id")
      val level = s.optString("level", "N5")
      val lessonNumber = s.getInt("lessonNumber")
      val title = s.getString("title")
      val fullPlain = s.optJSONArray("fullPlainParagraphs")?.toStringList() ?: emptyList()

      val pagesArr = s.optJSONArray("pages")
      val pages = mutableListOf<KanjiStoryPage>()
      if (pagesArr != null) {
        for (p in 0 until pagesArr.length()) {
          val pObj = pagesArr.optJSONObject(p) ?: continue
          val pageNum = pObj.optInt("pageNumber", p + 1)
          val plain = pObj.optJSONArray("plainParagraphs")?.toStringList() ?: emptyList()
          val english = pObj.optJSONArray("englishLines")?.toStringList() ?: emptyList()
          pages.add(
            KanjiStoryPage(
              pageNumber = pageNum,
              plainParagraphs = plain,
              furiganaLines = emptyList(),
              englishLines = english
            )
          )
        }
      }

      val vocabArr = s.optJSONArray("vocabulary")
      val vocab = mutableListOf<KanjiStoryVocab>()
      if (vocabArr != null) {
        for (v in 0 until vocabArr.length()) {
          val vObj = vocabArr.optJSONObject(v) ?: continue
          vocab.add(
            KanjiStoryVocab(
              wordWithReading = vObj.getString("wordWithReading"),
              meaning = vObj.getString("meaning"),
              audioText = vObj.optString("audioText", vObj.getString("wordWithReading"))
            )
          )
        }
      }

      result.add(
        KanjiStory(
          id = id,
          level = level,
          lessonNumber = lessonNumber,
          title = title,
          fullPlainParagraphs = fullPlain,
          pages = pages,
          vocabulary = vocab
        )
      )
    }
    return result
  }

  private fun parseExercisesJson(bytes: ByteArray): List<KanjiReadingExercise> {
    val root = JSONObject(bytes.toString(Charsets.UTF_8))
    val arr = root.optJSONArray("exercises") ?: return emptyList()
    val result = mutableListOf<KanjiReadingExercise>()
    for (i in 0 until arr.length()) {
      val e = arr.optJSONObject(i) ?: continue
      result.add(
        KanjiReadingExercise(
          id = e.optInt("id", i + 1),
          level = e.optString("level", "N5"),
          lessonNumber = e.optInt("lessonNumber", 1),
          sentenceWithKanji = e.getString("sentenceWithKanji"),
          readingHiragana = e.getString("readingHiragana"),
          translation = e.getString("translation")
        )
      )
    }
    return result
  }

  private fun JSONArray.toStringList(): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until length()) {
      val value = optString(i, "").trim()
      if (value.isNotEmpty()) list.add(value)
    }
    return list
  }

  private fun katakanaToHiragana(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
      if (ch in '\u30A1'..'\u30F6') {
        sb.append((ch.code - 0x60).toChar())
      } else {
        sb.append(ch)
      }
    }
    return sb.toString()
  }

  private fun writeAtomically(file: AtomicFile, bytes: ByteArray) {
    val stream = file.startWrite()
    try {
      stream.write(bytes)
      file.finishWrite(stream)
    } catch (error: Throwable) {
      file.failWrite(stream)
      throw error
    }
  }
}
