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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KanjiLesson
import com.example.data.KanjiRepository
import com.example.ui.components.InteractiveStoryParagraph
import com.example.ui.components.StoryKanjiBottomSheet
import com.example.ui.components.StoryKanjiLookupResult
import com.example.ui.components.buildStoryKanjiIndex
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
import com.example.ui.theme.SoftPurplePillBg
import com.example.ui.theme.isKanjiDarkTheme
import kotlinx.coroutines.delay

private val QuizGreen = Color(0xFF009D73)
private val QuizOrange = Color(0xFFFF9900)

@Composable
fun KanjiReadingScreen(
  selectedLevel: String,
  lessonsForLevel: List<KanjiLesson>,
  contentVersion: Int = 0,
  onBack: () -> Unit,
  onPlayAudio: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val readingLessons = remember(selectedLevel, lessonsForLevel, contentVersion) {
    KanjiRepository.getReadingLessonsForLevel(selectedLevel)
  }
  val storiesInLevel = remember(selectedLevel, readingLessons, contentVersion) {
    KanjiRepository.getStoriesForLevel(selectedLevel)
  }
  val exercisesInLevel = remember(selectedLevel, readingLessons, contentVersion) {
    KanjiRepository.getReadingExercisesForLevel(selectedLevel)
  }

  // Step 1: Lesson Selection state (user can pick 1 lesson, multiple lessons, or All Lessons)
  var selectedLessonIds by remember(selectedLevel, readingLessons) {
    mutableStateOf(readingLessons.map { it.id }.toSet())
  }
  var isReadingStarted by remember { mutableStateOf(false) }

  BackHandler {
    if (isReadingStarted) {
      isReadingStarted = false
    } else {
      onBack()
    }
  }

  val allLessonIds = remember(readingLessons) { readingLessons.map { it.id }.toSet() }
  val isAllSelected = selectedLessonIds.size == allLessonIds.size && allLessonIds.isNotEmpty()

  val selectedLessons = remember(readingLessons, selectedLessonIds) {
    readingLessons.filter { selectedLessonIds.contains(it.id) }
  }

  // Step 1 UI: Lesson Selector Screen for Reading (matching image_be2f43df.png)
  if (!isReadingStarted) {
    Scaffold(
      modifier = modifier
        .fillMaxSize()
        .background(MainBackground),
      containerColor = MainBackground,
      bottomBar = {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
          color = CardSurface,
          shadowElevation = 8.dp,
          border = BorderStroke(1.dp, BorderOutline)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp)
          ) {
            Button(
              onClick = {
                if (selectedLessons.isNotEmpty()) {
                  isReadingStarted = true
                }
              },
              enabled = selectedLessons.isNotEmpty(),
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("start_selected_lessons_reading_button"),
              shape = RoundedCornerShape(25.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BrandPurple,
                contentColor = Color.White,
                disabledContainerColor = BorderOutline,
                disabledContentColor = SecondaryText
              )
            ) {
              val buttonLabel = when {
                selectedLessons.isEmpty() -> "Select at least 1 lesson"
                isAllSelected -> "Start Reading · All Lessons"
                selectedLessons.size == 1 -> "Start ${selectedLessons.first().title} Reading"
                else -> "Start Reading · ${selectedLessons.size} Lessons"
              }
              Text(
                text = buttonLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp
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
        // Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("reading_back_button")
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = PrimaryText
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "$selectedLevel Kanji Reading",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = PrimaryText
            )
            Text(
              text = "Select lessons for reading or choose All Lessons",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = SecondaryText
            )
          }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp)
        ) {
          // 1. "All Lessons" Selection Card
          item {
            val allSubtitle = buildList {
              if (storiesInLevel.isNotEmpty()) {
                add(if (storiesInLevel.size == 1) "1 story" else "${storiesInLevel.size} stories")
              }
              if (exercisesInLevel.isNotEmpty()) {
                add(if (exercisesInLevel.size == 1) "1 sentence" else "${exercisesInLevel.size} sentences")
              }
            }.let { parts ->
              if (parts.isNotEmpty()) {
                "Read from $selectedLevel (${parts.joinToString(" · ")})"
              } else {
                "Select lessons from $selectedLevel for reading"
              }
            }
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedLessonIds = if (isAllSelected) {
                    emptySet()
                  } else {
                    allLessonIds
                  }
                }
                .testTag("reading_select_all_lessons_card"),
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isAllSelected) BrandPurpleVeryLight else CardSurface
              ),
              border = BorderStroke(
                width = if (isAllSelected) 1.6.dp else 1.dp,
                color = if (isAllSelected) BrandPurple else BorderOutline
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isAllSelected) BrandPurple else CardSurface)
                    .border(
                      1.5.dp,
                      if (isAllSelected) BrandPurple else Color(0xFFC5C6D0),
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isAllSelected) {
                    Icon(
                      Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "All Lessons (${readingLessons.size} Lessons)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = allSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                  )
                }

                OutlinedButton(
                  onClick = {
                    selectedLessonIds = allLessonIds
                    isReadingStarted = true
                  },
                  modifier = Modifier.heightIn(min = 38.dp),
                  border = BorderStroke(1.dp, BrandPurple),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isAllSelected) BrandPurple else CardSurface,
                    contentColor = if (isAllSelected) Color.White else BrandPurple
                  )
                ) {
                  Text(
                    text = "Start All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }

          // Section Header: Individual Lessons
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Select Lessons (${selectedLessons.size}/${readingLessons.size} selected)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
              )

              Text(
                text = if (selectedLessonIds.isNotEmpty()) "Clear" else "Select All",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPurple,
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .clickable {
                    selectedLessonIds = if (selectedLessonIds.isNotEmpty()) emptySet() else allLessonIds
                  }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          // 2. Lesson Cards
          items(readingLessons, key = { it.id }) { lesson ->
            val isLessonSelected = selectedLessonIds.contains(lesson.id)
            val lessonStories = storiesInLevel.filter { it.lessonNumber == lesson.lessonNumber }
            val lessonExercises = exercisesInLevel.filter { it.lessonNumber == lesson.lessonNumber }
            val badgeText = when {
              lessonStories.isNotEmpty() && lessonExercises.isNotEmpty() -> {
                val sLabel = if (lessonStories.size == 1) "1 story" else "${lessonStories.size} stories"
                val eLabel = if (lessonExercises.size == 1) "1 sentence" else "${lessonExercises.size} sentences"
                "$sLabel · $eLabel"
              }
              lessonStories.isNotEmpty() -> {
                if (lessonStories.size == 1) "1 story" else "${lessonStories.size} stories"
              }
              lessonExercises.isNotEmpty() -> {
                if (lessonExercises.size == 1) "1 sentence" else "${lessonExercises.size} sentences"
              }
              lesson.kanjiList.isNotEmpty() -> "${lesson.kanjiList.size} kanji"
              else -> "Reading"
            }
            val previewText = when {
              lessonStories.isNotEmpty() -> lessonStories.first().title
              lesson.kanjiList.isNotEmpty() -> lesson.kanjiList.joinToString(" ") { it.character }
              else -> lesson.subtitle
            }

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedLessonIds = if (isLessonSelected) {
                    selectedLessonIds - lesson.id
                  } else {
                    selectedLessonIds + lesson.id
                  }
                }
                .testTag("reading_lesson_card_${lesson.id}"),
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isLessonSelected) BrandPurpleVeryLight else CardSurface
              ),
              border = BorderStroke(
                width = if (isLessonSelected) 1.5.dp else 1.dp,
                color = if (isLessonSelected) BrandPurple else BorderOutline
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isLessonSelected) BrandPurple else CardSurface)
                    .border(
                      1.5.dp,
                      if (isLessonSelected) BrandPurple else SecondaryText.copy(alpha = 0.5f),
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isLessonSelected) {
                    Icon(
                      Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = lesson.title,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = PrimaryText
                    )
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = BrandPurpleLight
                    ) {
                      Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  Text(
                    text = previewText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                  onClick = {
                    selectedLessonIds = setOf(lesson.id)
                    isReadingStarted = true
                  },
                  modifier = Modifier.heightIn(min = 38.dp),
                  border = BorderStroke(1.dp, BorderOutline),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardSurface,
                    contentColor = BrandPurple
                  )
                ) {
                  Text(
                    text = "Read",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }
      }
    }
    return
  }

  // Step 2 UI: Two buttons at top ("Story Reading" and "Sentence") with smart auto-tab routing:
  // If selected lesson(s) have a Story (or both), open "Story" first; if only Sentences exist, open "Sentence" directly.
  val selectedLessonNumbers = remember(selectedLessons) {
    selectedLessons.map { it.lessonNumber }.toSet()
  }
  val storiesForSelection = remember(selectedLevel, selectedLessonNumbers, contentVersion) {
    KanjiRepository.getStoriesForLevel(selectedLevel)
      .filter { selectedLessonNumbers.contains(it.lessonNumber) }
  }
  val exercises = remember(selectedLevel, selectedLessonNumbers, contentVersion) {
    KanjiRepository.getReadingExercisesForLevel(selectedLevel)
      .filter { selectedLessonNumbers.contains(it.lessonNumber) }
  }
  val defaultReadingMode = remember(storiesForSelection, exercises) {
    when {
      storiesForSelection.isNotEmpty() -> "Story"
      exercises.isNotEmpty() -> "Sentence"
      else -> "Story"
    }
  }
  var readingMode by remember(selectedLessonIds, defaultReadingMode) {
    mutableStateOf(defaultReadingMode)
  }

  val currentStory = storiesForSelection.firstOrNull()
  var currentPageIndex by remember(currentStory?.id) { mutableIntStateOf(0) }
  val totalPages = currentStory?.pages?.size?.coerceAtLeast(1) ?: 0
  val currentPage = currentStory?.pages?.getOrElse(currentPageIndex.coerceIn(0, (totalPages - 1).coerceAtLeast(0))) {
    currentStory.pages.first()
  }

  var showTranslation by remember(currentStory?.id) { mutableStateOf(false) }
  var showSentenceTranslation by remember(selectedLessonIds) { mutableStateOf(false) }
  var isVocabExpanded by remember(currentStory?.id) { mutableStateOf(false) }
  var isKanjiPopupEnabled by rememberSaveable { mutableStateOf(true) }
  var selectedKanjiLookup by remember { mutableStateOf<StoryKanjiLookupResult?>(null) }
  var selectedParagraphIndex by remember { mutableStateOf<Int?>(null) }
  var selectedCharIndex by remember { mutableStateOf<Int?>(null) }

  LaunchedEffect(currentStory?.id, currentPageIndex, showTranslation, showSentenceTranslation, readingMode, selectedLessonIds) {
    selectedKanjiLookup = null
    selectedParagraphIndex = null
    selectedCharIndex = null
  }

  val lessonsWithReading = remember(readingLessons, storiesInLevel, exercisesInLevel) {
    readingLessons.filter { lesson ->
      storiesInLevel.any { it.lessonNumber == lesson.lessonNumber } ||
        exercisesInLevel.any { it.lessonNumber == lesson.lessonNumber }
    }
  }
  val storyAnchorLessonNumber = if (selectedLessonNumbers.size == 1) {
    selectedLessonNumbers.first()
  } else {
    currentStory?.lessonNumber
  }
  val storyLessonPos = remember(lessonsWithReading, storyAnchorLessonNumber) {
    if (storyAnchorLessonNumber != null) {
      lessonsWithReading.indexOfFirst { it.lessonNumber == storyAnchorLessonNumber }
    } else {
      -1
    }
  }
  val previousLessonForStory = if (storyLessonPos > 0) lessonsWithReading[storyLessonPos - 1] else null
  val nextLessonForStory = if (storyLessonPos in 0 until lessonsWithReading.lastIndex) {
    lessonsWithReading[storyLessonPos + 1]
  } else {
    null
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackground),
    containerColor = MainBackground,
    bottomBar = {
      if (readingMode == "Story" && currentStory != null) {
        val canGoPrevious = currentPageIndex > 0 || previousLessonForStory != null
        val canGoNext = currentPageIndex < totalPages - 1 || nextLessonForStory != null
        val previousLabel = when {
          currentPageIndex > 0 -> "Previous"
          previousLessonForStory != null -> previousLessonForStory.title
          else -> "Previous"
        }
        val nextLabel = when {
          currentPageIndex < totalPages - 1 -> "Next"
          nextLessonForStory != null -> nextLessonForStory.title
          else -> "Next"
        }

        // Bottom Navigation Bar: [ < Previous / Lesson X ]   1 / 2   [ Next / Lesson Y > ]
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
          color = MainBackground
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // < Previous pill button (previous page, or previous lesson when on Page 1)
            Surface(
              onClick = {
                if (currentPageIndex > 0) {
                  currentPageIndex--
                } else if (previousLessonForStory != null) {
                  selectedLessonIds = setOf(previousLessonForStory.id)
                }
              },
              enabled = canGoPrevious,
              shape = RoundedCornerShape(24.dp),
              color = SoftPurplePillBg,
              border = BorderStroke(
                1.dp,
                if (canGoPrevious) BrandPurple.copy(alpha = 0.45f) else SoftPurpleBorder
              ),
              modifier = Modifier
                .width(128.dp)
                .height(48.dp)
                .testTag("story_previous_button")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                  contentDescription = null,
                  tint = if (canGoPrevious) BrandPurple else BrandPurple.copy(alpha = 0.45f),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = previousLabel,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (canGoPrevious) BrandPurple else BrandPurple.copy(alpha = 0.45f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            // Center page indicator: 1 / 2
            Text(
              text = "${currentPageIndex + 1} / $totalPages",
              fontSize = 16.sp,
              fontWeight = FontWeight.ExtraBold,
              color = PrimaryText
            )

            // Next > pill button (next page, or next lesson when on last page)
            Button(
              onClick = {
                if (currentPageIndex < totalPages - 1) {
                  currentPageIndex++
                } else if (nextLessonForStory != null) {
                  selectedLessonIds = setOf(nextLessonForStory.id)
                }
              },
              enabled = canGoNext,
              shape = RoundedCornerShape(24.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BrandPurple,
                contentColor = Color.White,
                disabledContainerColor = BrandPurple.copy(alpha = 0.45f),
                disabledContentColor = Color.White
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
              modifier = Modifier
                .width(128.dp)
                .height(48.dp)
                .testTag("story_next_button")
            ) {
              Text(
                text = nextLabel,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
            }
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
      // Top Bar with Back Arrow + Two Buttons: [ Story Reading ] and [ Sentence ] + Page Counter "1 / 2"
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { isReadingStarted = false },
          modifier = Modifier.testTag("reading_active_back_button")
        ) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Lesson Selection",
            tint = PrimaryText
          )
        }

        // Two mode buttons: [ Story Reading ] and [ Sentence ]
        Row(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val isStorySelected = readingMode == "Story"
          Surface(
            onClick = { readingMode = "Story" },
            shape = RoundedCornerShape(12.dp),
            color = if (isStorySelected) BrandPurple else CardSurface,
            border = BorderStroke(
              1.2.dp,
              if (isStorySelected) BrandPurple else BorderOutline
            ),
            modifier = Modifier.testTag("tab_story_reading")
          ) {
            Text(
              text = "Story Reading",
              fontSize = 14.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (isStorySelected) Color.White else PrimaryText,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
          }

          val isSentenceSelected = readingMode == "Sentence"
          Surface(
            onClick = { readingMode = "Sentence" },
            shape = RoundedCornerShape(12.dp),
            color = if (isSentenceSelected) BrandPurple else CardSurface,
            border = BorderStroke(
              1.2.dp,
              if (isSentenceSelected) BrandPurple else BorderOutline
            ),
            modifier = Modifier.testTag("tab_sentence_reading")
          ) {
            Text(
              text = "Sentence",
              fontSize = 14.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (isSentenceSelected) Color.White else PrimaryText,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
          }
        }

        if (readingMode == "Story" && currentStory != null) {
          Text(
            text = "${currentPageIndex + 1} / $totalPages",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            modifier = Modifier.padding(end = 6.dp)
          )
        }
      }

      if (readingMode == "Story") {
        if (currentStory != null && currentPage != null) {
          // STORY READING VIEW
          val storyScrollState = rememberScrollState()
          LaunchedEffect(currentStory.id, currentPageIndex) {
            storyScrollState.scrollTo(0)
          }

          val allLessonsForLookup = KanjiRepository.allLessons
          val storyKanjiIndex = remember(currentStory.id, allLessonsForLookup) {
            buildStoryKanjiIndex(allLessonsForLookup, currentStory)
          }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(storyScrollState)
              .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // 1. Main Story Card
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("story_reading_card"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = CardSurface),
              border = BorderStroke(1.dp, SoftPurpleBorder)
            ) {
              Column(modifier = Modifier.fillMaxWidth()) {
                // Lavender Title Strip at top of card: 📖 プラニーさんのはなし
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandPurpleVeryLight)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = currentStory.title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText
                  )
                }

                HorizontalDivider(color = SoftPurpleBorder, thickness = 1.dp)

                // Story Content Area
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                  val paragraphsToShow = if (currentPageIndex == 0 && !showTranslation) {
                    currentStory.fullPlainParagraphs
                  } else {
                    currentPage.plainParagraphs
                  }

                  paragraphsToShow.forEachIndexed { paragraphIndex, paragraph ->
                    InteractiveStoryParagraph(
                      text = paragraph,
                      kanjiIndex = storyKanjiIndex,
                      isKanjiPopupEnabled = isKanjiPopupEnabled,
                      selectedCharIndex = if (selectedParagraphIndex == paragraphIndex) selectedCharIndex else null,
                      onKanjiTap = { charIndex, lookup ->
                        selectedParagraphIndex = paragraphIndex
                        selectedCharIndex = charIndex
                        selectedKanjiLookup = lookup
                      }
                    )
                  }

                  Spacer(modifier = Modifier.height(2.dp))

                  // Bottom action row: [ 文A えいごの いみを みる ] + [ 👁 Eye Toggle ]
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Surface(
                      onClick = { showTranslation = !showTranslation },
                      modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("toggle_translation_button"),
                      shape = RoundedCornerShape(24.dp),
                      color = if (showTranslation) BrandPurple else CardSurface,
                      border = BorderStroke(
                        1.2.dp,
                        if (showTranslation) BrandPurple else BrandPurple.copy(alpha = 0.65f)
                      )
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxSize()
                          .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        if (showTranslation) {
                          Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                          )
                        } else {
                          Text(
                            text = "文A",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandPurple
                          )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = if (showTranslation) "えいごの いみ" else "えいごの いみを みる",
                          fontSize = 13.5.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (showTranslation) Color.White else BrandPurple,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }
                    }

                    // Eye Toggle Button: turns tap-to-inspect Kanji popup ON / OFF
                    Surface(
                      onClick = {
                        isKanjiPopupEnabled = !isKanjiPopupEnabled
                        if (!isKanjiPopupEnabled) {
                          selectedKanjiLookup = null
                          selectedParagraphIndex = null
                          selectedCharIndex = null
                        }
                      },
                      modifier = Modifier
                        .size(48.dp)
                        .testTag("story_kanji_popup_eye_toggle"),
                      shape = CircleShape,
                      color = if (isKanjiPopupEnabled) BrandPurpleVeryLight else CardSurface,
                      border = BorderStroke(
                        1.2.dp,
                        if (isKanjiPopupEnabled) BrandPurple.copy(alpha = 0.65f) else BorderOutline
                      )
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = if (isKanjiPopupEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                          contentDescription = if (isKanjiPopupEnabled) {
                            "Turn Kanji popup off"
                          } else {
                            "Turn Kanji popup on"
                          },
                          tint = if (isKanjiPopupEnabled) BrandPurple else SecondaryText,
                          modifier = Modifier.size(22.dp)
                        )
                      }
                    }
                  }

                  // English Translation Box (shown when showTranslation == true)
                  if (showTranslation) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BrandPurpleLight)
                        .padding(16.dp)
                    ) {
                      Text(
                        text = currentPage.englishLines.joinToString("\n"),
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryText
                      )
                    }
                  }
                }
              }
            }

            // Compact Kanji Inspector Bottom Sheet
            val activeLookup = selectedKanjiLookup
            if (isKanjiPopupEnabled && activeLookup != null) {
              StoryKanjiBottomSheet(
                lookupResult = activeLookup,
                onDismiss = {
                  selectedKanjiLookup = null
                  selectedParagraphIndex = null
                  selectedCharIndex = null
                }
              )
            }

            // 2. Collapsible Story Vocabulary Card: 📖 このはなしの たんご (Vocabulary)
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("story_vocabulary_card"),
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = CardSurface),
              border = BorderStroke(1.dp, SoftPurpleBorder)
            ) {
              Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isVocabExpanded = !isVocabExpanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "このはなしの たんご (Vocabulary)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    modifier = Modifier.weight(1f)
                  )
                  Icon(
                    imageVector = if (isVocabExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isVocabExpanded) "Collapse Vocabulary" else "Expand Vocabulary",
                    tint = PrimaryText,
                    modifier = Modifier.size(22.dp)
                  )
                }

                if (isVocabExpanded) {
                  HorizontalDivider(color = BorderOutline, thickness = 1.dp)

                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 16.dp, vertical = 6.dp)
                  ) {
                    currentStory.vocabulary.forEachIndexed { idx, vocab ->
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = vocab.wordWithReading,
                          fontSize = 14.5.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = PrimaryText,
                          modifier = Modifier.weight(1.15f)
                        )

                        Text(
                          text = vocab.meaning,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Medium,
                          color = SecondaryText,
                          modifier = Modifier.weight(1f)
                        )

                        Box(
                          modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SoftPurplePillBg)
                            .border(1.dp, SoftPurpleBorder, CircleShape)
                            .clickable { onPlayAudio(vocab.audioText) },
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen ${vocab.wordWithReading}",
                            tint = BrandPurple,
                            modifier = Modifier.size(18.dp)
                          )
                        }
                      }

                      if (idx < currentStory.vocabulary.lastIndex) {
                        HorizontalDivider(color = BorderOutline, thickness = 0.8.dp)
                      }
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
          }
        } else {
          // Empty state when selected lesson(s) do not have a story yet
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("story_empty_state_card"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = CardSurface),
              border = BorderStroke(1.dp, SoftPurpleBorder)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BrandPurpleVeryLight),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = BrandPurple,
                    modifier = Modifier.size(28.dp)
                  )
                }

                Text(
                  text = "No story added for this lesson yet",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryText,
                  textAlign = TextAlign.Center
                )

                val firstAvailableStory = storiesInLevel.firstOrNull()
                Text(
                  text = if (firstAvailableStory != null) {
                    "Lesson ${firstAvailableStory.lessonNumber} (${firstAvailableStory.title}) is available to read."
                  } else {
                    "Story reading material will be added here soon."
                  },
                  fontSize = 14.sp,
                  color = SecondaryText,
                  textAlign = TextAlign.Center
                )

                if (firstAvailableStory != null) {
                  val targetLesson = readingLessons.firstOrNull { it.lessonNumber == firstAvailableStory.lessonNumber }
                  if (targetLesson != null) {
                    Button(
                      onClick = { selectedLessonIds = setOf(targetLesson.id) },
                      shape = RoundedCornerShape(22.dp),
                      colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPurple,
                        contentColor = Color.White
                      ),
                      modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("open_lesson_story_button")
                    ) {
                      Text(
                        text = "Open ${targetLesson.title} Story",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                      )
                    }
                  }
                }
              }
            }
          }
        }
      } else {
        // SENTENCE READING VIEW (empty until user provides book sentence material)
        if (exercises.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("sentence_empty_state_card"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = CardSurface),
              border = BorderStroke(1.dp, SoftPurpleBorder)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BrandPurpleVeryLight),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = BrandPurple,
                    modifier = Modifier.size(28.dp)
                  )
                }

                Text(
                  text = "No sentences added yet",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryText,
                  textAlign = TextAlign.Center
                )

                Text(
                  text = "Sentence reading material will appear here once added.",
                  fontSize = 14.sp,
                  color = SecondaryText,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        } else {
          // SENTENCE READING VIEW — unified clean Story-style card with interactive Kanji popup, Eye toggle, and all translations grouped below
          val sentenceScrollState = rememberScrollState()
          LaunchedEffect(selectedLessonIds) {
            sentenceScrollState.scrollTo(0)
          }

          val allLessonsForSentenceLookup = KanjiRepository.allLessons
          val sentenceKanjiIndex = remember(selectedLessonIds, allLessonsForSentenceLookup) {
            buildStoryKanjiIndex(allLessonsForSentenceLookup, story = null)
          }

          val distinctLessonNumbers = remember(exercises) {
            exercises.map { it.lessonNumber }.distinct().sorted()
          }
          val sentenceHeaderTitle = when {
            distinctLessonNumbers.size == 1 -> "Lesson ${distinctLessonNumbers.first()} · Sentences"
            distinctLessonNumbers.size <= 3 -> "Lessons ${distinctLessonNumbers.joinToString(", ")} · Sentences"
            else -> "$selectedLevel Reading Sentences"
          }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(sentenceScrollState)
              .navigationBarsPadding()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("sentence_reading_card"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = CardSurface),
              border = BorderStroke(1.dp, SoftPurpleBorder)
            ) {
              Column(modifier = Modifier.fillMaxWidth()) {
                // Lavender Title Strip matching Story Reading card
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandPurpleVeryLight)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = sentenceHeaderTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandPurpleLight
                  ) {
                    Text(
                      text = "${exercises.size}",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = BrandPurple,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                  }
                }

                HorizontalDivider(color = SoftPurpleBorder, thickness = 1.dp)

                // Sentences Content Area
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                  verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                  exercises.forEachIndexed { idx, exercise ->
                    if (distinctLessonNumbers.size > 1 && (idx == 0 || exercises[idx - 1].lessonNumber != exercise.lessonNumber)) {
                      if (idx > 0) {
                        HorizontalDivider(
                          color = BorderOutline,
                          thickness = 0.8.dp,
                          modifier = Modifier.padding(vertical = 2.dp)
                        )
                      }
                      Text(
                        text = "Lesson ${exercise.lessonNumber}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple
                      )
                    }

                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reading_sentence_row_${exercise.id}"),
                      verticalAlignment = Alignment.Top
                    ) {
                      Text(
                        text = "${idx + 1}.",
                        fontSize = 15.5.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple,
                        modifier = Modifier.width(28.dp)
                      )
                      InteractiveStoryParagraph(
                        text = exercise.sentenceWithKanji,
                        kanjiIndex = sentenceKanjiIndex,
                        isKanjiPopupEnabled = isKanjiPopupEnabled,
                        selectedCharIndex = if (selectedParagraphIndex == idx) selectedCharIndex else null,
                        onKanjiTap = { charIndex, lookup ->
                          selectedParagraphIndex = idx
                          selectedCharIndex = charIndex
                          selectedKanjiLookup = lookup
                        },
                        modifier = Modifier.weight(1f)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(2.dp))

                  // Bottom Action Row matching Story Reading: [ 文A えいごの いみを みる ] + [ 👁 Eye Toggle ]
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Surface(
                      onClick = { showSentenceTranslation = !showSentenceTranslation },
                      modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("toggle_sentence_translation_button"),
                      shape = RoundedCornerShape(24.dp),
                      color = if (showSentenceTranslation) BrandPurple else CardSurface,
                      border = BorderStroke(
                        1.2.dp,
                        if (showSentenceTranslation) BrandPurple else BrandPurple.copy(alpha = 0.65f)
                      )
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxSize()
                          .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        if (showSentenceTranslation) {
                          Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                          )
                        } else {
                          Text(
                            text = "文A",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandPurple
                          )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = if (showSentenceTranslation) "えいごの いみ" else "えいごの いみを みる",
                          fontSize = 13.5.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (showSentenceTranslation) Color.White else BrandPurple,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }
                    }

                    // Eye Toggle Button: turns tap-to-inspect Kanji popup ON / OFF
                    Surface(
                      onClick = {
                        isKanjiPopupEnabled = !isKanjiPopupEnabled
                        if (!isKanjiPopupEnabled) {
                          selectedKanjiLookup = null
                          selectedParagraphIndex = null
                          selectedCharIndex = null
                        }
                      },
                      modifier = Modifier
                        .size(48.dp)
                        .testTag("sentence_kanji_popup_eye_toggle"),
                      shape = CircleShape,
                      color = if (isKanjiPopupEnabled) BrandPurpleVeryLight else CardSurface,
                      border = BorderStroke(
                        1.2.dp,
                        if (isKanjiPopupEnabled) BrandPurple.copy(alpha = 0.65f) else BorderOutline
                      )
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = if (isKanjiPopupEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                          contentDescription = if (isKanjiPopupEnabled) {
                            "Turn Kanji popup off"
                          } else {
                            "Turn Kanji popup on"
                          },
                          tint = if (isKanjiPopupEnabled) BrandPurple else SecondaryText,
                          modifier = Modifier.size(22.dp)
                        )
                      }
                    }
                  }

                  // All English Translations Together Down Below (shown when showSentenceTranslation == true)
                  if (showSentenceTranslation) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BrandPurpleLight)
                        .padding(16.dp)
                        .testTag("sentence_translations_box"),
                      verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      exercises.forEachIndexed { idx, exercise ->
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          verticalAlignment = Alignment.Top
                        ) {
                          Text(
                            text = "${idx + 1}.",
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPurple,
                            modifier = Modifier.width(24.dp)
                          )
                          Text(
                            text = exercise.translation,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryText,
                            modifier = Modifier.weight(1f)
                          )
                        }
                      }
                    }
                  }
                }
              }
            }

            // Previous / Next Lesson Navigation below the Sentence card
            val currentLessonNumber = if (distinctLessonNumbers.size == 1) distinctLessonNumbers.first() else null
            val lessonsWithReading = remember(readingLessons, storiesInLevel, exercisesInLevel) {
              readingLessons.filter { lesson ->
                storiesInLevel.any { it.lessonNumber == lesson.lessonNumber } ||
                  exercisesInLevel.any { it.lessonNumber == lesson.lessonNumber }
              }
            }
            val currentReadingPos = if (currentLessonNumber != null) {
              lessonsWithReading.indexOfFirst { it.lessonNumber == currentLessonNumber }
            } else {
              -1
            }
            val previousReadingLesson = if (currentReadingPos > 0) {
              lessonsWithReading[currentReadingPos - 1]
            } else {
              null
            }
            val nextReadingLesson = if (currentReadingPos in 0 until lessonsWithReading.lastIndex) {
              lessonsWithReading[currentReadingPos + 1]
            } else {
              null
            }

            if (previousReadingLesson != null || nextReadingLesson != null) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (previousReadingLesson != null) {
                  Surface(
                    onClick = { selectedLessonIds = setOf(previousReadingLesson.id) },
                    shape = RoundedCornerShape(26.dp),
                    color = SoftPurplePillBg,
                    border = BorderStroke(1.dp, SoftPurpleBorder),
                    modifier = Modifier
                      .weight(if (nextReadingLesson != null) 0.9f else 1f)
                      .height(50.dp)
                      .testTag("sentence_previous_lesson_button")
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                      horizontalArrangement = Arrangement.Center,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(20.dp)
                      )
                      Spacer(modifier = Modifier.width(2.dp))
                      Text(
                        text = if (nextReadingLesson != null) {
                          previousReadingLesson.title
                        } else {
                          "Previous · ${previousReadingLesson.title}"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                }

                if (nextReadingLesson != null) {
                  Button(
                    onClick = { selectedLessonIds = setOf(nextReadingLesson.id) },
                    modifier = Modifier
                      .weight(if (previousReadingLesson != null) 1.35f else 1f)
                      .height(50.dp)
                      .testTag("sentence_next_lesson_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = BrandPurple,
                      contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                  ) {
                    Text(
                      text = "Next Lesson · ${nextReadingLesson.title}",
                      fontSize = 14.5.sp,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                      contentDescription = null,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }

            // Compact Kanji Inspector Bottom Sheet
            val activeLookup = selectedKanjiLookup
            if (isKanjiPopupEnabled && activeLookup != null) {
              StoryKanjiBottomSheet(
                lookupResult = activeLookup,
                onDismiss = {
                  selectedKanjiLookup = null
                  selectedParagraphIndex = null
                  selectedCharIndex = null
                }
              )
            }

            Spacer(modifier = Modifier.height(16.dp))
          }
        }
      }
    }
  }
}

@Composable
fun KanjiQuizScreen(
  selectedLevel: String,
  lessonsForLevel: List<KanjiLesson>,
  onBack: () -> Unit,
  onPlayAudio: (String) -> Unit,
  onQuizCompleted: (Int, Set<Int>) -> Unit,
  modifier: Modifier = Modifier
) {
  // Step 1: Lesson Selection state (user can pick 1 lesson, multiple lessons, or All Lessons)
  var selectedLessonIds by remember(selectedLevel, lessonsForLevel) {
    mutableStateOf(lessonsForLevel.map { it.id }.toSet())
  }
  var isQuizStarted by remember { mutableStateOf(false) }

  // Quiz session state
  var quizSessionKey by remember { mutableIntStateOf(0) }
  var currentQuestionIndex by remember { mutableIntStateOf(0) }
  var isFinished by remember { mutableStateOf(false) }

  // Per-question state tracking (supports both MCQ mode and "Options Hidden" recall mode + Prev/Next)
  val selectedOptionsMap = remember(quizSessionKey) { mutableStateMapOf<Int, String>() }
  val meaningRevealedMap = remember(quizSessionKey) { mutableStateMapOf<Int, Boolean>() }
  // questionResultMap: true = correct, false = wrong
  val questionResultMap = remember(quizSessionKey) { mutableStateMapOf<Int, Boolean>() }

  // "Hide Options" / "Options Hidden" mode & Recall Timer (3s / 5s) matching Vocab Start Drill
  var hideMcqOptions by remember { mutableStateOf(false) }
  var timerSeconds by remember { mutableStateOf<Int?>(null) }
  var timerRemainingSeconds by remember { mutableIntStateOf(0) }
  var timerPaused by remember { mutableStateOf(false) }

  fun resetQuizSession() {
    quizSessionKey++
    currentQuestionIndex = 0
    isFinished = false
    timerPaused = false
  }

  BackHandler {
    if (isQuizStarted) {
      isQuizStarted = false
      isFinished = false
      currentQuestionIndex = 0
    } else {
      onBack()
    }
  }

  val allLessonIds = remember(lessonsForLevel) { lessonsForLevel.map { it.id }.toSet() }
  val isAllSelected = selectedLessonIds.size == allLessonIds.size && allLessonIds.isNotEmpty()

  val selectedLessons = remember(lessonsForLevel, selectedLessonIds) {
    lessonsForLevel.filter { selectedLessonIds.contains(it.id) }
  }

  val selectedKanjiPool = remember(selectedLessons) {
    selectedLessons.flatMap { it.kanjiList }
  }

  // Step 1 UI: Lesson Selector Screen before starting the quiz
  if (!isQuizStarted) {
    Scaffold(
      modifier = modifier
        .fillMaxSize()
        .background(MainBackground),
      containerColor = MainBackground,
      bottomBar = {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
          color = CardSurface,
          shadowElevation = 8.dp,
          border = BorderStroke(1.dp, BorderOutline)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp)
          ) {
            Button(
              onClick = {
                if (selectedKanjiPool.isNotEmpty()) {
                  resetQuizSession()
                  isQuizStarted = true
                }
              },
              enabled = selectedKanjiPool.isNotEmpty(),
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("start_selected_lessons_quiz_button"),
              shape = RoundedCornerShape(25.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BrandPurple,
                contentColor = Color.White,
                disabledContainerColor = BorderOutline,
                disabledContentColor = SecondaryText
              )
            ) {
              val buttonLabel = when {
                selectedLessons.isEmpty() -> "Select at least 1 lesson"
                isAllSelected -> "Start Quiz · All Lessons (${selectedKanjiPool.size} Kanji)"
                selectedLessons.size == 1 -> "Start ${selectedLessons.first().title} Quiz (${selectedKanjiPool.size} Kanji)"
                else -> "Start Quiz · ${selectedLessons.size} Lessons (${selectedKanjiPool.size} Kanji)"
              }
              Text(
                text = buttonLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp
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
        // Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("quiz_back_button")
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = PrimaryText
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "$selectedLevel Kanji Quiz",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = PrimaryText
            )
            Text(
              text = "Select lessons for your quiz or choose All Lessons",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = SecondaryText
            )
          }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp)
        ) {
          // 1. "All Lessons" Selection Card
          item {
            val totalKanjiInLevel = lessonsForLevel.sumOf { it.kanjiList.size }
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedLessonIds = if (isAllSelected) {
                    emptySet()
                  } else {
                    allLessonIds
                  }
                }
                .testTag("quiz_select_all_lessons_card"),
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isAllSelected) BrandPurpleVeryLight else CardSurface
              ),
              border = BorderStroke(
                width = if (isAllSelected) 1.6.dp else 1.dp,
                color = if (isAllSelected) BrandPurple else BorderOutline
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isAllSelected) BrandPurple else CardSurface)
                    .border(
                      1.5.dp,
                      if (isAllSelected) BrandPurple else Color(0xFFC5C6D0),
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isAllSelected) {
                    Icon(
                      Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "All Lessons (${lessonsForLevel.size} Lessons)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "Mix all $totalKanjiInLevel Kanji from $selectedLevel",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                  )
                }

                OutlinedButton(
                  onClick = {
                    selectedLessonIds = allLessonIds
                    resetQuizSession()
                    isQuizStarted = true
                  },
                  modifier = Modifier.heightIn(min = 38.dp),
                  border = BorderStroke(1.dp, BrandPurple),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isAllSelected) BrandPurple else CardSurface,
                    contentColor = if (isAllSelected) Color.White else BrandPurple
                  )
                ) {
                  Text(
                    text = "Start All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }

          // Section Header: Individual Lessons
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Select Lessons (${selectedLessons.size}/${lessonsForLevel.size} selected)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
              )

              Text(
                text = if (selectedLessonIds.isNotEmpty()) "Clear" else "Select All",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPurple,
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .clickable {
                    selectedLessonIds = if (selectedLessonIds.isNotEmpty()) emptySet() else allLessonIds
                  }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          // 2. Lesson Cards
          items(lessonsForLevel, key = { it.id }) { lesson ->
            val isLessonSelected = selectedLessonIds.contains(lesson.id)
            val kanjiPreview = lesson.kanjiList.joinToString(" ") { it.character }

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedLessonIds = if (isLessonSelected) {
                    selectedLessonIds - lesson.id
                  } else {
                    selectedLessonIds + lesson.id
                  }
                }
                .testTag("quiz_lesson_card_${lesson.id}"),
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isLessonSelected) BrandPurpleVeryLight else CardSurface
              ),
              border = BorderStroke(
                width = if (isLessonSelected) 1.5.dp else 1.dp,
                color = if (isLessonSelected) BrandPurple else BorderOutline
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isLessonSelected) BrandPurple else CardSurface)
                    .border(
                      1.5.dp,
                      if (isLessonSelected) BrandPurple else SecondaryText.copy(alpha = 0.5f),
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isLessonSelected) {
                    Icon(
                      Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = lesson.title,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = PrimaryText
                    )
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = BrandPurpleLight
                    ) {
                      Text(
                        text = "${lesson.kanjiList.size} kanji",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  Text(
                    text = kanjiPreview,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                  onClick = {
                    selectedLessonIds = setOf(lesson.id)
                    resetQuizSession()
                    isQuizStarted = true
                  },
                  modifier = Modifier.heightIn(min = 38.dp),
                  border = BorderStroke(1.dp, BorderOutline),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardSurface,
                    contentColor = BrandPurple
                  )
                ) {
                  Text(
                    text = "Quiz",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }
      }
    }
    return
  }

  // Step 2 UI: Active Quiz for the selected lesson(s)
  val questions = remember(quizSessionKey, selectedKanjiPool) {
    val pool = if (selectedKanjiPool.isNotEmpty()) selectedKanjiPool else KanjiRepository.allKanjiAcrossLevels
    pool.shuffled()
  }

  val currentKanji = questions.getOrElse(currentQuestionIndex) { questions.first() }
  val selectedOption = selectedOptionsMap[currentQuestionIndex]
  val isMeaningRevealed = meaningRevealedMap[currentQuestionIndex] == true
  val isQuestionAnswered = questionResultMap.containsKey(currentQuestionIndex)
  val isAnsweredWrong = selectedOption != null && selectedOption != currentKanji.meaning

  val correctCount = questionResultMap.values.count { it }
  val wrongCount = questionResultMap.values.count { !it }

  val options = remember(quizSessionKey, currentQuestionIndex, questions) {
    val levelPoolMeanings = lessonsForLevel
      .flatMap { it.kanjiList }
      .map { it.meaning }
      .distinct()
      .filter { it != currentKanji.meaning }
      .shuffled()
    val fallbackMeanings = KanjiRepository.allKanjiAcrossLevels
      .map { it.meaning }
      .distinct()
      .filter { it != currentKanji.meaning && !levelPoolMeanings.contains(it) }
      .shuffled()
    val distractors = (levelPoolMeanings + fallbackMeanings).take(3)
    (distractors + currentKanji.meaning).shuffled()
  }

  fun advanceToNextOrFinish(countUnansweredRecallAsCorrect: Boolean) {
    if (countUnansweredRecallAsCorrect && hideMcqOptions && !questionResultMap.containsKey(currentQuestionIndex)) {
      questionResultMap[currentQuestionIndex] = !isMeaningRevealed
    }
    if (currentQuestionIndex < questions.size - 1) {
      currentQuestionIndex++
    } else {
      val finalCorrect = questionResultMap.values.count { it }
      val scorePercent = ((finalCorrect.toFloat() / questions.size.toFloat()) * 100).toInt()
      onQuizCompleted(scorePercent, questions.map { it.number }.toSet())
      isFinished = true
    }
  }

  // Auto-advance directly to the next question when the user selects the CORRECT option in MCQ mode
  LaunchedEffect(selectedOption, currentQuestionIndex, quizSessionKey, hideMcqOptions) {
    if (hideMcqOptions) return@LaunchedEffect
    val chosen = selectedOption ?: return@LaunchedEffect
    if (chosen == currentKanji.meaning) {
      delay(300)
      advanceToNextOrFinish(countUnansweredRecallAsCorrect = false)
    }
  }

  // Recall Timer (3s or 5s) when Options Hidden is active
  LaunchedEffect(currentQuestionIndex, hideMcqOptions, timerSeconds, quizSessionKey, isFinished) {
    val secs = timerSeconds
    if (!hideMcqOptions || secs == null || isFinished) {
      timerRemainingSeconds = 0
      return@LaunchedEffect
    }
    timerRemainingSeconds = secs
    timerPaused = false
  }

  LaunchedEffect(
    currentQuestionIndex,
    hideMcqOptions,
    timerSeconds,
    timerPaused,
    isMeaningRevealed,
    isQuestionAnswered,
    isFinished
  ) {
    val secs = timerSeconds
    if (!hideMcqOptions || secs == null || timerPaused || isMeaningRevealed || isQuestionAnswered || isFinished) {
      return@LaunchedEffect
    }
    while (timerRemainingSeconds > 0) {
      delay(1000)
      if (timerPaused || isMeaningRevealed || isQuestionAnswered || !hideMcqOptions) return@LaunchedEffect
      timerRemainingSeconds--
      if (timerRemainingSeconds <= 0) {
        // Timer reached 0: auto-advance (counts as known unless meaning was checked)
        advanceToNextOrFinish(countUnansweredRecallAsCorrect = true)
        return@LaunchedEffect
      }
    }
  }

  val selectedLessonsSummary = remember(selectedLessons, isAllSelected) {
    when {
      isAllSelected -> "All Lessons"
      selectedLessons.size == 1 -> selectedLessons.first().title
      else -> selectedLessons.joinToString(", ") { "L${it.lessonNumber}" }
    }
  }

  val scrollState = rememberScrollState()
  LaunchedEffect(currentQuestionIndex) {
    scrollState.scrollTo(0)
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackground),
    containerColor = MainBackground,
    bottomBar = {
      if (!isFinished) {
        if (hideMcqOptions) {
          // Bottom Bar matching Vocab Start Drill screenshot: [ <- Prev ] and [ Next -> ]
          Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = CardSurface,
            border = BorderStroke(1.dp, BorderOutline)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = {
                  if (currentQuestionIndex > 0) {
                    currentQuestionIndex--
                  }
                },
                enabled = currentQuestionIndex > 0,
                modifier = Modifier
                  .heightIn(min = 48.dp)
                  .testTag("btn_previous_question"),
                border = BorderStroke(1.dp, BorderOutline),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = CardSurface,
                  contentColor = PrimaryText,
                  disabledContentColor = SecondaryText.copy(alpha = 0.45f)
                )
              ) {
                Text("← Prev", fontWeight = FontWeight.SemiBold)
              }

              Button(
                onClick = {
                  advanceToNextOrFinish(countUnansweredRecallAsCorrect = true)
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = BrandPurple,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .heightIn(min = 48.dp)
                  .testTag("btn_next_question")
              ) {
                Text(
                  text = if (currentQuestionIndex == questions.lastIndex) "Finish" else "Next →",
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        } else if (isAnsweredWrong) {
          // In MCQ mode, only show Next Question button when user answered WRONG
          Surface(
            color = MainBackground,
            modifier = Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
              .padding(horizontal = 16.dp, vertical = 8.dp)
          ) {
            Button(
              onClick = {
                advanceToNextOrFinish(countUnansweredRecallAsCorrect = false)
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("quiz_next_question_button"),
              shape = RoundedCornerShape(25.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BrandPurple,
                contentColor = Color.White
              )
            ) {
              Text(
                text = if (currentQuestionIndex < questions.size - 1) "Next Question" else "Finish Quiz",
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp
              )
            }
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
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. Top Bar: <- Lessons | CHAPTER / LEVEL | Lessons button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            isQuizStarted = false
            isFinished = false
            currentQuestionIndex = 0
          },
          modifier = Modifier.testTag("quiz_active_back_button")
        ) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Lesson Selection",
            tint = PrimaryText
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "$selectedLevel Quiz · $selectedLessonsSummary",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = if (isFinished) "Quiz Completed" else "Question ${currentQuestionIndex + 1} of ${questions.size}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandPurple
          )
        }

        OutlinedButton(
          onClick = {
            isQuizStarted = false
            isFinished = false
            currentQuestionIndex = 0
          },
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.dp, BorderOutline),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CardSurface,
            contentColor = BrandPurple
          )
        ) {
          Text(
            text = "Lessons",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      if (isFinished) {
        val scorePercent = ((correctCount.toFloat() / questions.size.toFloat()) * 100).toInt()
        Spacer(modifier = Modifier.height(12.dp))
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = CardSurface),
          border = BorderStroke(1.dp, BorderOutline)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.CheckCircle,
              contentDescription = null,
              tint = BrandPurple,
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Quiz Complete!",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = PrimaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$selectedLevel · $selectedLessonsSummary",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = SecondaryText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "You scored $correctCount / ${questions.size} ($scorePercent%)",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              color = BrandPurple
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = { resetQuizSession() },
                modifier = Modifier
                  .weight(1f)
                  .heightIn(min = 48.dp),
                border = BorderStroke(1.dp, BorderOutline),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = CardSurface,
                  contentColor = BrandPurple
                )
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry", fontWeight = FontWeight.SemiBold)
              }

              Button(
                onClick = {
                  isQuizStarted = false
                  isFinished = false
                  currentQuestionIndex = 0
                },
                modifier = Modifier
                  .weight(1f)
                  .heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = BrandPurple,
                  contentColor = Color.White
                )
              ) {
                Text("Change Lessons", fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      } else {
        // 2. Progress Bar
        LinearProgressIndicator(
          progress = { (currentQuestionIndex + 1).toFloat() / questions.size.toFloat() },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = BrandPurple,
          trackColor = BorderOutline,
          strokeCap = StrokeCap.Round
        )

        // 3. Subtitle Row: Lesson name on left, "1 of 30" on right (matching Vocab Start Drill)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = selectedLessonsSummary,
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText
          )
          Text(
            text = "${currentQuestionIndex + 1} of ${questions.size}",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText
          )
        }

        // 4. [ Hide Options / Options Hidden ] toggle button + Live Score (✓ 0   × 0)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          OutlinedButton(
            onClick = {
              hideMcqOptions = !hideMcqOptions
              if (!hideMcqOptions) {
                timerSeconds = null
                timerPaused = true
              }
            },
            modifier = Modifier.testTag("toggle_hide_options_button"),
            border = BorderStroke(1.dp, BorderOutline),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = CardSurface,
              contentColor = BrandPurple
            )
          ) {
            Icon(
              imageVector = if (hideMcqOptions) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = BrandPurple
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (hideMcqOptions) "Options Hidden" else "Hide Options",
              fontWeight = FontWeight.SemiBold
            )
          }

          Text(
            text = "✓ $correctCount   × $wrongCount",
            color = QuizGreen,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
          )
        }

        // 5. Timer selector row ("Timer": [ 3s ] [ 5s ]) when Options Hidden is active
        if (hideMcqOptions && timerSeconds == null) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Timer",
              style = MaterialTheme.typography.labelMedium,
              color = SecondaryText
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf(3, 5).forEach { seconds ->
                OutlinedButton(
                  onClick = {
                    timerSeconds = seconds
                    timerRemainingSeconds = seconds
                    timerPaused = false
                  },
                  modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("select_timer_$seconds"),
                  border = BorderStroke(1.dp, BorderOutline),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardSurface,
                    contentColor = BrandPurple
                  )
                ) {
                  Text(
                    text = "${seconds}s",
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }

        // 6. Main Kanji Prompt Card (matching screenshot 2 when Options Hidden, or clean card when MCQ)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("card_question_prompt"),
          shape = RoundedCornerShape(28.dp),
          colors = CardDefaults.cardColors(containerColor = CardSurface),
          border = BorderStroke(1.dp, BorderOutline)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = if (hideMcqOptions) 28.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            if (hideMcqOptions && timerSeconds != null) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                TextButton(
                  onClick = {
                    timerSeconds = null
                    timerPaused = true
                  }
                ) {
                  Text(
                    text = "Change timer (${timerSeconds}s)",
                    fontSize = 12.sp,
                    color = SecondaryText
                  )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = if (isMeaningRevealed || isQuestionAnswered) {
                      "Timer stopped"
                    } else {
                      "${timerRemainingSeconds.coerceAtLeast(1)}s"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandPurple,
                    modifier = Modifier.testTag("recall_countdown")
                  )
                  IconButton(
                    onClick = { timerPaused = !timerPaused },
                    enabled = !isMeaningRevealed && !isQuestionAnswered,
                    modifier = Modifier.testTag("toggle_timer_pause")
                  ) {
                    Icon(
                      imageVector = if (timerPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                      contentDescription = if (timerPaused) "Resume timer" else "Pause timer",
                      tint = PrimaryText
                    )
                  }
                }
              }
            }

            Text(
              text = "What does this Japanese word mean?",
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center,
              color = SecondaryText
            )

            if (hideMcqOptions) {
              Text(
                text = currentKanji.character,
                fontSize = 58.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("text_prompt_main")
              )
            } else {
              Box(
                modifier = Modifier
                  .size(104.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .background(BrandPurpleVeryLight)
                  .border(1.5.dp, SoftPurpleBorder, RoundedCornerShape(20.dp))
                  .clickable { onPlayAudio(currentKanji.character) },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = currentKanji.character,
                  fontSize = 54.sp,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryText
                )
              }
            }

            val readings = (currentKanji.kunReadings + currentKanji.onReadingsDisplay).joinToString(" · ")
            Text(
              text = if (hideMcqOptions) "[$readings]" else readings,
              fontSize = 15.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = BrandPurple,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("text_prompt_subtext")
            )

            TextButton(
              onClick = { onPlayAudio(currentKanji.character) }
            ) {
              Icon(
                Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Listen Pronunciation",
                color = BrandPurple,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        // 7. Either Recall "Check Meaning" Card (when Options Hidden) OR Multiple-Choice Options
        if (hideMcqOptions) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BorderOutline)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Text(
                text = "Know it? Tap Next. Check Meaning counts as wrong.",
                textAlign = TextAlign.Center,
                color = PrimaryText,
                style = MaterialTheme.typography.bodyMedium
              )

              if (isMeaningRevealed) {
                Text(
                  text = currentKanji.meaning,
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryText,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag("text_revealed_meaning")
                )
                Text(
                  text = "Added to mistakes",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFFEF4444)
                )
              } else {
                Button(
                  onClick = {
                    meaningRevealedMap[currentQuestionIndex] = true
                    questionResultMap[currentQuestionIndex] = false
                    timerPaused = true
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = QuizOrange,
                    contentColor = Color.White
                  ),
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier
                    .heightIn(min = 46.dp)
                    .testTag("btn_check_meaning")
                ) {
                  Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Check Meaning",
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        } else {
          // Answer Options (Multiple Choice)
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val darkMode = isKanjiDarkTheme
            options.forEach { option ->
              val isSelected = selectedOption == option
              val isCorrect = option == currentKanji.meaning
              val containerColor = when {
                selectedOption == null -> CardSurface
                isCorrect -> if (darkMode) Color(0xFF064E3B) else Color(0xFFECFDF5)
                isSelected -> if (darkMode) Color(0xFF7F1D1D) else Color(0xFFFEF2F2)
                else -> CardSurface
              }
              val borderColor = when {
                selectedOption == null -> BorderOutline
                isCorrect -> Color(0xFF10B981)
                isSelected -> Color(0xFFEF4444)
                else -> BorderOutline
              }

              Surface(
                onClick = {
                  if (selectedOption == null) {
                    selectedOptionsMap[currentQuestionIndex] = option
                    questionResultMap[currentQuestionIndex] = (option == currentKanji.meaning)
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 54.dp),
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
                border = BorderStroke(1.2.dp, borderColor)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = option,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                    modifier = Modifier.weight(1f)
                  )

                  if (selectedOption != null) {
                    if (isCorrect) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAnsweredWrong) {
                          Text(
                            text = "Correct",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                        }
                        Icon(
                          Icons.Default.Check,
                          contentDescription = "Correct",
                          tint = Color(0xFF10B981),
                          modifier = Modifier.size(20.dp)
                        )
                      }
                    } else if (isSelected) {
                      Icon(
                        Icons.Default.Close,
                        contentDescription = "Wrong",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
