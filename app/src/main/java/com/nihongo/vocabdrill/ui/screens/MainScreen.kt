package com.nihongo.vocabdrill.ui.screens

import androidx.activity.compose.BackHandler

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.nihongo.vocabdrill.model.Screen
import com.nihongo.vocabdrill.model.ThemeMode
import com.nihongo.vocabdrill.kanji.ui.KanjiModuleScreen
import com.nihongo.vocabdrill.ui.components.AppUpdateCard
import com.nihongo.vocabdrill.ui.theme.NihongoVocabTheme
import com.nihongo.vocabdrill.update.AppUpdateState
import com.nihongo.vocabdrill.update.AppUpdateViewModel
import com.nihongo.vocabdrill.viewmodel.NihongoViewModel
import java.io.File

@Composable
fun MainScreen(
    viewModel: NihongoViewModel,
    appUpdateViewModel: AppUpdateViewModel,
    onInstallUpdate: (File) -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val previewLesson by viewModel.previewLesson.collectAsState()
    val showSettings by viewModel.showSettingsDialog.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val updateState by appUpdateViewModel.state.collectAsState()
    var dismissedUpdateVersion by rememberSaveable { mutableStateOf(-1L) }
    var showKanjiModule by rememberSaveable { mutableStateOf(false) }


    val updateVersionCode = when (updateState) {
        is AppUpdateState.Available -> (updateState as AppUpdateState.Available).info.versionCode
        is AppUpdateState.Downloading -> (updateState as AppUpdateState.Downloading).info.versionCode
        is AppUpdateState.Downloaded -> (updateState as AppUpdateState.Downloaded).info.versionCode
        is AppUpdateState.Failed -> (updateState as AppUpdateState.Failed).info?.versionCode ?: -1L
        else -> -1L
    }
    val shouldShowUpdateCard = currentScreen == Screen.ChapterSelect &&
        updateVersionCode > 0L &&
        (updateVersionCode != dismissedUpdateVersion || updateState !is AppUpdateState.Available)

    NihongoVocabTheme(themeMode = settings.themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                if (shouldShowUpdateCard) {
                    AppUpdateCard(
                        state = updateState,
                        onDownload = appUpdateViewModel::downloadUpdate,
                        onInstall = {
                            val downloaded = updateState as? AppUpdateState.Downloaded
                            downloaded?.let { onInstallUpdate(it.apkFile) }
                        },
                        onRetryCheck = { appUpdateViewModel.checkForUpdate() },
                        onLater = if (updateState is AppUpdateState.Available) {
                            { dismissedUpdateVersion = updateVersionCode }
                        } else {
                            null
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Crossfade(
                    targetState = currentScreen,
                    modifier = Modifier.weight(1f),
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        is Screen.ChapterSelect -> {
                            if (showKanjiModule) {
                                val isDark = when (settings.themeMode) {
                                    ThemeMode.DARK -> true
                                    ThemeMode.LIGHT -> false
                                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                                }
                                KanjiModuleScreen(
                                    onBackToVocabulary = { showKanjiModule = false },
                                    isDarkTheme = isDark,
                                    onToggleTheme = {
                                        viewModel.updateThemeMode(
                                            if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
                                        )
                                    }
                                )
                            } else {
                                ChapterSelectScreen(
                                    viewModel = viewModel,
                                    onStartQuiz = { lessonNum -> viewModel.startQuiz(lessonNum) },
                                    onOpenKanji = { showKanjiModule = true }
                                )
                            }
                        }
                        is Screen.Quiz -> {
                            QuizScreen(
                                lessonNumber = screen.lessonNumber,
                                viewModel = viewModel,
                                customTitle = screen.customTitle
                            )
                        }
                        is Screen.Completion -> {
                            CompletionScreen(
                                summary = screen.summary,
                                viewModel = viewModel,
                                onReturnHome = {
                                    viewModel.navigateToHome()
                                },
                                onRetry = {
                                    viewModel.retryCurrentQuiz()
                                },
                                onNextLesson = { nextLesson ->
                                    viewModel.startQuiz(nextLesson)
                                },
                                onDrillMistakes = { mistakes ->
                                    viewModel.startQuizForMistakes(mistakes)
                                }
                            )
                        }
                        is Screen.FavoritesList -> {
                            viewModel.setChapterFilter(com.nihongo.vocabdrill.viewmodel.ChapterFilter.FAVORITES)
                            ChapterSelectScreen(
                                viewModel = viewModel,
                                onStartQuiz = { lessonNum -> viewModel.startQuiz(lessonNum) },
                                onOpenKanji = { showKanjiModule = true }
                            )
                        }
                    }
                }

                // Vocab Preview Dialog
                previewLesson?.let { lesson ->
                    VocabPreviewDialog(
                        lesson = lesson,
                        viewModel = viewModel,
                        onDismiss = { viewModel.closePreview() },
                        onStartQuiz = {
                            viewModel.startQuiz(lesson.lessonNumber)
                        }
                    )
                }

                // Settings Dialog
                if (showSettings) {
                    SettingsDialog(
                        viewModel = viewModel,
                        onDismiss = { viewModel.closeSettings() }
                    )
                }
            }
        }
    }
}
