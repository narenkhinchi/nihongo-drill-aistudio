package com.nihongo.vocabdrill.update

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val sha256: String,
    val sizeBytes: Long,
    val releaseNotes: String
)

sealed interface AppUpdateState {
    data object Checking : AppUpdateState
    data object UpToDate : AppUpdateState
    data class Available(val info: AppUpdateInfo) : AppUpdateState
    data class Downloading(val info: AppUpdateInfo, val progress: Float) : AppUpdateState
    data class Downloaded(val info: AppUpdateInfo, val apkFile: File) : AppUpdateState
    data class Failed(val info: AppUpdateInfo?, val message: String) : AppUpdateState
}

class AppUpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppUpdateRepository(application)
    private val _state = MutableStateFlow<AppUpdateState>(AppUpdateState.Checking)
    val state: StateFlow<AppUpdateState> = _state

    init {
        checkForUpdate(silent = true)
    }

    fun checkForUpdate(silent: Boolean = false) {
        if (_state.value is AppUpdateState.Downloading) return
        _state.value = AppUpdateState.Checking
        viewModelScope.launch {
            try {
                val update = withContext(Dispatchers.IO) { repository.findUpdate() }
                _state.value = update?.let(AppUpdateState::Available) ?: AppUpdateState.UpToDate
            } catch (error: Exception) {
                _state.value = if (silent) {
                    AppUpdateState.Failed(null, "")
                } else {
                    AppUpdateState.Failed(null, error.message ?: "Could not check for updates.")
                }
            }
        }
    }

    fun downloadUpdate() {
        val current = _state.value
        val info = when (current) {
            is AppUpdateState.Available -> current.info
            is AppUpdateState.Failed -> current.info
            else -> null
        } ?: return

        _state.value = AppUpdateState.Downloading(info, 0f)
        viewModelScope.launch {
            try {
                val apk = withContext(Dispatchers.IO) {
                    repository.download(info) { downloaded, total ->
                        val progress = if (total > 0L) {
                            (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        _state.value = AppUpdateState.Downloading(info, progress)
                    }
                }
                _state.value = AppUpdateState.Downloaded(info, apk)
            } catch (error: Exception) {
                _state.value = AppUpdateState.Failed(
                    info,
                    error.message ?: "The update could not be downloaded. Please try again."
                )
            }
        }
    }
}

private class AppUpdateRepository(private val application: Application) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .callTimeout(6, TimeUnit.MINUTES)
        .build()

    fun findUpdate(): AppUpdateInfo? {
        val request = Request.Builder()
            .url(MANIFEST_URL)
            .cacheControl(CacheControl.FORCE_NETWORK)
            .header("Cache-Control", "no-cache")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Update check failed (${response.code}). Please try again.")
            }
            val json = JSONObject(response.body?.string() ?: throw IOException("Empty update information."))
            val targetApplicationId = json.optString("applicationId")
            if (targetApplicationId != application.packageName) {
                throw IOException("This update is not for this app.")
            }

            val versionCode = json.optLong("versionCode", -1L)
            val versionName = json.optString("versionName", "").ifBlank { "New version" }
            val apkUrl = json.optString("apkUrl", "")
            val sha256 = json.optString("sha256", "").lowercase()
            val sizeBytes = json.optLong("sizeBytes", -1L)
            val releaseNotes = json.optString("releaseNotes", "App improvements and updates.")

            if (versionCode <= BuildConfig.VERSION_CODE.toLong()) return null
            if (versionCode < 1L || sizeBytes !in 1L..MAX_APK_BYTES) {
                throw IOException("The update information is incomplete.")
            }
            val uri = Uri.parse(apkUrl)
            if (uri.scheme != "https" || uri.host != ALLOWED_HOST || !uri.path.orEmpty().startsWith("/narenkhinchi/nihongo-drill-releases/releases/download/")) {
                throw IOException("The update download address is not trusted.")
            }
            if (!sha256.matches(Regex("[a-f0-9]{64}"))) {
                throw IOException("The update checksum is invalid.")
            }

            return AppUpdateInfo(
                versionCode = versionCode,
                versionName = versionName,
                apkUrl = apkUrl,
                sha256 = sha256,
                sizeBytes = sizeBytes,
                releaseNotes = releaseNotes
            )
        }
    }

    fun download(info: AppUpdateInfo, onProgress: (Long, Long) -> Unit): File {
        val request = Request.Builder()
            .url(info.apkUrl)
            .cacheControl(CacheControl.FORCE_NETWORK)
            .header("Cache-Control", "no-cache")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Download failed (${response.code}). Please try again.")
            }
            val body = response.body ?: throw IOException("The update file was empty.")
            val directory = File(application.filesDir, "updates")
            if (!directory.exists() && !directory.mkdirs()) {
                throw IOException("Could not prepare storage for the update.")
            }

            val partialFile = File(directory, "nihongo-drill-update.apk.part")
            val finalFile = File(directory, "nihongo-drill-update.apk")
            if (partialFile.exists()) partialFile.delete()

            val digest = MessageDigest.getInstance("SHA-256")
            var downloaded = 0L
            var lastReported = 0L
            try {
                body.byteStream().use { input ->
                    partialFile.outputStream().buffered().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            downloaded += count
                            if (downloaded > info.sizeBytes || downloaded > MAX_APK_BYTES) {
                                throw IOException("The downloaded file is larger than expected.")
                            }
                            digest.update(buffer, 0, count)
                            output.write(buffer, 0, count)
                            if (downloaded - lastReported >= PROGRESS_UPDATE_BYTES) {
                                lastReported = downloaded
                                onProgress(downloaded, info.sizeBytes)
                            }
                        }
                        output.flush()
                    }
                }

                if (downloaded != info.sizeBytes) {
                    throw IOException("The download was incomplete. Please try again.")
                }
                val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
                if (!actualHash.equals(info.sha256, ignoreCase = true)) {
                    throw IOException("The update failed its security check. Please download it again.")
                }

                if (finalFile.exists() && !finalFile.delete()) {
                    throw IOException("Could not replace the previous update file.")
                }
                if (!partialFile.renameTo(finalFile)) {
                    throw IOException("Could not finish saving the update.")
                }
                onProgress(downloaded, info.sizeBytes)
                return finalFile
            } catch (error: Exception) {
                partialFile.delete()
                throw error
            }
        }
    }

    companion object {
        private const val MANIFEST_URL = "https://github.com/narenkhinchi/nihongo-drill-releases/releases/latest/download/latest.json"
        private const val ALLOWED_HOST = "github.com"
        private const val MAX_APK_BYTES = 100L * 1024L * 1024L
        private const val PROGRESS_UPDATE_BYTES = 256L * 1024L
    }
}
