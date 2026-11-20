package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Kanji
import com.example.data.KanjiLesson
import com.example.data.KanjiRepository
import com.example.data.Vocabulary
import com.example.util.JapaneseTtsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KanjiUiState(
  val isHomeScreenOpen: Boolean = true,
  val selectedLevel: String = "N5",
  val selectedFilter: String = "All", // "All", "To Learn", "Completed", "Starred"
  val hideOtherLessons: Boolean = false,
  val activeLessonNumber: Int = 1,
  val currentLessonId: String = "N5_L1",
  val allKanji: List<Kanji> = KanjiRepository.lesson1Kanji,
  val currentIndex: Int = 0,
  val bookmarkedIds: Set<Int> = emptySet(),
  val completedLessonIds: Set<String> = emptySet(),
  val lessonBestScores: Map<String, Int> = emptyMap(),
  val practicedKanjiIds: Set<Int> = emptySet(),
  val accuracyReadings: List<Int> = emptyList(),
  val previewLesson: KanjiLesson? = null,
  val isSearchSheetOpen: Boolean = false,
  val searchQuery: String = "",
  val currentlyPlayingText: String? = null,
  val isLessonCompleted: Boolean = false,
  val isPracticeWritingScreenOpen: Boolean = false,
  val isReadingScreenOpen: Boolean = false,
  val isQuizScreenOpen: Boolean = false,
  val showKanjiSelectorSheet: Boolean = false
) {
  val currentLesson: KanjiLesson
    get() = KanjiRepository.allLessons.find { it.id == currentLessonId } ?: KanjiRepository.allLessons.first()

  val lessonsForSelectedLevel: List<KanjiLesson>
    get() = KanjiRepository.getLessonsForLevel(selectedLevel)

  val filteredLessons: List<KanjiLesson>
    get() {
      val base = lessonsForSelectedLevel
      if (hideOtherLessons) {
        val currentOnly = base.filter { it.lessonNumber == activeLessonNumber }
        return currentOnly.ifEmpty { base.take(1) }
      }
      return when (selectedFilter) {
        "To Learn" -> base.filter { !completedLessonIds.contains(it.id) }
        "Completed" -> base.filter { completedLessonIds.contains(it.id) }
        "Starred" -> base.filter { lesson -> lesson.kanjiList.any { bookmarkedIds.contains(it.number) } }
        else -> base
      }
    }

  val totalKanjiInLevel: Int
    get() = lessonsForSelectedLevel.sumOf { it.kanjiList.size }

  val completedLessonsCountForLevel: Int
    get() = lessonsForSelectedLevel.count { completedLessonIds.contains(it.id) }

  val practicedKanjiCountForLevel: Int
    get() {
      val levelKanjiIds = lessonsForSelectedLevel.flatMap { it.kanjiList }.map { it.number }.toSet()
      return practicedKanjiIds.count { levelKanjiIds.contains(it) }
    }

  val formattedAccuracy: String
    get() = if (accuracyReadings.isEmpty()) "0%" else "${accuracyReadings.average().toInt()}%"

  val currentKanjiList: List<Kanji>
    get() = allKanji

  val currentKanji: Kanji
    get() = allKanji.getOrElse(currentIndex.coerceIn(0, allKanji.size - 1)) { allKanji[0] }

  val currentVocabulary: List<Vocabulary>
    get() = KanjiRepository.getVocabularyForKanji(currentKanji.number)

  val isCurrentBookmarked: Boolean
    get() = bookmarkedIds.contains(currentKanji.number)

  val progress: Float
    get() = (currentIndex + 1).toFloat() / allKanji.size.toFloat()
}

class KanjiViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("kanji_module_prefs_v1", Context.MODE_PRIVATE)

  private val _uiState = MutableStateFlow(
    KanjiUiState(
      hideOtherLessons = prefs.getBoolean("hide_other_lessons", false),
      activeLessonNumber = prefs.getInt("active_lesson_number", 1)
    )
  )
  val uiState: StateFlow<KanjiUiState> = _uiState.asStateFlow()

  private val ttsHelper = JapaneseTtsHelper(application)

  fun toggleHideOtherLessons() {
    val next = !_uiState.value.hideOtherLessons
    prefs.edit().putBoolean("hide_other_lessons", next).apply()
    _uiState.update { it.copy(hideOtherLessons = next) }
  }

  fun selectLevel(level: String) {
    _uiState.update { state ->
      val lessonsInLevel = KanjiRepository.getLessonsForLevel(level)
      val nextActiveNum = if (lessonsInLevel.any { it.lessonNumber == state.activeLessonNumber }) {
        state.activeLessonNumber
      } else {
        lessonsInLevel.firstOrNull()?.lessonNumber ?: 1
      }
      state.copy(selectedLevel = level, activeLessonNumber = nextActiveNum)
    }
  }

  fun selectFilter(filter: String) {
    _uiState.update { it.copy(selectedFilter = filter) }
  }

  fun openHomeScreen() {
    _uiState.update {
      it.copy(
        isHomeScreenOpen = true,
        isLessonCompleted = false,
        isPracticeWritingScreenOpen = false,
        isReadingScreenOpen = false,
        isQuizScreenOpen = false
      )
    }
  }

  fun openReadingScreen() {
    _uiState.update {
      it.copy(
        isHomeScreenOpen = false,
        isReadingScreenOpen = true,
        isQuizScreenOpen = false,
        isLessonCompleted = false,
        isPracticeWritingScreenOpen = false
      )
    }
  }

  fun closeReadingScreen() {
    _uiState.update {
      it.copy(
        isReadingScreenOpen = false,
        isHomeScreenOpen = true
      )
    }
  }

  fun openQuizScreen() {
    _uiState.update {
      it.copy(
        isHomeScreenOpen = false,
        isQuizScreenOpen = true,
        isReadingScreenOpen = false,
        isLessonCompleted = false,
        isPracticeWritingScreenOpen = false
      )
    }
  }

  fun closeQuizScreen() {
    _uiState.update {
      it.copy(
        isQuizScreenOpen = false,
        isHomeScreenOpen = true
      )
    }
  }

  fun recordQuizResult(scorePercent: Int, practicedIds: Set<Int>) {
    _uiState.update { state ->
      state.copy(
        accuracyReadings = state.accuracyReadings + scorePercent,
        practicedKanjiIds = state.practicedKanjiIds + practicedIds
      )
    }
  }

  fun startLesson(lesson: KanjiLesson, startKanjiIndex: Int = 0) {
    prefs.edit().putInt("active_lesson_number", lesson.lessonNumber).apply()
    val firstKanjiId = lesson.kanjiList.getOrNull(startKanjiIndex)?.number
    _uiState.update { state ->
      state.copy(
        isHomeScreenOpen = false,
        activeLessonNumber = lesson.lessonNumber,
        currentLessonId = lesson.id,
        selectedLevel = lesson.level,
        allKanji = lesson.kanjiList,
        currentIndex = startKanjiIndex.coerceIn(0, lesson.kanjiList.size - 1),
        isLessonCompleted = false,
        isPracticeWritingScreenOpen = false,
        previewLesson = null,
        isSearchSheetOpen = false,
        practicedKanjiIds = if (firstKanjiId != null) state.practicedKanjiIds + firstKanjiId else state.practicedKanjiIds
      )
    }
  }

  fun openLessonPreview(lesson: KanjiLesson) {
    _uiState.update { it.copy(previewLesson = lesson) }
  }

  fun closeLessonPreview() {
    _uiState.update { it.copy(previewLesson = null) }
  }

  fun setSearchSheetOpen(open: Boolean) {
    _uiState.update { it.copy(isSearchSheetOpen = open, searchQuery = if (!open) "" else it.searchQuery) }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun selectKanjiFromSearch(kanji: Kanji, openWritingDirectly: Boolean = false) {
    val lesson = KanjiRepository.allLessons.find { l -> l.kanjiList.any { it.number == kanji.number } }
      ?: KanjiRepository.allLessons.first()
    val idx = lesson.kanjiList.indexOfFirst { it.number == kanji.number }.coerceAtLeast(0)
    prefs.edit().putInt("active_lesson_number", lesson.lessonNumber).apply()
    _uiState.update { state ->
      state.copy(
        isHomeScreenOpen = false,
        isSearchSheetOpen = false,
        previewLesson = null,
        activeLessonNumber = lesson.lessonNumber,
        currentLessonId = lesson.id,
        selectedLevel = lesson.level,
        allKanji = lesson.kanjiList,
        currentIndex = idx,
        isLessonCompleted = false,
        isPracticeWritingScreenOpen = openWritingDirectly,
        practicedKanjiIds = state.practicedKanjiIds + kanji.number
      )
    }
  }

  fun selectKanji(index: Int) {
    _uiState.update { state ->
      val safeIndex = index.coerceIn(0, state.allKanji.size - 1)
      val kanjiId = state.allKanji.getOrNull(safeIndex)?.number
      state.copy(
        currentIndex = safeIndex,
        isLessonCompleted = false,
        isPracticeWritingScreenOpen = false,
        practicedKanjiIds = if (kanjiId != null) state.practicedKanjiIds + kanjiId else state.practicedKanjiIds
      )
    }
  }

  fun nextKanji() {
    _uiState.update { state ->
      if (state.currentIndex < state.allKanji.size - 1) {
        val nextIdx = state.currentIndex + 1
        val nextId = state.allKanji.getOrNull(nextIdx)?.number
        state.copy(
          currentIndex = nextIdx,
          practicedKanjiIds = if (nextId != null) state.practicedKanjiIds + nextId else state.practicedKanjiIds,
          accuracyReadings = if (state.isPracticeWritingScreenOpen) state.accuracyReadings + 96 else state.accuracyReadings
        )
      } else {
        // Reached the end: mark lesson completed and show overview screen
        val allLessonKanjiIds = state.allKanji.map { it.number }.toSet()
        val prevBest = state.lessonBestScores[state.currentLessonId] ?: 0
        val newBest = maxOf(prevBest, 98)
        state.copy(
          isLessonCompleted = true,
          isPracticeWritingScreenOpen = false,
          completedLessonIds = state.completedLessonIds + state.currentLessonId,
          lessonBestScores = state.lessonBestScores + (state.currentLessonId to newBest),
          practicedKanjiIds = state.practicedKanjiIds + allLessonKanjiIds,
          accuracyReadings = state.accuracyReadings + 98
        )
      }
    }
  }

  fun previousKanji() {
    _uiState.update { state ->
      if (state.currentIndex > 0) {
        state.copy(currentIndex = state.currentIndex - 1)
      } else {
        state
      }
    }
  }

  fun completeLesson() {
    _uiState.update { state ->
      val allLessonKanjiIds = state.allKanji.map { it.number }.toSet()
      val prevBest = state.lessonBestScores[state.currentLessonId] ?: 0
      val newBest = maxOf(prevBest, 98)
      state.copy(
        isLessonCompleted = true,
        isPracticeWritingScreenOpen = false,
        completedLessonIds = state.completedLessonIds + state.currentLessonId,
        lessonBestScores = state.lessonBestScores + (state.currentLessonId to newBest),
        practicedKanjiIds = state.practicedKanjiIds + allLessonKanjiIds,
        accuracyReadings = state.accuracyReadings + 98
      )
    }
  }

  fun restartLesson() {
    _uiState.update { it.copy(isLessonCompleted = false, currentIndex = 0, isPracticeWritingScreenOpen = false) }
  }

  fun returnToLesson() {
    _uiState.update { it.copy(isLessonCompleted = false, isPracticeWritingScreenOpen = false) }
  }

  fun openPracticeWriting() {
    _uiState.update { state ->
      state.copy(
        isPracticeWritingScreenOpen = true,
        practicedKanjiIds = state.practicedKanjiIds + state.currentKanji.number
      )
    }
  }

  fun closePracticeWriting() {
    _uiState.update { it.copy(isPracticeWritingScreenOpen = false) }
  }

  fun startNextLesson() {
    _uiState.update { state ->
      val lessons = KanjiRepository.allLessons
      val currentIdx = lessons.indexOfFirst { it.id == state.currentLessonId }
      val nextLesson = if (currentIdx >= 0 && currentIdx < lessons.size - 1) {
        lessons[currentIdx + 1]
      } else {
        lessons.firstOrNull() ?: state.currentLesson
      }
      prefs.edit().putInt("active_lesson_number", nextLesson.lessonNumber).apply()
      state.copy(
        activeLessonNumber = nextLesson.lessonNumber,
        selectedLevel = nextLesson.level,
        currentLessonId = nextLesson.id,
        allKanji = nextLesson.kanjiList,
        isLessonCompleted = false,
        currentIndex = 0,
        isPracticeWritingScreenOpen = false,
        practicedKanjiIds = state.practicedKanjiIds + (nextLesson.kanjiList.firstOrNull()?.number ?: 1)
      )
    }
  }

  fun toggleBookmarkForKanji(kanjiNumber: Int) {
    _uiState.update { state ->
      val updated = if (state.bookmarkedIds.contains(kanjiNumber)) {
        state.bookmarkedIds - kanjiNumber
      } else {
        state.bookmarkedIds + kanjiNumber
      }
      state.copy(bookmarkedIds = updated)
    }
  }

  fun toggleBookmark() {
    toggleBookmarkForKanji(_uiState.value.currentKanji.number)
  }

  fun setKanjiSelectorVisible(visible: Boolean) {
    _uiState.update { it.copy(showKanjiSelectorSheet = visible) }
  }

  fun playAudio(text: String) {
    ttsHelper.speak(text)
    viewModelScope.launch {
      _uiState.update { it.copy(currentlyPlayingText = text) }
      delay(1200)
      _uiState.update {
        if (it.currentlyPlayingText == text) it.copy(currentlyPlayingText = null) else it
      }
    }
  }

  fun playAllKunReadings() {
    val kanji = _uiState.value.currentKanji
    val text = kanji.kunReadings.joinToString("、")
    playAudio(text)
  }

  fun playAllOnReadings() {
    val kanji = _uiState.value.currentKanji
    val text = kanji.onReadingsDisplay.joinToString("、")
    playAudio(text)
  }

  override fun onCleared() {
    super.onCleared()
    ttsHelper.shutdown()
  }
}
