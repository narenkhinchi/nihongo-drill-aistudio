package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Kanji
import com.example.data.KanjiRepository
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.BrandPurpleLight
import com.example.ui.theme.BrandPurpleVeryLight
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.MainBackground
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftPurpleBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCompletedScreen(
  kanjiList: List<Kanji>,
  lessonTitle: String = "Lesson 1",
  lessonLevel: String = "N5",
  onReviseAgain: () -> Unit,
  onStartNextLesson: () -> Unit,
  onBackToLesson: () -> Unit,
  onPlayKanjiAudio: (String) -> Unit,
  onSelectKanjiForReview: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val totalVocabWords = kanjiList.sumOf { KanjiRepository.getVocabularyForKanji(it.number).size }

  // Handle Android system back press
  BackHandler {
    onBackToLesson()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackground),
    containerColor = MainBackground,
    bottomBar = {
      // Bottom sticky actions: Revise Again / Start Next Lesson
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding(),
        color = CardSurface,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, BorderOutline)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Revise Again button
          OutlinedButton(
            onClick = onReviseAgain,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("revise_again_button"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.2.dp, BrandPurple),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = CardSurface,
              contentColor = BrandPurple
            )
          ) {
            Icon(
              Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "Revise Again",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              maxLines = 1,
              softWrap = false
            )
          }

          // Start Next Lesson button
          Button(
            onClick = onStartNextLesson,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("next_lesson_button"),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = BrandPurple,
              contentColor = Color.White
            )
          ) {
            Text(
              text = "Next Lesson",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              maxLines = 1,
              softWrap = false
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(17.dp)
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .statusBarsPadding()
    ) {
      // Top Navigation Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackToLesson,
          modifier = Modifier.testTag("completed_back_button")
        ) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = PrimaryText
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = "$lessonTitle Overview",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryText
        )
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
      ) {
        // Hero Celebration Card
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.2.dp, BorderOutline),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clip(CircleShape)
                  .background(BrandPurpleLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.EmojiEvents,
                  contentDescription = "Trophy",
                  tint = BrandPurple,
                  modifier = Modifier.size(38.dp)
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = "$lessonTitle Completed! 🎉",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = "お疲れ様でした！ You have learned all ${kanjiList.size} Kanji from $lessonLevel · $lessonTitle.",
                fontSize = 14.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
              )

              Spacer(modifier = Modifier.height(18.dp))

              // Stat Badges Row (Kanji & Vocabulary)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                StatBadge(
                  label = "Kanji Learned",
                  value = "${kanjiList.size} / ${kanjiList.size}",
                  modifier = Modifier.weight(1f)
                )
                StatBadge(
                  label = "Vocabulary Words",
                  value = "$totalVocabWords Words",
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }

        // Section Title: "Lesson Kanji Summary"
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Kanji Learned (${kanjiList.size})",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = PrimaryText
            )
            Text(
              text = "Tap to review",
              fontSize = 12.5.sp,
              color = SecondaryText
            )
          }
        }

        // List of all Kanji items in order with pronunciation and readings
        itemsIndexed(kanjiList) { index, kanji ->
          KanjiOverviewItemCard(
            kanji = kanji,
            onCardClick = { onSelectKanjiForReview(index) },
            onPlayAudio = { onPlayKanjiAudio(kanji.character) }
          )
        }
      }
    }
  }
}

@Composable
private fun StatBadge(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(CardSurfaceElevated)
      .border(1.2.dp, BorderOutline, RoundedCornerShape(14.dp))
      .padding(vertical = 12.dp, horizontal = 12.dp)
  ) {
    Text(
      text = value,
      fontSize = 17.sp,
      fontWeight = FontWeight.Bold,
      color = BrandPurple
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = label,
      fontSize = 12.5.sp,
      fontWeight = FontWeight.Medium,
      color = SecondaryText
    )
  }
}

@Composable
private fun KanjiOverviewItemCard(
  kanji: Kanji,
  onCardClick: () -> Unit,
  onPlayAudio: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onCardClick)
      .testTag("kanji_overview_item_${kanji.number}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurface),
    border = BorderStroke(1.2.dp, BorderOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Big Kanji Box
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(BrandPurpleVeryLight)
          .border(1.dp, SoftPurpleBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = kanji.character,
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryText
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Middle details: Meaning + Kun/On readings
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${kanji.number}. ${kanji.meaning}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = "Kun: ${kanji.kunReadings.joinToString(", ")}  ·  On: ${kanji.onReadingsDisplay.joinToString(", ")}",
          fontSize = 12.5.sp,
          color = SecondaryText
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "${kanji.strokeCount} strokes  ·  ${KanjiRepository.getVocabularyForKanji(kanji.number).size} vocab words",
          fontSize = 11.5.sp,
          color = BrandPurple,
          fontWeight = FontWeight.Medium
        )
      }

      // Speaker icon button
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(BrandPurpleVeryLight)
          .border(1.dp, SoftPurpleBorder, CircleShape)
          .clickable(onClick = onPlayAudio),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.AutoMirrored.Filled.VolumeUp,
          contentDescription = "Pronounce ${kanji.character}",
          tint = BrandPurple,
          modifier = Modifier.size(19.dp)
        )
      }
    }
  }
}
