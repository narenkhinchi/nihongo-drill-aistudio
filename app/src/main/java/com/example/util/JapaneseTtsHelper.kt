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
        val result = tts?.setLanguage(Locale.JAPANESE)
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
    if (text.isBlank()) return
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nihongo_drill_${System.currentTimeMillis()}")
  }

  fun shutdown() {
    tts?.stop()
    tts?.shutdown()
    tts = null
    isInitialized = false
  }
}
