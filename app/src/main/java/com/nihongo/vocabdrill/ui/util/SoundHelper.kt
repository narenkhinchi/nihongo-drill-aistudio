package com.nihongo.vocabdrill.ui.util

import android.content.Context
import android.media.SoundPool
import android.media.AudioAttributes
import com.example.R
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Handles synthesized audio chimes, tactile haptic feedback, and native Japanese TTS.
 */
class SoundHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var toneGenerator: ToneGenerator? = null
    private val soundPool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private var chimeReady = false
    private var chimeId = 0
    private var vibrator: Vibrator? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        soundPool.setOnLoadCompleteListener { _, _, status -> chimeReady = status == 0 }
        chimeId = soundPool.load(context, R.raw.correct_soft, 1)
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator init failed", e)
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrator init failed", e)
        }

        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.w(TAG, "TextToSpeech init failed", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.JAPANESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Try broader Japanese locale fallback
                val altResult = textToSpeech?.setLanguage(Locale.JAPAN)
                isTtsReady = (altResult != TextToSpeech.LANG_MISSING_DATA && altResult != TextToSpeech.LANG_NOT_SUPPORTED)
            } else {
                isTtsReady = true
            }
            textToSpeech?.setSpeechRate(0.9f) // Slightly slower for language learners
        } else {
            isTtsReady = false
        }
    }

    fun setSpeechRate(rate: Float) {
        try {
            textToSpeech?.setSpeechRate(rate)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set speech rate", e)
        }
    }

    fun playCorrectChime(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            if (chimeReady) soundPool.play(chimeId, 0.32f, 0.32f, 1, 0, 1f)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play correct chime", e)
        }
    }

    fun playIncorrectBuzz(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play incorrect buzz", e)
        }
    }

    fun playClickTone(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play click tone", e)
        }
    }

    fun vibrateCorrect(vibrationEnabled: Boolean) {
        if (!vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrate correct failed", e)
        }
    }

    fun vibrateIncorrect(vibrationEnabled: Boolean) {
        if (!vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 14, 22, 18)
                val amplitudes = intArrayOf(0, 35, 70, 30)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrate incorrect failed", e)
        }
    }

    fun vibrateTick(vibrationEnabled: Boolean) {
        if (!vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, 60))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrate tick failed", e)
        }
    }

    fun speakJapanese(text: String) {
        if (!isTtsReady || text.isBlank()) return
        try {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nihongo_tts_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.w(TAG, "TTS speak failed", e)
        }
    }

    fun stopSpeech() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "TTS stop failed", e)
        }
    }

    fun release() {
        soundPool.release()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator release error", e)
        }
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.w(TAG, "TTS shutdown error", e)
        }
    }

    companion object {
        private const val TAG = "SoundHelper"
    }
}
