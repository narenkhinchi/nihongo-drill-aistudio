package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Kanji
import com.example.data.KanjiLesson
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
import com.example.ui.theme.isKanjiDarkTheme
import com.example.ui.viewmodel.KanjiUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiHomeScreen(
  uiState: KanjiUiState,
  onSelectLevel: (String) -> Unit,
  onSelectFilter: (String) -> Unit,
  onStartLesson: (KanjiLesson) -> Unit,
  onPreviewLessonKanji: (KanjiLesson) -> Unit,
  onClosePreviewLesson: () -> Unit,
  onSelectKanjiInLesson: (KanjiLesson, Int) -> Unit,
  onOpenSearch: () -> Unit,
  onCloseSearch: () -> Unit,
  onUpdateSearchQuery: (String) -> Unit,
  onSelectKanjiFromSearch: (Kanji, Boolean) -> Unit,
  onPlayAudio: (String) -> Unit,
  onToggleBookmark: (Int) -> Unit,
  onToggleHideOtherLessons: () -> Unit = {},
  onOpenReading: () -> Unit = {},
  onStartQuiz: () -> Unit = {},
  onBackToVocabulary: (() -> Unit)? = null,
  isDarkTheme: Boolean = isKanjiDarkTheme,
  onToggleTheme: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val levels = listOf("N5", "N4", "N3", "N2", "N1")
  val filters = listOf("All", "To Learn", "Completed", "Starred")

  BackHandler(enabled = onBackToVocabulary != null) {
    onBackToVocabulary?.invoke()
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackground),
    containerColor = MainBackground,
    bottomBar = {
      // Sticky Bottom Bar: Unified "Search Kanji" CTA
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding(),
        color = CardSurface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, BorderOutline)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onOpenSearch,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("search_kanji_bottom_button"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.4.dp, BrandPurple.copy(alpha = 0.45f)),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = BrandPurpleVeryLight.copy(alpha = 0.55f),
              contentColor = BrandPurple
            )
          ) {
            Icon(
              Icons.Default.Search,
              contentDescription = null,
              tint = BrandPurple,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Search Kanji · Meaning, Reading or Character",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .statusBarsPadding()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
      // 1. Top "<- Vocabulary" navigation + Header: Kanji / Your book · Your pace + Book/Search Icon
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable(enabled = onBackToVocabulary != null) {
                onBackToVocabulary?.invoke()
              }
              .padding(vertical = 4.dp)
              .testTag("back_to_vocabulary_button"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Vocabulary",
              tint = BrandPurple,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Vocabulary",
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Bold,
              color = BrandPurple
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Kanji",
              fontSize = 28.sp,
              fontWeight = FontWeight.ExtraBold,
              color = PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Your book · Your pace",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Medium,
              color = SecondaryText
            )
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Dark / Light Mode Toggle Icon Button (matching Vocab Home screen)
            if (onToggleTheme != null) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(CardSurface)
                  .border(1.2.dp, BorderOutline, RoundedCornerShape(12.dp))
                  .clickable(onClick = onToggleTheme)
                  .testTag("kanji_toggle_theme_button"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                  contentDescription = if (isDarkTheme) "Switch to light mode" else "Switch to night mode",
                  tint = PrimaryText,
                  modifier = Modifier.size(21.dp)
                )
              }
            }

            // Quick Search Icon Button
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardSurface)
                .border(1.2.dp, BorderOutline, RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenSearch),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Search,
                contentDescription = "Search Kanji",
                tint = BrandPurple,
                modifier = Modifier.size(21.dp)
              )
            }

            // Book / Starred Quick Filter Icon Button
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (uiState.selectedFilter == "Starred") BrandPurpleLight else CardSurface)
                .border(
                  1.2.dp,
                  if (uiState.selectedFilter == "Starred") BrandPurple else BorderOutline,
                  RoundedCornerShape(12.dp)
                )
                .clickable {
                  onSelectFilter(if (uiState.selectedFilter == "Starred") "All" else "Starred")
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = "Book Curriculum",
                tint = PrimaryText,
                modifier = Modifier.size(21.dp)
              )
            }
          }
          }
        }
      }

      // 2. JLPT Level Tabs: N5 | N4 | N3 | N2 | N1
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          levels.forEach { level ->
            val isSelected = uiState.selectedLevel == level
            Surface(
              onClick = { onSelectLevel(level) },
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("level_tab_$level"),
              shape = RoundedCornerShape(13.dp),
              color = if (isSelected) BrandPurple else CardSurface,
              border = BorderStroke(
                1.2.dp,
                if (isSelected) BrandPurple else BorderOutline
              )
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = level,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isSelected) Color.White else PrimaryText
                )
              }
            }
          }
        }
      }

      // 3. Progress Summary Card matching Vocab home screen layout & spacing
      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("your_progress_card"),
          shape = RoundedCornerShape(18.dp),
          color = CardSurface,
          border = BorderStroke(1.dp, BorderOutline)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              "Completed" to "${uiState.completedLessonsCountForLevel}/${uiState.lessonsForSelectedLevel.size}",
              "Accuracy" to uiState.formattedAccuracy,
              "Practiced" to "${uiState.practicedKanjiCountForLevel}"
            ).forEach { (label, value) ->
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = value,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryText
                )
                Text(
                  text = label,
                  style = MaterialTheme.typography.bodySmall,
                  color = SecondaryText
                )
              }
            }
          }
        }
      }

      // 3b. Action Buttons Row: [ Reading ] and [ Start Quiz ] below Progress Summary Card
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onOpenReading,
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
              .testTag("kanji_reading_button"),
            border = BorderStroke(1.dp, BorderOutline),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = CardSurface,
              contentColor = BrandPurple
            )
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.MenuBook,
              contentDescription = null,
              tint = BrandPurple,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Reading",
              fontWeight = FontWeight.SemiBold
            )
          }

          Button(
            onClick = onStartQuiz,
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
              .testTag("kanji_start_quiz_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = BrandPurple,
              contentColor = Color.White
            )
          ) {
            Text(
              text = "Start Quiz",
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // 4. Visibility toggle ("All lessons visible" + Eye icon), Filter Chips, & Lessons Header
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (uiState.hideOtherLessons) {
                "Showing lesson ${uiState.activeLessonNumber}"
              } else {
                "All lessons visible"
              },
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.bodySmall,
              color = SecondaryText
            )
            IconToggleButton(
              checked = uiState.hideOtherLessons,
              onCheckedChange = { onToggleHideOtherLessons() },
              modifier = Modifier.testTag("kanji_toggle_other_lessons")
            ) {
              Icon(
                imageVector = if (uiState.hideOtherLessons) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (uiState.hideOtherLessons) "Show all lessons" else "Hide other lessons",
                tint = PrimaryText
              )
            }
          }

          if (!uiState.hideOtherLessons) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              filters.forEach { filter ->
                val isSelected = uiState.selectedFilter == filter
                val chipLabel = if (filter == "All") {
                  "All (${uiState.lessonsForSelectedLevel.size})"
                } else {
                  filter
                }
                FilterChip(
                  selected = isSelected,
                  onClick = { onSelectFilter(filter) },
                  label = {
                    Text(
                      text = chipLabel,
                      fontWeight = FontWeight.Medium
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandPurpleLight,
                    selectedLabelColor = BrandPurple,
                    containerColor = CardSurface,
                    labelColor = PrimaryText
                  ),
                  border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = BorderOutline,
                    selectedBorderColor = BrandPurple.copy(alpha = 0.25f)
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Lessons",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = PrimaryText
            )

            Text(
              text = "${uiState.selectedLevel} · ${uiState.totalKanjiInLevel} Kanji",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = SecondaryText
            )
          }
        }
      }

      // 5. Lesson Cards List
      val displayedLessons = uiState.filteredLessons
      if (displayedLessons.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, BorderOutline)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = when (uiState.selectedFilter) {
                  "Completed" -> "No completed lessons in ${uiState.selectedLevel} yet"
                  "Starred" -> "No starred Kanji in ${uiState.selectedLevel} yet"
                  else -> "All lessons in ${uiState.selectedLevel} completed!"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Start a lesson or bookmark Kanji with ★ while studying.",
                fontSize = 13.sp,
                color = SecondaryText
              )
            }
          }
        }
      } else {
        items(displayedLessons, key = { it.id }) { lesson ->
          val isCompleted = uiState.completedLessonIds.contains(lesson.id)
          val bestScore = uiState.lessonBestScores[lesson.id]

          LessonCardItem(
            lesson = lesson,
            isCompleted = isCompleted,
            bestScore = bestScore,
            onPreviewClick = { onPreviewLessonKanji(lesson) },
            onStartClick = { onStartLesson(lesson) }
          )
        }
      }
    }
  }

  // Lesson Kanji Preview Bottom Sheet (when user taps "Kanji" button on any Lesson Card)
  uiState.previewLesson?.let { lesson ->
    LessonKanjiPreviewSheet(
      lesson = lesson,
      bookmarkedIds = uiState.bookmarkedIds,
      practicedIds = uiState.practicedKanjiIds,
      onDismiss = onClosePreviewLesson,
      onPlayAudio = onPlayAudio,
      onToggleBookmark = onToggleBookmark,
      onStartFullLesson = { onStartLesson(lesson) },
      onSelectKanji = { index -> onSelectKanjiInLesson(lesson, index) }
    )
  }

  // Interactive Kanji Search Sheet (when user taps "Search Kanji")
  if (uiState.isSearchSheetOpen) {
    KanjiSearchBottomSheet(
      query = uiState.searchQuery,
      onQueryChange = onUpdateSearchQuery,
      bookmarkedIds = uiState.bookmarkedIds,
      onDismiss = onCloseSearch,
      onPlayAudio = onPlayAudio,
      onToggleBookmark = onToggleBookmark,
      onSelectKanji = onSelectKanjiFromSearch
    )
  }
}

@Composable
private fun LessonCardItem(
  lesson: KanjiLesson,
  isCompleted: Boolean,
  bestScore: Int?,
  onPreviewClick: () -> Unit,
  onStartClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("lesson_card_${lesson.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurface),
    border = BorderStroke(1.dp, BorderOutline)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(
        text = lesson.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = PrimaryText
      )

      Text(
        text = "${lesson.kanjiList.size} kanji" + (bestScore?.let { " · Best $it%" } ?: ""),
        style = MaterialTheme.typography.bodySmall,
        color = SecondaryText
      )

      // Action Buttons Row: Pill-shaped [ Kanji ] and [ Start ] matching Vocab Lesson card
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = onPreviewClick,
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .testTag("preview_kanji_${lesson.id}"),
          border = BorderStroke(1.dp, BorderOutline),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CardSurface,
            contentColor = BrandPurple
          )
        ) {
          Text(
            text = "Kanji",
            fontWeight = FontWeight.SemiBold
          )
        }

        Button(
          onClick = onStartClick,
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .testTag("start_lesson_${lesson.id}"),
          colors = ButtonDefaults.buttonColors(
            containerColor = BrandPurple,
            contentColor = Color.White
          )
        ) {
          Text(
            text = if (isCompleted) "Revise" else "Start",
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

/**
 * Bottom Sheet opened when tapping "Kanji" button on a Lesson Card.
 * Shows all Kanji in that lesson with readings, meanings, audio, and direct jump.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonKanjiPreviewSheet(
  lesson: KanjiLesson,
  bookmarkedIds: Set<Int>,
  practicedIds: Set<Int>,
  onDismiss: () -> Unit,
  onPlayAudio: (String) -> Unit,
  onToggleBookmark: (Int) -> Unit,
  onStartFullLesson: () -> Unit,
  onSelectKanji: (Int) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = CardSurface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 28.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = lesson.title,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryText
          )
          Text(
            text = "${lesson.level} · ${lesson.kanjiList.size} Kanji in this lesson",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandPurple
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = SecondaryText)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f, fill = false),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(lesson.kanjiList.size) { index ->
          val kanji = lesson.kanjiList[index]
          val isStarred = bookmarkedIds.contains(kanji.number)
          val isPracticed = practicedIds.contains(kanji.number)

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectKanji(index) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated),
            border = BorderStroke(1.dp, if (isPracticed) BrandPurple.copy(alpha = 0.45f) else BorderOutline)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(BrandPurpleVeryLight)
                  .border(1.dp, SoftPurpleBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = kanji.character,
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryText
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${index + 1}. ${kanji.meaning}",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryText
                )
                Spacer(modifier = Modifier.height(2.dp))
                val kun = if (kanji.kunReadings.isNotEmpty()) "Kun: ${kanji.kunReadings.joinToString(", ")}" else ""
                val on = if (kanji.onReadingsDisplay.isNotEmpty()) "On: ${kanji.onReadingsDisplay.joinToString(", ")}" else ""
                val combined = listOf(kun, on).filter { it.isNotEmpty() }.joinToString("  ·  ")
                Text(
                  text = combined,
                  fontSize = 12.sp,
                  color = SecondaryText,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              IconButton(
                onClick = { onToggleBookmark(kanji.number) },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  if (isStarred) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                  contentDescription = "Star Kanji",
                  tint = if (isStarred) BrandPurple else SecondaryText,
                  modifier = Modifier.size(20.dp)
                )
              }

              IconButton(
                onClick = { onPlayAudio(kanji.character) },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  Icons.AutoMirrored.Filled.VolumeUp,
                  contentDescription = "Pronounce",
                  tint = BrandPurple,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Button(
        onClick = onStartFullLesson,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = BrandPurple,
          contentColor = Color.White
        )
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Start ${lesson.title}",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }
    }
  }
}

/**
 * Interactive Kanji Search & Dictionary Bottom Sheet.
 * Searches across all JLPT levels by character, English meaning, or Kun/On reading.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KanjiSearchBottomSheet(
  query: String,
  onQueryChange: (String) -> Unit,
  bookmarkedIds: Set<Int>,
  onDismiss: () -> Unit,
  onPlayAudio: (String) -> Unit,
  onToggleBookmark: (Int) -> Unit,
  onSelectKanji: (Kanji, Boolean) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedLevelFilter by remember { mutableStateOf("All") }
  val filterOptions = listOf("All", "N5", "N4", "N3", "N2", "N1", "Starred")

  val results = remember(query, selectedLevelFilter, bookmarkedIds) {
    val q = query.trim().lowercase()
    KanjiRepository.allKanjiAcrossLevels.filter { kanji ->
      val levelMatches = when (selectedLevelFilter) {
        "All" -> true
        "Starred" -> bookmarkedIds.contains(kanji.number)
        else -> kanji.level == selectedLevelFilter
      }
      val queryMatches = if (q.isEmpty()) {
        true
      } else {
        kanji.character.contains(q) ||
          kanji.meaning.lowercase().contains(q) ||
          kanji.kunReadings.any { it.contains(q) } ||
          kanji.onReadingsDisplay.any { it.contains(q) } ||
          kanji.onReadings.any { it.lowercase().contains(q) }
      }
      levelMatches && queryMatches
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = CardSurface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 28.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Search Kanji",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryText
          )
          Text(
            text = "Search by English meaning, reading, or character",
            fontSize = 12.5.sp,
            color = SecondaryText
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = SecondaryText)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Input Box
      OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("kanji_search_input"),
        placeholder = {
          Text(
            text = "Try 'sun', 'mountain', 'water', '日', 'ひ'...",
            fontSize = 14.sp,
            color = SecondaryText
          )
        },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = BrandPurple)
        },
        trailingIcon = {
          if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
              Icon(Icons.Default.Close, contentDescription = "Clear", tint = SecondaryText)
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = PrimaryText,
          unfocusedTextColor = PrimaryText,
          focusedBorderColor = BrandPurple,
          unfocusedBorderColor = BorderOutline,
          focusedContainerColor = CardSurfaceElevated,
          unfocusedContainerColor = CardSurfaceElevated
        )
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Level / Starred Quick Filter Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        filterOptions.forEach { opt ->
          val selected = selectedLevelFilter == opt
          Surface(
            onClick = { selectedLevelFilter = opt },
            shape = RoundedCornerShape(16.dp),
            color = if (selected) BrandPurple else CardSurfaceElevated,
            border = BorderStroke(1.dp, if (selected) BrandPurple else BorderOutline)
          ) {
            Text(
              text = opt,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (selected) Color.White else PrimaryText,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Results List
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(380.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(results, key = { it.number }) { kanji ->
          val isStarred = bookmarkedIds.contains(kanji.number)

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectKanji(kanji, false) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated),
            border = BorderStroke(1.dp, BorderOutline)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(BrandPurpleVeryLight)
                  .border(1.dp, SoftPurpleBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = kanji.character,
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryText
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = kanji.meaning,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(BrandPurpleLight)
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = kanji.level,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = BrandPurple
                    )
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val readings = (kanji.kunReadings + kanji.onReadingsDisplay).joinToString(" · ")
                Text(
                  text = "$readings  (${kanji.strokeCount} strokes)",
                  fontSize = 12.sp,
                  color = SecondaryText,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              // Direct Write Button
              IconButton(
                onClick = { onSelectKanji(kanji, true) },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  Icons.Default.Edit,
                  contentDescription = "Practice Writing",
                  tint = BrandPurple,
                  modifier = Modifier.size(18.dp)
                )
              }

              // Audio Button
              IconButton(
                onClick = { onPlayAudio(kanji.character) },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  Icons.AutoMirrored.Filled.VolumeUp,
                  contentDescription = "Pronounce",
                  tint = BrandPurple,
                  modifier = Modifier.size(19.dp)
                )
              }

              // Star Button
              IconButton(
                onClick = { onToggleBookmark(kanji.number) },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  if (isStarred) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                  contentDescription = "Bookmark",
                  tint = if (isStarred) BrandPurple else SecondaryText,
                  modifier = Modifier.size(19.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
