package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class JapaneseTtsHelper(context: Context) {
  private var tts: TextToSpeech? = null
  private var isInitialized = false

  init {
    tts = TextToSpeech(context.applicationContext) { status ->
      if (status == TextToSpeech.SUCCESS) {
        val japanResult = tts?.setLanguage(Locale.JAPAN)
        val result = if (japanResult == TextToSpeech.LANG_MISSING_DATA || japanResult == TextToSpeech.LANG_NOT_SUPPORTED) {
          tts?.setLanguage(Locale.JAPANESE)
        } else {
          japanResult
        }
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
          Log.w("JapaneseTtsHelper", "Japanese language is not supported or missing data")
          // Fallback to default locale if Japanese is missing
          tts?.language = Locale.getDefault()
        } else {
          tts?.setPitch(1.0f)
          tts?.setSpeechRate(0.85f) // slightly slower for language learners
          isInitialized = true
        }
      } else {
        Log.e("JapaneseTtsHelper", "TTS Initialization failed: $status")
      }
    }
  }

  fun speak(text: String) {
    val clean = sanitizeForSpeech(text)
    if (clean.isBlank()) return
    tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "nihongo_drill_${System.currentTimeMillis()}")
  }

  fun shutdown() {
    tts?.stop()
    tts?.shutdown()
    tts = null
    isInitialized = false
  }

  companion object {
    /**
     * Cleans textbook formatting symbols before passing to Android Japanese TTS:
     * - Strips okurigana/prefix/suffix hyphens (-, –, —), brackets (（, ）, (, )), wave dash (〜),
     *   and furigana middle dots (・) so TTS never pronounces "mainasu" / "haifun" or chops syllables.
     * - Preserves katakana long-vowel mark ー (U+30FC).
     * - Converts alternative separators (／, /, ＝, =) into a natural Japanese pause (、).
     */
    fun sanitizeForSpeech(raw: String): String {
      return raw
        .replace("／", "、")
        .replace("/", "、")
        .replace("＝", "、")
        .replace("=", "、")
        .replace("・", "")
        .replace("（", "")
        .replace("）", "")
        .replace("(", "")
        .replace(")", "")
        .replace("[", "")
        .replace("]", "")
        .replace("-", "")
        .replace("–", "")
        .replace("—", "")
        .replace("〜", "")
        .replace("~", "")
        .replace(Regex("、{2,}"), "、")
        .replace(Regex("\\s+"), " ")
        .trim()
        .trim('、')
    }
  }
}
