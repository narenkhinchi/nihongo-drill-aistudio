package com.nihongo.vocabdrill.viewmodel

import android.app.Application
import android.os.SystemClock
import com.nihongo.vocabdrill.model.RecallCountdown
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nihongo.vocabdrill.data.LessonRepository
import com.nihongo.vocabdrill.data.PreferencesRepository
import com.nihongo.vocabdrill.model.AppSettings
import com.nihongo.vocabdrill.model.DrillDirection
import com.nihongo.vocabdrill.model.LessonInfo
import com.nihongo.vocabdrill.model.QuizQuestion
import com.nihongo.vocabdrill.model.QuizSummary
import com.nihongo.vocabdrill.model.Screen
import com.nihongo.vocabdrill.model.ThemeMode
import com.nihongo.vocabdrill.model.UserProgress
import com.nihongo.vocabdrill.model.VocabItem
import com.nihongo.vocabdrill.ui.util.SoundHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ChapterFilter(val label: String) {
    ALL("All"),
    TO_LEARN("To Learn"),
    COMPLETED("Completed"),
    FAVORITES("Starred")
}

class NihongoViewModel @JvmOverloads constructor(
    application: Application,
    private val lessonRepo: LessonRepository = LessonRepository(application)
) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application)
    val soundHelper = SoundHelper(application)

    val appSettings: StateFlow<AppSettings> = prefsRepo.settings
    val userProgress: StateFlow<UserProgress> = prefsRepo.progress

    private val _currentScreen = MutableStateFlow<Screen>(Screen.ChapterSelect)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _chapterFilter = MutableStateFlow(ChapterFilter.ALL)
    val chapterFilter: StateFlow<ChapterFilter> = _chapterFilter.asStateFlow()

    val lessonLibrary = lessonRepo.library
    val allLessons: List<LessonInfo> get() = lessonRepo.getAllLessons()
    fun refreshLessons(force: Boolean = false) { viewModelScope.launch { lessonRepo.refresh(force) } }
    fun downloadLesson(number: Int) { viewModelScope.launch { lessonRepo.download(number) } }
    fun wordsForLesson(number: Int) = lessonRepo.getVocabForLesson(number)
    fun canStartLesson(number: Int) = lessonLibrary.value.isDownloaded(number)

    val filteredLessons: StateFlow<List<LessonInfo>> = combine(
        _searchQuery,
        _chapterFilter,
        userProgress,
        appSettings,
        lessonLibrary
    ) { query, filter, progress, settings, library ->
        var list = library.lessons

        // Apply tab filter
        list = when (filter) {
            ChapterFilter.ALL -> list
            ChapterFilter.TO_LEARN -> list.filter { !progress.completedLessons.contains(it.lessonNumber) }
            ChapterFilter.COMPLETED -> list.filter { progress.completedLessons.contains(it.lessonNumber) }
            ChapterFilter.FAVORITES -> {
                val favIds = progress.favoriteWordIds
                list.filter { lesson ->
                    val words = lessonRepo.getVocabForLesson(lesson.lessonNumber)
                    words.any { favIds.contains(it.id) }
                }
            }
        }

        if (settings.hideOtherLessons) list = allLessons.filter { it.lessonNumber == settings.currentLessonNumber }

        // Apply search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { lesson ->
                lesson.lessonNumber.toString().contains(q) ||
                lesson.title.lowercase().contains(q) ||
                lesson.japaneseTitle.contains(q) ||
                lesson.description.lowercase().contains(q) ||
                lesson.grammarFocus.lowercase().contains(q)
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allLessons)

    private val _previewLesson = MutableStateFlow<LessonInfo?>(null)
    val previewLesson: StateFlow<LessonInfo?> = _previewLesson.asStateFlow()

    private val _previewVocabList = MutableStateFlow<List<VocabItem>>(emptyList())
    val previewVocabList: StateFlow<List<VocabItem>> = _previewVocabList.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    // Active Quiz State
    private val _currentQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val currentQuizQuestions: StateFlow<List<QuizQuestion>> = _currentQuizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _activeLessonNumber = MutableStateFlow(1)
    val activeLessonNumber: StateFlow<Int> = _activeLessonNumber.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private var encouragementGate = com.nihongo.vocabdrill.model.EncouragementGate()
    private val _encouragementRemaining = MutableStateFlow<Int?>(null)
    val encouragementRemaining = _encouragementRemaining.asStateFlow()
    private var sessionWords: List<VocabItem> = emptyList()
    private var sessionTitle = "Review"

    fun toggleEncouragement(enabled: Boolean) {
        prefsRepo.toggleEncouragement(enabled)
        if (!enabled) _encouragementRemaining.value = null
    }

    fun continueAfterEncouragement() {
        _encouragementRemaining.value = null
        nextQuestion()
    }

    fun startSavedMistakes(lessonNumber: Int) {
        prefsRepo.setCurrentLesson(lessonNumber)
        val words = lessonRepo.getVocabForLesson(lessonNumber)
            .filter { it.id in userProgress.value.wrongWordIds }
        startDrillWithItems(words.shuffled(), 0, "Lesson $lessonNumber · Mistakes")
    }

    private var autoAdvanceJob: Job? = null
    private val recallClock = RecallCountdown(SystemClock::elapsedRealtime)
    private var recallTimerJob: Job? = null
    private var quizForeground = true
    private val _recallTimerSeconds = MutableStateFlow<Int?>(null)
    val recallTimerSeconds = _recallTimerSeconds.asStateFlow()
    private val _recallRemainingMillis = MutableStateFlow(0L)
    val recallRemainingMillis = _recallRemainingMillis.asStateFlow()
    private val _recallTimerPaused = MutableStateFlow(true)
    val recallTimerPaused = _recallTimerPaused.asStateFlow()

    fun selectRecallTimer(seconds: Int) {
        if (_currentScreen.value !is Screen.Quiz || !appSettings.value.hideMcqOptions ||
            _recallTimerSeconds.value != null || seconds !in listOf(3, 5)) return
        _recallTimerSeconds.value = seconds
        prepareRecallTimer()
    }

    fun setQuizForeground(foreground: Boolean) {
        quizForeground = foreground
        if (!foreground) pauseAutoAdvance()
        // Returning from another app never silently restarts a paused clock.
    }

    private fun stopRecallTicker() {
        recallClock.pause()
        _recallRemainingMillis.value = recallClock.remainingMillis
        recallTimerJob?.cancel()
        recallTimerJob = null
        _recallTimerPaused.value = true
    }

    private fun prepareRecallTimer() {
        stopRecallTicker()
        recallClock.reset((_recallTimerSeconds.value ?: 0) * 1000L)
        _recallRemainingMillis.value = recallClock.remainingMillis
        startRecallTicker()
    }

    private fun startRecallTicker() {
        val index = _currentQuestionIndex.value
        val question = _currentQuizQuestions.value.getOrNull(index) ?: return
        if (_currentScreen.value !is Screen.Quiz || !appSettings.value.hideMcqOptions ||
            (_recallTimerSeconds.value ?: 0) == 0 || question.isAnswered || !quizForeground ||
            _showSettingsDialog.value || _encouragementRemaining.value != null) return
        if (recallClock.remainingMillis <= 0) {
            nextQuestion()
            return
        }
        recallTimerJob?.cancel()
        recallClock.resume()
        _recallTimerPaused.value = false
        recallTimerJob = viewModelScope.launch {
            while (true) {
                delay(50)
                if (_currentQuestionIndex.value != index || _currentScreen.value !is Screen.Quiz) return@launch
                val remaining = recallClock.remainingMillis
                if ((remaining + 999) / 1000 != (_recallRemainingMillis.value + 999) / 1000) {
                    _recallRemainingMillis.value = remaining
                }
                if (remaining == 0L) {
                    recallClock.pause()
                    recallTimerJob = null
                    _recallTimerPaused.value = true
                    nextQuestion()
                    return@launch
                }
            }
        }
    }

    fun toggleRecallTimerPause() {
        if (_recallTimerPaused.value) startRecallTicker() else stopRecallTicker()
    }

    private fun resetRecallTimer() {
        stopRecallTicker()
        recallClock.reset(0)
        _recallRemainingMillis.value = 0
        _recallTimerSeconds.value = null
    }

    fun revealMeaning(expectedIndex: Int = _currentQuestionIndex.value) {
        if (_currentScreen.value !is Screen.Quiz || !appSettings.value.hideMcqOptions ||
            expectedIndex != _currentQuestionIndex.value || _encouragementRemaining.value != null) return
        val current = _currentQuizQuestions.value.getOrNull(expectedIndex) ?: return
        if (current.meaningRevealed) return
        stopRecallTicker()
        if (!current.isAnswered) {
            scoreCurrent(false, recall = true, revealed = true)
        } else {
            // A later meaning check can turn a self-reported right answer into one wrong answer.
            if (current.isCorrect) soundHelper.vibrateIncorrect(true)
            val questions = _currentQuizQuestions.value.toMutableList()
            questions[expectedIndex] = current.copy(meaningRevealed = true, isRecallAnswer = true, isCorrect = false)
            _currentQuizQuestions.value = questions
            prefsRepo.recordWordAnswer(current.vocabItem.id, false)
            _currentStreak.value = 0
            encouragementGate.answer(false, 0, appSettings.value.encouragement)
            var run = 0
            maxStreakInSession = 0
            questions.forEach { run = if (it.isCorrect) run + 1 else 0; maxStreakInSession = maxOf(maxStreakInSession, run) }
        }
    }
    private var maxStreakInSession = 0
    private var quizStartTime = 0L

    val currentQuestion: StateFlow<QuizQuestion?> = combine(
        _currentQuizQuestions,
        _currentQuestionIndex
    ) { questions, index ->
        if (questions.isNotEmpty() && index in questions.indices) {
            questions[index]
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleOtherLessons() { prefsRepo.toggleOtherLessons() }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setChapterFilter(filter: ChapterFilter) {
        _chapterFilter.value = filter
    }

    fun openPreview(lesson: LessonInfo) {
        _previewLesson.value = lesson
        _previewVocabList.value = lessonRepo.getVocabForLesson(lesson.lessonNumber)
    }

    fun closePreview() {
        _previewLesson.value = null
        _previewVocabList.value = emptyList()
    }

    fun pauseAutoAdvance() {
        autoAdvanceJob?.cancel()
        stopRecallTicker()
    }

    fun openSettings() {
        pauseAutoAdvance()
        _showSettingsDialog.value = true
    }

    fun closeSettings() {
        _showSettingsDialog.value = false
    }

    val favoriteWordsList: StateFlow<List<VocabItem>> = combine(userProgress, lessonLibrary) { progress, library ->
        library.installed.values.flatMap { it.words }.filter { progress.favoriteWordIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startQuiz(lessonNumber: Int, questionLimit: Int = appSettings.value.questionsPerDrill) {
        val lessonWords = lessonRepo.getVocabForLesson(lessonNumber).shuffled()
        val limit = if (questionLimit <= 0 || questionLimit >= lessonWords.size) lessonWords.size else questionLimit
        val selectedWords = lessonWords.take(limit)
        startDrillWithItems(
            words = selectedWords,
            lessonNum = lessonNumber,
            title = lessonRepo.getLessonInfo(lessonNumber)?.title ?: "Lesson $lessonNumber"
        )
    }

    fun startQuickRandomDrill() {
        val allVocab = lessonRepo.getAllVocab().shuffled()
        val limit = appSettings.value.questionsPerDrill.coerceAtLeast(10)
        val selectedWords = allVocab.take(limit)
        startDrillWithItems(
            words = selectedWords,
            lessonNum = 0,
            title = "Quick Random Review"
        )
    }

    fun startQuizForMistakes(wrongItems: List<VocabItem>) {
        if (wrongItems.isEmpty()) return
        startDrillWithItems(
            words = wrongItems.shuffled(),
            lessonNum = 0,
            title = "Mistakes Review Drill"
        )
    }

    fun startFavoriteWordsDrill() {
        val favs = lessonRepo.getAllVocab().filter { it.id in userProgress.value.favoriteWordIds }
        if (favs.isEmpty()) return
        startDrillWithItems(
            words = favs.shuffled(),
            lessonNum = 0,
            title = "Starred Words Drill"
        )
    }

    private fun startDrillWithItems(words: List<VocabItem>, lessonNum: Int, title: String) {
        if (words.isEmpty()) return
        autoAdvanceJob?.cancel()
        encouragementGate = com.nihongo.vocabdrill.model.EncouragementGate()
        _encouragementRemaining.value = null
        if (lessonNum > 0) prefsRepo.setCurrentLesson(lessonNum)
        sessionWords = words.toList()
        sessionTitle = title
        resetRecallTimer()
        closePreview()
        val overallDirection = appSettings.value.drillDirection
        val questions = words.mapIndexed { idx, item ->
            val effectiveDirection = when (overallDirection) {
                DrillDirection.MIX_BOTH -> if (idx % 2 == 0) DrillDirection.HIRAGANA_TO_ENGLISH else DrillDirection.ENGLISH_TO_HIRAGANA
                else -> overallDirection
            }
            val (promptText, promptSubtext, correctAnswer) = when (effectiveDirection) {
                DrillDirection.HIRAGANA_TO_ENGLISH -> {
                    Triple(item.displayJapanese, item.romaji, item.english)
                }
                DrillDirection.ENGLISH_TO_HIRAGANA -> {
                    Triple(item.english, null, item.displayJapanese)
                }
                DrillDirection.KANJI_TO_KANA -> {
                    val prompt = if (item.kanji.isNotBlank()) item.kanji else item.kana
                    Triple(prompt, item.romaji, item.kana)
                }
                else -> {
                    Triple(item.displayJapanese, item.romaji, item.english)
                }
            }
            val distractors = lessonRepo.getDistractors(item, count = 3, direction = effectiveDirection)
            val options = (distractors + correctAnswer).shuffled()

            QuizQuestion(
                questionIndex = idx,
                vocabItem = item,
                promptText = promptText,
                promptSubtext = promptSubtext,
                options = options,
                correctAnswer = correctAnswer
            )
        }

        _activeLessonNumber.value = lessonNum
        _currentQuizQuestions.value = questions
        _currentQuestionIndex.value = 0
        _currentStreak.value = 0
        maxStreakInSession = 0
        quizStartTime = System.currentTimeMillis()
        _currentScreen.value = Screen.Quiz(lessonNum, customTitle = title)

        if (appSettings.value.autoPlaySpeech && overallDirection != DrillDirection.ENGLISH_TO_HIRAGANA && questions.isNotEmpty()) {
            soundHelper.speakJapanese(questions[0].vocabItem.kana)
        }
    }

    private fun scoreCurrent(correct: Boolean, selected: String? = null, recall: Boolean = false, revealed: Boolean = false) {
        val index = _currentQuestionIndex.value
        val questions = _currentQuizQuestions.value.toMutableList()
        val current = questions.getOrNull(index) ?: return
        if (current.isAnswered) return
        stopRecallTicker()
        _currentStreak.value = if (correct) _currentStreak.value + 1 else 0
        maxStreakInSession = maxOf(maxStreakInSession, _currentStreak.value)
        if (correct) soundHelper.playCorrectChime(appSettings.value.soundEffects)
        else soundHelper.vibrateIncorrect(true)
        questions[index] = current.copy(selectedAnswer = selected, isAnswered = true, isCorrect = correct,
            isRecallAnswer = recall, meaningRevealed = revealed, currentStreak = _currentStreak.value)
        _currentQuizQuestions.value = questions
        prefsRepo.recordWordAnswer(current.vocabItem.id, correct)
        val remaining = questions.size - index - 1
        if (encouragementGate.answer(correct, remaining, appSettings.value.encouragement)) {
            autoAdvanceJob?.cancel()
            _encouragementRemaining.value = remaining
        }
    }

    fun submitAnswer(selectedOption: String) {
        if (_currentScreen.value !is Screen.Quiz || _encouragementRemaining.value != null) return
        val index = _currentQuestionIndex.value
        val current = _currentQuizQuestions.value.getOrNull(index) ?: return
        if (current.isAnswered || selectedOption !in current.options || appSettings.value.hideMcqOptions) return
        scoreCurrent(selectedOption == current.correctAnswer, selected = selectedOption)
        if (_encouragementRemaining.value != null) return
        autoAdvanceJob?.cancel()
        autoAdvanceJob = viewModelScope.launch {
            delay(1000)
            if (_currentScreen.value is Screen.Quiz && _currentQuestionIndex.value == index &&
                !appSettings.value.hideMcqOptions && !_showSettingsDialog.value) nextQuestion()
        }
    }

    fun previousQuestion() {
        if (_currentScreen.value !is Screen.Quiz || _encouragementRemaining.value != null) return
        autoAdvanceJob?.cancel()
        val prevIndex = _currentQuestionIndex.value - 1
        if (prevIndex >= 0) {
            _currentQuestionIndex.value = prevIndex
            prepareRecallTimer()
        }
    }

    fun nextQuestion() {
        if (_currentScreen.value !is Screen.Quiz || _encouragementRemaining.value != null) return
        autoAdvanceJob?.cancel()
        val current = _currentQuizQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        if (!current.isAnswered) {
            if (!appSettings.value.hideMcqOptions) return
            scoreCurrent(true, recall = true)
            if (_encouragementRemaining.value != null) return
        }
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < _currentQuizQuestions.value.size) {
            _currentQuestionIndex.value = nextIndex
            prepareRecallTimer()
            val next = _currentQuizQuestions.value[nextIndex]
            if (appSettings.value.autoPlaySpeech && next.promptText != next.vocabItem.english) {
                soundHelper.speakJapanese(next.vocabItem.kana)
            }
        } else {
            completeQuiz()
        }
    }

    private fun completeQuiz() {
        val questions = _currentQuizQuestions.value
        stopRecallTicker()
        val correctCount = questions.count { it.isCorrect }
        val wrongItems = questions.filter { !it.isCorrect }.map { it.vocabItem }
        val lessonNum = _activeLessonNumber.value
        val lessonTitle = sessionTitle
        val elapsedSec = ((System.currentTimeMillis() - quizStartTime) / 1000).coerceAtLeast(1)

        val summary = QuizSummary(
            lessonNumber = lessonNum,
            lessonTitle = lessonTitle,
            totalQuestions = questions.size,
            correctCount = correctCount,
            wrongItems = wrongItems,
            timeTakenSeconds = elapsedSec,
            includesRecallScore = questions.any { it.isRecallAnswer },
            maxStreak = maxStreakInSession
        )

        if (lessonNum > 0) {
            prefsRepo.recordQuizResult(
                lessonNumber = lessonNum,
                accuracyPercentage = summary.accuracyPercentage,
                totalQuestions = summary.totalQuestions,
                correctQuestions = summary.correctCount
            )
        }

        _currentScreen.value = Screen.Completion(summary)
    }

    fun retryCurrentQuiz() {
        startDrillWithItems(sessionWords.shuffled(), _activeLessonNumber.value, sessionTitle)
    }

    fun quitQuiz() {
        resetRecallTimer()
        _encouragementRemaining.value = null
        autoAdvanceJob?.cancel()
        soundHelper.stopSpeech()
        _currentQuizQuestions.value = emptyList()
        _currentScreen.value = Screen.ChapterSelect
    }

    fun navigateToHome() {
        resetRecallTimer()
        _encouragementRemaining.value = null
        autoAdvanceJob?.cancel()
        soundHelper.stopSpeech()
        _currentScreen.value = Screen.ChapterSelect
    }

    fun speak(text: String) {
        soundHelper.speakJapanese(text)
    }

    fun toggleFavorite(vocabId: String) {
        soundHelper.vibrateTick(appSettings.value.vibration)
        prefsRepo.toggleFavorite(vocabId)
    }

    fun updateThemeMode(mode: ThemeMode) {
        prefsRepo.updateThemeMode(mode)
    }

    fun updateDrillDirection(direction: DrillDirection) {
        prefsRepo.updateDrillDirection(direction)
        val screen = _currentScreen.value
        if (screen is Screen.Quiz) {
            val items = _currentQuizQuestions.value.map { it.vocabItem }
            val lockedDuration = _recallTimerSeconds.value
            startDrillWithItems(items, screen.lessonNumber, screen.customTitle ?: "Lesson")
            _recallTimerSeconds.value = lockedDuration
            prepareRecallTimer()
        }
    }

    fun toggleRomaji(enabled: Boolean) {
        prefsRepo.toggleRomaji(enabled)
    }

    fun toggleSoundEffects(enabled: Boolean) {
        prefsRepo.toggleSoundEffects(enabled)
    }

    fun toggleVibration(enabled: Boolean) {
        prefsRepo.toggleVibration(enabled)
    }

    fun toggleAutoPlaySpeech(enabled: Boolean) {
        prefsRepo.toggleAutoPlaySpeech(enabled)
    }

    fun toggleHideMcqOptions(enabled: Boolean) {
        autoAdvanceJob?.cancel()
        stopRecallTicker()
        prefsRepo.toggleHideMcqOptions(enabled)
        // Keep the selected duration locked even when options are toggled off and back on.
        // Resume is explicit; toggling a mode must not restart a ticking clock.
    }

    fun updateQuestionsPerDrill(count: Int) {
        prefsRepo.updateQuestionsPerDrill(count)
    }

    fun updateSpeechSpeed(speed: Float) {
        prefsRepo.updateSpeechSpeed(speed)
        soundHelper.setSpeechRate(speed)
    }

    fun openFavoritesScreen() {
        _currentScreen.value = Screen.FavoritesList
    }

    fun resetProgress() {
        prefsRepo.resetAllProgress()
    }

    override fun onCleared() {
        recallTimerJob?.cancel()
        autoAdvanceJob?.cancel()
        super.onCleared()
        soundHelper.release()
    }
}
