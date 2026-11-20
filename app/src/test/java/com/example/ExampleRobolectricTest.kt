package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.KanjiRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nihongo Drill", appName)
  }

  @Test
  fun `verify kanji repository data loaded`() {
    val kanjiList = KanjiRepository.lesson1Kanji
    assertEquals(10, kanjiList.size)

    val firstKanji = kanjiList[0]
    assertEquals("日", firstKanji.character)
    assertEquals("Sun · Day", firstKanji.meaning)
    assertEquals(4, firstKanji.strokeCount)

    val vocab = KanjiRepository.getVocabularyForKanji(1)
    assertTrue(vocab.isNotEmpty())
    assertEquals("日", vocab[0].word)
  }
}
