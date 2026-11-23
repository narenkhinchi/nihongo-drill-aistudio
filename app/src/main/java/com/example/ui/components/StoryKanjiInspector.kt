package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Kanji
import com.example.data.KanjiLesson
import com.example.data.KanjiRepository
import com.example.data.KanjiStory
import com.example.data.KanjiStoryVocab
import com.example.data.Vocabulary
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.BrandPurpleDark
import com.example.ui.theme.BrandPurpleLight
import com.example.ui.theme.BrandPurpleVeryLight
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftPurpleBorder
import com.example.ui.theme.isKanjiDarkTheme

sealed interface StoryKanjiLookupResult {
  val character: Char

  data class FullKanji(
    override val character: Char,
    val kanji: Kanji,
    val lessonTitle: String,
    val level: String,
    val examples: List<Vocabulary>
  ) : StoryKanjiLookupResult

  data class StoryVocabHint(
    override val character: Char,
    val vocab: KanjiStoryVocab
  ) : StoryKanjiLookupResult
}

internal fun isCjkKanji(ch: Char): Boolean {
  return ch in '\u4E00'..'\u9FFF' ||
    ch in '\u3400'..'\u4DBF' ||
    ch in '\uF900'..'\uFAFF'
}

/**
 * Dynamically builds a 2-layer lookup map for any story:
 * 1. All built-in and Firebase-synced Kanji lessons (full meaning, readings, top 3 examples).
 * 2. Fallback to the current story's vocabulary list for Kanji whose full lesson is not uploaded yet.
 */
fun buildStoryKanjiIndex(
  allLessons: List<KanjiLesson>,
  story: KanjiStory
): Map<Char, StoryKanjiLookupResult> {
  val result = LinkedHashMap<Char, StoryKanjiLookupResult>()

  // Layer 1: Uploaded & built-in lessons
  for (lesson in allLessons) {
    for (kanji in lesson.kanjiList) {
      val ch = kanji.character.firstOrNull() ?: continue
      if (!result.containsKey(ch)) {
        val examples = KanjiRepository.getVocabularyForKanji(kanji.number).take(3)
        result[ch] = StoryKanjiLookupResult.FullKanji(
          character = ch,
          kanji = kanji,
          lessonTitle = lesson.title,
          level = lesson.level,
          examples = examples
        )
      }
    }
  }

  // Layer 2: Story vocabulary fallback for future-lesson Kanji
  for (vocab in story.vocabulary) {
    for (ch in vocab.wordWithReading) {
      if (isCjkKanji(ch) && !result.containsKey(ch)) {
        result[ch] = StoryKanjiLookupResult.StoryVocabHint(
          character = ch,
          vocab = vocab
        )
      }
    }
  }

  return result
}

private fun parseStoryVocabDisplay(wordWithReading: String): Pair<String, String?> {
  val openFull = wordWithReading.indexOf('（')
  val closeFull = wordWithReading.indexOf('）', startIndex = (openFull + 1).coerceAtLeast(0))
  if (openFull > 0 && closeFull > openFull) {
    val word = wordWithReading.substring(0, openFull).trim()
    val reading = wordWithReading.substring(openFull + 1, closeFull).trim()
    return word to reading.ifBlank { null }
  }
  val openAscii = wordWithReading.indexOf('(')
  val closeAscii = wordWithReading.indexOf(')', startIndex = (openAscii + 1).coerceAtLeast(0))
  if (openAscii > 0 && closeAscii > openAscii) {
    val word = wordWithReading.substring(0, openAscii).trim()
    val reading = wordWithReading.substring(openAscii + 1, closeAscii).trim()
    return word to reading.ifBlank { null }
  }
  return wordWithReading.trim() to null
}

@Composable
fun InteractiveStoryParagraph(
  text: String,
  kanjiIndex: Map<Char, StoryKanjiLookupResult>,
  isKanjiPopupEnabled: Boolean,
  selectedCharIndex: Int?,
  onKanjiTap: (charIndex: Int, lookup: StoryKanjiLookupResult) -> Unit,
  modifier: Modifier = Modifier
) {
  var textLayoutResult by remember(text) { mutableStateOf<TextLayoutResult?>(null) }

  val tappableIndices = remember(text, kanjiIndex) {
    buildList {
      text.forEachIndexed { idx, ch ->
        if (kanjiIndex.containsKey(ch)) add(idx)
      }
    }
  }

  val darkTheme = isKanjiDarkTheme
  val underlineColor = if (darkTheme) {
    BrandPurpleDark.copy(alpha = 0.68f)
  } else {
    BrandPurple.copy(alpha = 0.58f)
  }
  val highlightColor = if (darkTheme) {
    BrandPurple.copy(alpha = 0.30f)
  } else {
    BrandPurple.copy(alpha = 0.16f)
  }

  val tapModifier = if (isKanjiPopupEnabled && tappableIndices.isNotEmpty()) {
    Modifier.pointerInput(text, kanjiIndex, isKanjiPopupEnabled) {
      detectTapGestures { tapOffset ->
        val layout = textLayoutResult ?: return@detectTapGestures
        val rawOffset = layout.getOffsetForPosition(tapOffset)
        val candidates = listOf(rawOffset, rawOffset - 1, rawOffset + 1)
          .filter { it in text.indices }
          .distinct()

        // First: exact glyph bounding-box check so adjacent Kanji never collide
        val exactCharIndex = candidates.firstOrNull { idx ->
          val box = layout.getBoundingBox(idx)
          box.width > 0f &&
            tapOffset.x >= box.left &&
            tapOffset.x <= box.right &&
            tapOffset.y >= box.top &&
            tapOffset.y <= box.bottom
        }

        val resolvedIndex = when {
          exactCharIndex != null && kanjiIndex.containsKey(text[exactCharIndex]) -> exactCharIndex
          exactCharIndex != null -> null // User clearly tapped a non-Kanji character
          else -> {
            // Slight 3.dp tolerance for taps right on the edge of a Kanji glyph
            val pad = 3.dp.toPx()
            candidates.firstOrNull { idx ->
              if (!kanjiIndex.containsKey(text[idx])) return@firstOrNull false
              val box = layout.getBoundingBox(idx)
              box.width > 0f &&
                tapOffset.x >= (box.left - pad) &&
                tapOffset.x <= (box.right + pad) &&
                tapOffset.y >= (box.top - pad) &&
                tapOffset.y <= (box.bottom + pad)
            }
          }
        }

        if (resolvedIndex != null) {
          val lookup = kanjiIndex[text[resolvedIndex]]
          if (lookup != null) {
            onKanjiTap(resolvedIndex, lookup)
          }
        }
      }
    }
  } else {
    Modifier
  }

  Text(
    text = text,
    fontSize = 16.5.sp,
    lineHeight = 28.sp,
    fontWeight = FontWeight.Medium,
    color = PrimaryText,
    onTextLayout = { textLayoutResult = it },
    modifier = modifier
      .fillMaxWidth()
      .drawBehind {
        if (!isKanjiPopupEnabled) return@drawBehind
        val layout = textLayoutResult ?: return@drawBehind

        // Draw subtle selection highlight behind currently inspected Kanji
        if (selectedCharIndex != null && selectedCharIndex in text.indices) {
          val box = layout.getBoundingBox(selectedCharIndex)
          if (box.width > 0f && box.height > 0f) {
            val line = layout.getLineForOffset(selectedCharIndex)
            val baseline = layout.getLineBaseline(line)
            val lineBottom = layout.getLineBottom(line)
            val top = (baseline - 16.5.sp.toPx() * 0.95f).coerceAtLeast(box.top)
            val bottom = (baseline + 3.5.dp.toPx()).coerceAtMost(lineBottom)
            val hPad = 1.5.dp.toPx()
            drawRoundRect(
              color = highlightColor,
              topLeft = Offset(box.left - hPad, top),
              size = Size(box.width + hPad * 2f, (bottom - top).coerceAtLeast(4f)),
              cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
          }
        }

        // Draw subtle dotted underline directly below each tappable Kanji
        val dashEffect = PathEffect.dashPathEffect(
          floatArrayOf(2.2.dp.toPx(), 2.6.dp.toPx()),
          0f
        )
        val strokeWidth = 1.35.dp.toPx()
        val baselineOffset = 3.dp.toPx()

        for (idx in tappableIndices) {
          if (idx !in text.indices) continue
          val box = layout.getBoundingBox(idx)
          if (box.width <= 0f || box.height <= 0f) continue
          val line = layout.getLineForOffset(idx)
          val baseline = layout.getLineBaseline(line)
          val lineBottom = layout.getLineBottom(line)
          val y = (baseline + baselineOffset).coerceAtMost(lineBottom - 1.dp.toPx())
          val inset = (box.width * 0.08f).coerceAtMost(1.8.dp.toPx())
          drawLine(
            color = underlineColor,
            start = Offset(box.left + inset, y),
            end = Offset(box.right - inset, y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = dashEffect
          )
        }
      }
      .then(tapModifier)
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StoryKanjiBottomSheet(
  lookupResult: StoryKanjiLookupResult,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = CardSurface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = {
      BottomSheetDefaults.DragHandle(color = BorderOutline)
    },
    modifier = Modifier.testTag("story_kanji_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 22.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      when (lookupResult) {
        is StoryKanjiLookupResult.FullKanji -> {
          FullKanjiSheetContent(
            result = lookupResult,
            onDismiss = onDismiss
          )
        }
        is StoryKanjiLookupResult.StoryVocabHint -> {
          StoryVocabHintSheetContent(
            result = lookupResult,
            onDismiss = onDismiss
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FullKanjiSheetContent(
  result: StoryKanjiLookupResult.FullKanji,
  onDismiss: () -> Unit
) {
  val kanji = result.kanji
  val kunList = kanji.kunReadings.filter { it.isNotBlank() }
  val onList = kanji.onReadingsDisplay.ifEmpty { kanji.onReadings }.filter { it.isNotBlank() }

  // Header: Kanji Box + Meaning + Lesson Badge + Close Button
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(58.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(BrandPurpleVeryLight)
        .border(1.2.dp, SoftPurpleBorder, RoundedCornerShape(16.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = kanji.character,
        fontSize = 31.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryText
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(
        text = kanji.meaning,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        color = PrimaryText,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = BrandPurpleLight
        ) {
          Text(
            text = "${result.level} · ${result.lessonTitle}",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPurpleDark,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
        if (kanji.strokeCount > 0) {
          Text(
            text = "${kanji.strokeCount} strokes",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = SecondaryText
          )
        }
      }
    }

    IconButton(
      onClick = onDismiss,
      modifier = Modifier
        .size(48.dp)
        .testTag("story_kanji_sheet_close")
    ) {
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close Kanji details",
        tint = SecondaryText
      )
    }
  }

  // Readings Section (Kun'yomi & On'yomi)
  if (kunList.isNotEmpty() || onList.isNotEmpty()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(CardSurfaceElevated)
        .border(1.dp, BorderOutline, RoundedCornerShape(14.dp))
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (kunList.isNotEmpty()) {
        ReadingChipRow(label = "Kun", readings = kunList)
      }
      if (kunList.isNotEmpty() && onList.isNotEmpty()) {
        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
      }
      if (onList.isNotEmpty()) {
        ReadingChipRow(label = "On", readings = onList)
      }
    }
  }

  // Common Examples Section (Top 3)
  if (result.examples.isNotEmpty()) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "Common Examples",
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = SecondaryText,
        modifier = Modifier.padding(horizontal = 2.dp)
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(CardSurfaceElevated)
          .border(1.dp, BorderOutline, RoundedCornerShape(14.dp))
          .padding(horizontal = 14.dp, vertical = 4.dp)
      ) {
        result.examples.forEachIndexed { idx, vocab ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(
              modifier = Modifier.weight(1.1f),
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = vocab.word,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
              )
              if (vocab.reading.isNotBlank()) {
                Text(
                  text = vocab.reading,
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = BrandPurpleDark
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
              text = vocab.meaning,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Medium,
              color = SecondaryText,
              textAlign = TextAlign.End,
              modifier = Modifier.weight(0.95f)
            )
          }

          if (idx < result.examples.lastIndex) {
            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadingChipRow(
  label: String,
  readings: List<String>
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = BrandPurpleLight,
      modifier = Modifier
        .width(44.dp)
        .padding(top = 2.dp)
    ) {
      Text(
        text = label,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = BrandPurpleDark,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 4.dp)
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    FlowRow(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      readings.forEach { reading ->
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = CardSurface,
          border = BorderStroke(1.dp, BorderOutline)
        ) {
          Text(
            text = reading,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun StoryVocabHintSheetContent(
  result: StoryKanjiLookupResult.StoryVocabHint,
  onDismiss: () -> Unit
) {
  val (baseWord, reading) = remember(result.vocab.wordWithReading) {
    parseStoryVocabDisplay(result.vocab.wordWithReading)
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(58.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(BrandPurpleVeryLight)
        .border(1.2.dp, SoftPurpleBorder, RoundedCornerShape(16.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = result.character.toString(),
        fontSize = 31.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryText
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = baseWord,
          fontSize = 18.sp,
          fontWeight = FontWeight.ExtraBold,
          color = PrimaryText
        )
        if (!reading.isNullOrBlank()) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = CardSurfaceElevated,
            border = BorderStroke(1.dp, BorderOutline)
          ) {
            Text(
              text = reading,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = BrandPurpleDark,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = BrandPurpleLight
      ) {
        Text(
          text = "From this story · Full lesson coming soon",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = BrandPurpleDark,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
    }

    IconButton(
      onClick = onDismiss,
      modifier = Modifier
        .size(48.dp)
        .testTag("story_kanji_sheet_close")
    ) {
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close Kanji details",
        tint = SecondaryText
      )
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(CardSurfaceElevated)
      .border(1.dp, BorderOutline, RoundedCornerShape(14.dp))
      .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = "Meaning in this story",
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = SecondaryText
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = result.vocab.meaning,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = PrimaryText
    )
  }
}
