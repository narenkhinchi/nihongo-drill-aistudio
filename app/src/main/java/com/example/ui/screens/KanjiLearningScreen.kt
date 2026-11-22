package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Kanji
import com.example.data.Vocabulary
import com.example.ui.components.PracticeWritingBox
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
import com.example.ui.viewmodel.KanjiViewModel
import kotlinx.coroutines.launch

/**
 * Drop-in entry point matching `com.nihongo.vocabdrill.kanji.ui.KanjiModuleScreen`
 * in the Nihongo Drill Android repository (`MainScreen.kt` -> `showKanjiModule`).
 * Replaces the legacy `KanjiDashboardScreen`, `KanjiLessonScreen`, and `KanjiDetailScreen`.
 */
@Composable
fun KanjiModuleScreen(
  modifier: Modifier = Modifier,
  onBackToVocabulary: (() -> Unit)? = null,
  isDarkTheme: Boolean = isKanjiDarkTheme,
  onToggleTheme: (() -> Unit)? = null,
  viewModel: KanjiViewModel = viewModel()
) {
  KanjiLearningScreen(
    modifier = modifier,
    onBackToVocabulary = onBackToVocabulary,
    isDarkTheme = isDarkTheme,
    onToggleTheme = onToggleTheme,
    viewModel = viewModel
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiLearningScreen(
  modifier: Modifier = Modifier,
  onBackToVocabulary: (() -> Unit)? = null,
  isDarkTheme: Boolean = isKanjiDarkTheme,
  onToggleTheme: (() -> Unit)? = null,
  viewModel: KanjiViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  var showOverflowMenu by remember { mutableStateOf(false) }
  var showLeaveLessonDialog by remember { mutableStateOf(false) }

  // 1. Main Home Screen (All Lessons across N5-N1, Your Progress, Preview, and Search Kanji)
  if (uiState.isHomeScreenOpen) {
    KanjiHomeScreen(
      uiState = uiState,
      onSelectLevel = { level -> viewModel.selectLevel(level) },
      onSelectFilter = { filter -> viewModel.selectFilter(filter) },
      onStartLesson = { lesson -> viewModel.startLesson(lesson) },
      onPreviewLessonKanji = { lesson -> viewModel.openLessonPreview(lesson) },
      onClosePreviewLesson = { viewModel.closeLessonPreview() },
      onSelectKanjiInLesson = { lesson, index -> viewModel.startLesson(lesson, index) },
      onOpenSearch = { viewModel.setSearchSheetOpen(true) },
      onCloseSearch = { viewModel.setSearchSheetOpen(false) },
      onUpdateSearchQuery = { query -> viewModel.updateSearchQuery(query) },
      onSelectKanjiFromSearch = { kanji, openWriting -> viewModel.selectKanjiFromSearch(kanji, openWriting) },
      onPlayAudio = { text -> viewModel.playAudio(text) },
      onToggleBookmark = { kanjiNumber -> viewModel.toggleBookmarkForKanji(kanjiNumber) },
      onToggleHideOtherLessons = { viewModel.toggleHideOtherLessons() },
      onOpenReading = { viewModel.openReadingScreen() },
      onStartQuiz = { viewModel.openQuizScreen() },
      onBackToVocabulary = onBackToVocabulary,
      isDarkTheme = isDarkTheme,
      onToggleTheme = onToggleTheme,
      onSyncFromServer = { viewModel.syncFromServer(userTriggered = true) },
      onClearSyncMessage = { viewModel.clearSyncMessage() }
    )
    return
  }

  // 1b. Kanji Reading Screen (Lesson Selection + Reading flow)
  if (uiState.isReadingScreenOpen) {
    KanjiReadingScreen(
      selectedLevel = uiState.selectedLevel,
      lessonsForLevel = uiState.lessonsForSelectedLevel,
      onBack = { viewModel.closeReadingScreen() },
      onPlayAudio = { text -> viewModel.playAudio(text) }
    )
    return
  }

  // 1c. Kanji Quiz Screen
  if (uiState.isQuizScreenOpen) {
    KanjiQuizScreen(
      selectedLevel = uiState.selectedLevel,
      lessonsForLevel = uiState.lessonsForSelectedLevel,
      onBack = { viewModel.closeQuizScreen() },
      onPlayAudio = { text -> viewModel.playAudio(text) },
      onQuizCompleted = { scorePercent, practicedIds ->
        viewModel.recordQuizResult(scorePercent, practicedIds)
      }
    )
    return
  }

  // 2. If practice writing is open, show dedicated PracticeWritingScreen
  if (uiState.isPracticeWritingScreenOpen) {
    PracticeWritingScreen(
      kanji = uiState.currentKanji,
      lessonTitle = uiState.currentLesson.title,
      currentIndex = uiState.currentIndex,
      totalKanji = uiState.allKanji.size,
      isBookmarked = uiState.isCurrentBookmarked,
      onToggleBookmark = { viewModel.toggleBookmark() },
      onBack = { viewModel.closePracticeWriting() },
      onPlayAudio = { viewModel.playAudio(uiState.currentKanji.character) },
      onNextKanji = { viewModel.nextKanji() }
    )
    return
  }

  // 3. If lesson is completed, show the dedicated LessonCompletedScreen with overview and actions!
  if (uiState.isLessonCompleted) {
    LessonCompletedScreen(
      kanjiList = uiState.allKanji,
      lessonTitle = uiState.currentLesson.title,
      lessonLevel = uiState.currentLesson.level,
      onReviseAgain = {
        viewModel.restartLesson()
        coroutineScope.launch {
          snackbarHostState.showSnackbar("${uiState.currentLesson.title} restarted!")
        }
      },
      onStartNextLesson = {
        viewModel.startNextLesson()
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Next lesson started! Ready for more Kanji study 🎉")
        }
      },
      onBackToLesson = { viewModel.openHomeScreen() },
      onPlayKanjiAudio = { text -> viewModel.playAudio(text) },
      onSelectKanjiForReview = { index -> viewModel.selectKanji(index) }
    )
    return
  }

  BackHandler {
    showLeaveLessonDialog = true
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackground),
    containerColor = MainBackground,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      // Bottom Navigation: Previous / Next Kanji or Complete Lesson
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
            .padding(horizontal = 20.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Previous button
          OutlinedButton(
            onClick = { viewModel.previousKanji() },
            enabled = uiState.currentIndex > 0,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("previous_kanji_button"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
              1.2.dp,
              if (uiState.currentIndex > 0) BorderOutline else BorderOutline.copy(alpha = 0.5f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = if (uiState.currentIndex > 0) CardSurface else CardSurfaceElevated,
              contentColor = SecondaryText,
              disabledContentColor = SecondaryText.copy(alpha = 0.45f)
            )
          ) {
            Text(
              text = "< Previous",
              fontWeight = FontWeight.SemiBold,
              fontSize = 15.sp
            )
          }

          // Next Kanji / Complete Lesson button
          val isLastKanji = uiState.currentIndex >= uiState.currentKanjiList.size - 1

          Button(
            onClick = {
              if (!isLastKanji) {
                viewModel.nextKanji()
              } else {
                viewModel.completeLesson()
              }
            },
            modifier = Modifier
              .weight(1.3f)
              .height(48.dp)
              .testTag("next_kanji_button"),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = BrandPurple,
              contentColor = Color.White
            )
          ) {
            Text(
              text = if (!isLastKanji) "Next Kanji >" else "Complete Lesson 🎉",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
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
      // 1. Top App Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { showLeaveLessonDialog = true },
          modifier = Modifier.testTag("top_back_button")
        ) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Lessons",
            tint = PrimaryText
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .clickable { viewModel.setKanjiSelectorVisible(true) }
        ) {
          Text(
            text = uiState.currentLesson.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
          )
          Text(
            text = "${uiState.currentLesson.level} · Kanji ${uiState.currentIndex + 1} of ${uiState.currentKanjiList.size}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandPurple
          )
        }

        // Bookmark button
        IconButton(
          onClick = {
            viewModel.toggleBookmark()
            coroutineScope.launch {
              val msg = if (uiState.isCurrentBookmarked) {
                "Removed ${uiState.currentKanji.character} from bookmarks"
              } else {
                "Saved ${uiState.currentKanji.character} to bookmarks ★"
              }
              snackbarHostState.showSnackbar(msg)
            }
          },
          modifier = Modifier.testTag("bookmark_button")
        ) {
          Icon(
            if (uiState.isCurrentBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = "Bookmark",
            tint = if (uiState.isCurrentBookmarked) BrandPurple else SecondaryText
          )
        }

        // 3-dots Menu
        Box {
          IconButton(
            onClick = { showOverflowMenu = true },
            modifier = Modifier.testTag("overflow_menu_button")
          ) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = SecondaryText)
          }

          DropdownMenu(
            expanded = showOverflowMenu,
            onDismissRequest = { showOverflowMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("${uiState.currentLesson.title} Overview") },
              leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = BrandPurple) },
              onClick = {
                showOverflowMenu = false
                viewModel.completeLesson()
              }
            )
            DropdownMenuItem(
              text = { Text("All Kanji in ${uiState.currentLesson.title}") },
              leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandPurple) },
              onClick = {
                showOverflowMenu = false
                viewModel.setKanjiSelectorVisible(true)
              }
            )
            DropdownMenuItem(
              text = { Text("Pronounce Kanji") },
              leadingIcon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = BrandPurple) },
              onClick = {
                showOverflowMenu = false
                viewModel.playAudio(uiState.currentKanji.character)
              }
            )
          }
        }
      }

      // Progress bar (1 / 10)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        LinearProgressIndicator(
          progress = { uiState.progress },
          modifier = Modifier
            .weight(1f)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = BrandPurple,
          trackColor = BorderOutline,
          strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
          text = "${uiState.currentIndex + 1} / ${uiState.currentKanjiList.size}",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = SecondaryText
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Main Scrollable Content
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item {
          // Card 1: Main Kanji Hero Card with Slide Animation
          AnimatedContent(
            targetState = uiState.currentKanji,
            transitionSpec = {
              (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                slideOutHorizontally { width -> -width } + fadeOut()
              )
            },
            label = "KanjiCardAnimation"
          ) { kanji ->
            KanjiHeroCard(
              kanji = kanji,
              isPlaying = uiState.currentlyPlayingText == kanji.character,
              onPlayAudio = { viewModel.playAudio(kanji.character) }
            )
          }
        }

        item {
          // Card 2: Readings Card (Kun'yomi & On'yomi with interactive audio chips)
          ReadingsCard(
            kanji = uiState.currentKanji,
            currentlyPlaying = uiState.currentlyPlayingText,
            onPlayReading = { reading -> viewModel.playAudio(reading) },
            onPlayAllKun = { viewModel.playAllKunReadings() },
            onPlayAllOn = { viewModel.playAllOnReadings() }
          )
        }

        item {
          // Card 3: Vocabulary Card
          VocabularyCard(
            vocabList = uiState.currentVocabulary,
            currentlyPlaying = uiState.currentlyPlayingText,
            onPlayVocab = { vocab -> viewModel.playVocabAudio(vocab) }
          )
        }

        item {
          // Card 4: Practice Writing Row
          PracticeWritingCard(
            kanji = uiState.currentKanji,
            onClick = { viewModel.openPracticeWriting() }
          )
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }

  // Kanji Overview Grid Bottom Sheet
  if (uiState.showKanjiSelectorSheet) {
    KanjiSelectorBottomSheet(
      lessonTitle = uiState.currentLesson.title,
      kanjiList = uiState.currentKanjiList,
      currentIndex = uiState.currentIndex,
      onSelectKanji = { selectedIndex ->
        viewModel.selectKanji(selectedIndex)
        viewModel.setKanjiSelectorVisible(false)
      },
      onDismiss = { viewModel.setKanjiSelectorVisible(false) }
    )
  }

  // Leave Lesson Confirmation Dialog matching reference UI
  if (showLeaveLessonDialog) {
    LeaveLessonDialog(
      onContinue = { showLeaveLessonDialog = false },
      onLeave = {
        showLeaveLessonDialog = false
        viewModel.openHomeScreen()
      }
    )
  }
}

/**
 * Leave Lesson Confirmation Dialog matching the user's reference screenshot:
 * - Clean white rounded card (22.dp)
 * - "Leave this lesson?" header
 * - "Your mistakes are saved for retry. This unfinished drill will not count as a completed lesson."
 * - Side-by-side [ Continue ] (white outlined) and [ Leave ] (coral red #EF4444) buttons
 */
@Composable
fun LeaveLessonDialog(
  onContinue: () -> Unit,
  onLeave: () -> Unit
) {
  Dialog(onDismissRequest = onContinue) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("leave_lesson_dialog"),
      shape = RoundedCornerShape(22.dp),
      color = CardSurface,
      shadowElevation = 12.dp,
      border = BorderStroke(1.dp, BorderOutline)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 24.dp)
      ) {
        Text(
          text = "Leave this lesson?",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryText
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Your mistakes are saved for retry. This unfinished drill will not count as a completed lesson.",
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Medium,
          color = SecondaryText,
          lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Continue button
          OutlinedButton(
            onClick = onContinue,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("leave_dialog_continue_button"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.2.dp, BorderOutline),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = CardSurfaceElevated,
              contentColor = PrimaryText
            )
          ) {
            Text(
              text = "Continue",
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Leave button (Coral Red #EF4444, white bold text)
          Button(
            onClick = onLeave,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("leave_dialog_leave_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFEF4444),
              contentColor = Color.White
            )
          ) {
            Text(
              text = "Leave",
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

/**
 * 1. Kanji Hero Card (Big Kanji box on left, Title & Badges in middle, Audio button on right)
 */
@Composable
fun KanjiHeroCard(
  kanji: Kanji,
  isPlaying: Boolean,
  onPlayAudio: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("kanji_hero_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurface),
    border = BorderStroke(1.2.dp, BorderOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Big Kanji Character Box with distinct border
      Box(
        modifier = Modifier
          .size(86.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(BrandPurpleVeryLight)
          .border(1.5.dp, SoftPurpleBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = kanji.character,
          fontSize = 46.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryText
        )
      }

      Spacer(modifier = Modifier.width(18.dp))

      // Middle: Meaning + Badges
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = kanji.meaning,
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Level badge (N5)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(BrandPurpleLight)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = kanji.level,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BrandPurple
            )
          }

          // Stroke count badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(BrandPurpleLight)
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${kanji.strokeCount} strokes",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BrandPurple
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Speaker / Audio button on right with active feedback
      val audioBgColor by animateColorAsState(
        targetValue = if (isPlaying) BrandPurple else BrandPurpleVeryLight,
        animationSpec = tween(200),
        label = "HeroAudioBg"
      )
      val audioIconColor by animateColorAsState(
        targetValue = if (isPlaying) Color.White else BrandPurple,
        animationSpec = tween(200),
        label = "HeroAudioIcon"
      )

      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .border(1.2.dp, SoftPurpleBorder, CircleShape)
          .background(audioBgColor)
          .clickable(onClick = onPlayAudio)
          .testTag("kanji_audio_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.AutoMirrored.Filled.VolumeUp,
          contentDescription = "Pronounce ${kanji.character}",
          tint = audioIconColor,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}

/**
 * 2. Readings Card (Kun'yomi & On'yomi rows with chips and speaker buttons)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadingsCard(
  kanji: Kanji,
  currentlyPlaying: String?,
  onPlayReading: (String) -> Unit,
  onPlayAllKun: () -> Unit,
  onPlayAllOn: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("readings_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurface),
    border = BorderStroke(1.2.dp, BorderOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Text(
        text = "Readings",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryText
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Kun'yomi Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Label pill
        Box(
          modifier = Modifier
            .width(78.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BrandPurpleLight)
            .padding(vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Kun'yomi",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPurple,
            maxLines = 1,
            softWrap = false
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Wrapping readings chips so pills never squish vertically
        FlowRow(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (kanji.kunReadings.isEmpty()) {
            Text(
              text = "—",
              fontSize = 14.sp,
              color = SecondaryText,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )
          } else {
            kanji.kunReadings.forEach { reading ->
              val isChipPlaying = currentlyPlaying == reading
              val chipBg by animateColorAsState(
                targetValue = if (isChipPlaying) BrandPurple else CardSurfaceElevated,
                label = "KunChipBg"
              )
              val chipTextColor by animateColorAsState(
                targetValue = if (isChipPlaying) Color.White else PrimaryText,
                label = "KunChipText"
              )

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .border(
                    1.2.dp,
                    if (isChipPlaying) BrandPurple else BorderOutline,
                    RoundedCornerShape(12.dp)
                  )
                  .background(chipBg)
                  .clickable { onPlayReading(reading) }
                  .padding(horizontal = 11.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = reading,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = chipTextColor,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }

        if (kanji.kunReadings.isNotEmpty()) {
          Spacer(modifier = Modifier.width(10.dp))

          // Speaker icon on right
          val isAllKunPlaying = currentlyPlaying == kanji.kunReadings.joinToString("、")
          val allKunBg by animateColorAsState(
            targetValue = if (isAllKunPlaying) BrandPurple else BrandPurpleVeryLight,
            label = "AllKunBg"
          )
          val allKunIcon by animateColorAsState(
            targetValue = if (isAllKunPlaying) Color.White else BrandPurple,
            label = "AllKunIcon"
          )

          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .border(1.2.dp, SoftPurpleBorder, CircleShape)
              .background(allKunBg)
              .clickable(onClick = onPlayAllKun),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.AutoMirrored.Filled.VolumeUp,
              contentDescription = "Play Kun'yomi",
              tint = allKunIcon,
              modifier = Modifier.size(19.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // On'yomi Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Label pill
        Box(
          modifier = Modifier
            .width(78.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BrandPurpleLight)
            .padding(vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "On'yomi",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPurple,
            maxLines = 1,
            softWrap = false
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Wrapping readings chips so pills never squish vertically
        FlowRow(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (kanji.onReadingsDisplay.isEmpty()) {
            Text(
              text = "—",
              fontSize = 14.sp,
              color = SecondaryText,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )
          } else {
            kanji.onReadingsDisplay.forEach { reading ->
              val isChipPlaying = currentlyPlaying == reading
              val chipBg by animateColorAsState(
                targetValue = if (isChipPlaying) BrandPurple else CardSurfaceElevated,
                label = "OnChipBg"
              )
              val chipTextColor by animateColorAsState(
                targetValue = if (isChipPlaying) Color.White else PrimaryText,
                label = "OnChipText"
              )

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .border(
                    1.2.dp,
                    if (isChipPlaying) BrandPurple else BorderOutline,
                    RoundedCornerShape(12.dp)
                  )
                  .background(chipBg)
                  .clickable { onPlayReading(reading) }
                  .padding(horizontal = 11.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = reading,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = chipTextColor,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }

        if (kanji.onReadingsDisplay.isNotEmpty()) {
          Spacer(modifier = Modifier.width(10.dp))

          // Speaker icon on right
          val isAllOnPlaying = currentlyPlaying == kanji.onReadingsDisplay.joinToString("、")
          val allOnBg by animateColorAsState(
            targetValue = if (isAllOnPlaying) BrandPurple else BrandPurpleVeryLight,
            label = "AllOnBg"
          )
          val allOnIcon by animateColorAsState(
            targetValue = if (isAllOnPlaying) Color.White else BrandPurple,
            label = "AllOnIcon"
          )

          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .border(1.2.dp, SoftPurpleBorder, CircleShape)
              .background(allOnBg)
              .clickable(onClick = onPlayAllOn),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.AutoMirrored.Filled.VolumeUp,
              contentDescription = "Play On'yomi",
              tint = allOnIcon,
              modifier = Modifier.size(19.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * 3. Vocabulary Card (from your book)
 */
@Composable
fun VocabularyCard(
  vocabList: List<Vocabulary>,
  currentlyPlaying: String?,
  onPlayVocab: (Vocabulary) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("vocabulary_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurface),
    border = BorderStroke(1.2.dp, BorderOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Vocabulary",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryText
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "(from your book)",
          fontSize = 13.sp,
          color = SecondaryText
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      vocabList.forEachIndexed { index, vocab ->
        val isVocabPlaying = currentlyPlaying == vocab.word
        val rowBg by animateColorAsState(
          targetValue = if (isVocabPlaying) BrandPurpleVeryLight else Color.Transparent,
          label = "VocabRowBg"
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(rowBg)
            .clickable { onPlayVocab(vocab) }
            .padding(horizontal = 4.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Speaker icon
          Icon(
            Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Pronounce ${vocab.word}",
            tint = BrandPurple,
            modifier = Modifier.size(20.dp)
          )

          Spacer(modifier = Modifier.width(10.dp))

          // Word stacked cleanly above its reading (no awkward mid-furigana line breaks)
          Column(modifier = Modifier.weight(1.15f)) {
            Text(
              text = vocab.word,
              fontSize = 15.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = PrimaryText
            )
            if (vocab.reading.isNotBlank()) {
              Text(
                text = vocab.reading,
                fontSize = 12.5.sp,
                color = BrandPurple
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Meaning
          Text(
            text = vocab.meaning,
            fontSize = 13.sp,
            color = SecondaryText,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
          )
        }

        if (index < vocabList.size - 1) {
          HorizontalDivider(
            color = BorderOutline,
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 2.dp)
          )
        }
      }
    }
  }
}

/**
 * 4. Practice Writing Card / Row with preview genkouyoushi boxes
 */
@Composable
fun PracticeWritingCard(
  kanji: Kanji,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("practice_writing_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = CardSurface),
    border = BorderStroke(1.2.dp, BorderOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Edit / pencil icon
      Icon(
        Icons.Default.Edit,
        contentDescription = "Practice Writing",
        tint = BrandPurple,
        modifier = Modifier.size(24.dp)
      )

      Spacer(modifier = Modifier.width(12.dp))

      Text(
        text = "Practice Writing",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryText,
        maxLines = 1,
        softWrap = false
      )

      Spacer(modifier = Modifier.weight(1f))
      Spacer(modifier = Modifier.width(10.dp))

      // 3 crisp preview boxes with comfortable breathing room
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        repeat(3) {
          PracticeWritingBox(
            character = kanji.character,
            size = 34.dp,
            onClick = onClick
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Chevron
      Icon(
        Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = "Open Practice Writing",
        tint = SecondaryText,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

/**
 * Bottom sheet to pick any Kanji from the lesson
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiSelectorBottomSheet(
  lessonTitle: String,
  kanjiList: List<Kanji>,
  currentIndex: Int,
  onSelectKanji: (Int) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()

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
        .padding(bottom = 32.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "$lessonTitle (${kanjiList.size} Kanji)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
          )
          Text(
            text = "Tap any Kanji to jump directly",
            fontSize = 13.sp,
            color = SecondaryText
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = SecondaryText)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        itemsIndexed(kanjiList) { index, item ->
          val isSelected = index == currentIndex

          Column(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) BrandPurpleLight else CardSurfaceElevated)
              .border(
                1.5.dp,
                if (isSelected) BrandPurple else BorderOutline,
                RoundedCornerShape(12.dp)
              )
              .clickable { onSelectKanji(index) }
              .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = item.character,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) BrandPurple else PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = item.meaning.take(6),
              fontSize = 10.sp,
              color = SecondaryText,
              maxLines = 1
            )
          }
        }
      }
    }
  }
}
