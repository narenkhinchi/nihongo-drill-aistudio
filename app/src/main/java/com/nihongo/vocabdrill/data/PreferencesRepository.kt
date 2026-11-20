package com.nihongo.vocabdrill.data

import android.content.Context
import android.content.SharedPreferences
import com.nihongo.vocabdrill.model.AppSettings
import com.nihongo.vocabdrill.model.DrillDirection
import com.nihongo.vocabdrill.model.LessonProgress
import com.nihongo.vocabdrill.model.ThemeMode
import com.nihongo.vocabdrill.model.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reactive repository backed by SharedPreferences and StateFlow.
 */
class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _progress = MutableStateFlow(loadProgress())
    val progress: StateFlow<UserProgress> = _progress.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeModeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        val themeMode = runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.LIGHT)

        val directionStr = prefs.getString(KEY_DRILL_DIRECTION, DrillDirection.JAPANESE_TO_ENGLISH.name)
            ?: DrillDirection.JAPANESE_TO_ENGLISH.name
        val direction = runCatching { DrillDirection.valueOf(directionStr) }
            .getOrDefault(DrillDirection.JAPANESE_TO_ENGLISH)

        val showRomaji = prefs.getBoolean(KEY_SHOW_ROMAJI, true)
        val soundEffects = prefs.getBoolean(KEY_SOUND_EFFECTS, true)
        val vibration = prefs.getBoolean(KEY_VIBRATION, true)
        val autoPlaySpeech = prefs.getBoolean(KEY_AUTO_PLAY_SPEECH, true)
        val hideMcqOptions = prefs.getBoolean(KEY_HIDE_MCQ_OPTIONS, false)
        val questionsPerDrill = prefs.getInt(KEY_QUESTIONS_PER_DRILL, 10)
        val speechSpeed = prefs.getFloat(KEY_SPEECH_SPEED, 0.9f)

        return AppSettings(
            themeMode = themeMode,
            drillDirection = if (direction == DrillDirection.KANJI_TO_KANA) DrillDirection.HIRAGANA_TO_ENGLISH else direction,
            showRomaji = showRomaji,
            soundEffects = soundEffects,
            vibration = false,
            autoPlaySpeech = autoPlaySpeech,
            hideMcqOptions = hideMcqOptions,
            encouragement = prefs.getBoolean("encouragement", true),
            hideOtherLessons = prefs.getBoolean("hide_other_lessons", false),
            currentLessonNumber = prefs.getInt("current_lesson", 1),
            questionsPerDrill = 0,
            speechSpeed = speechSpeed
        )
    }

    private fun savedLessonNumbers(): Set<Int> = (1..25).toSet() +
        prefs.all.keys.mapNotNull { key ->
            if (key.startsWith("${KEY_SCORE_PREFIX}_") || key.startsWith("${KEY_ATTEMPTS_PREFIX}_") ||
                key.startsWith("${KEY_DATE_PREFIX}_")) key.substringAfterLast('_').toIntOrNull() else null
        } + (prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet()) ?: emptySet()).mapNotNull { it.toIntOrNull() }

    private fun loadProgress(): UserProgress {
        val completedStrSet = prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet()) ?: emptySet()
        val completed = completedStrSet.mapNotNull { it.toIntOrNull() }.toSet()

        val scores = mutableMapOf<Int, Int>()
        val progressMap = mutableMapOf<Int, LessonProgress>()

        for (i in savedLessonNumbers()) {
            val score = prefs.getInt("${KEY_SCORE_PREFIX}_$i", -1)
            val attempts = prefs.getInt("${KEY_ATTEMPTS_PREFIX}_$i", 0)
            val lastDate = prefs.getString("${KEY_DATE_PREFIX}_$i", "") ?: ""

            if (score >= 0) {
                scores[i] = score
            }

            progressMap[i] = LessonProgress(
                lessonNumber = i,
                isCompleted = completed.contains(i),
                bestScore = if (score >= 0) score else 0,
                attemptsCount = attempts,
                lastPracticedDate = lastDate
            )
        }

        val favorites = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        val practiced = prefs.getInt(KEY_TOTAL_PRACTICED, 0)
        val correct = prefs.getInt(KEY_TOTAL_CORRECT, 0)

        return UserProgress(
            completedLessons = completed,
            lessonScores = scores,
            lessonProgressMap = progressMap,
            favoriteWordIds = favorites.toSet(),
            wrongWordIds = prefs.getStringSet("wrong_words", emptySet())!!.toSet(),
            totalPracticedQuestions = practiced,
            totalCorrectAnswers = correct
        )
    }

    fun setCurrentLesson(number: Int) {
        prefs.edit().putInt("current_lesson", number).apply()
        _settings.value = _settings.value.copy(currentLessonNumber = number)
    }
    fun toggleOtherLessons() {
        val hide = !_settings.value.hideOtherLessons
        prefs.edit().putBoolean("hide_other_lessons", hide).apply()
        _settings.value = _settings.value.copy(hideOtherLessons = hide)
    }
    fun toggleEncouragement(enabled: Boolean) {
        prefs.edit().putBoolean("encouragement", enabled).apply()
        _settings.value = _settings.value.copy(encouragement = enabled)
    }

    fun recordWordAnswer(id: String, correct: Boolean) {
        val wrong = _progress.value.wrongWordIds.toMutableSet()
        if (correct) wrong.remove(id) else wrong.add(id)
        prefs.edit().putStringSet("wrong_words", wrong).apply()
        _progress.value = _progress.value.copy(wrongWordIds = wrong)
    }

    fun updateThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun updateDrillDirection(direction: DrillDirection) {
        prefs.edit().putString(KEY_DRILL_DIRECTION, direction.name).apply()
        _settings.value = _settings.value.copy(drillDirection = direction)
    }

    fun toggleRomaji(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_ROMAJI, enabled).apply()
        _settings.value = _settings.value.copy(showRomaji = enabled)
    }

    fun toggleSoundEffects(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_EFFECTS, enabled).apply()
        _settings.value = _settings.value.copy(soundEffects = enabled)
    }

    fun toggleVibration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        _settings.value = _settings.value.copy(vibration = enabled)
    }

    fun toggleAutoPlaySpeech(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY_SPEECH, enabled).apply()
        _settings.value = _settings.value.copy(autoPlaySpeech = enabled)
    }

    fun toggleHideMcqOptions(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_MCQ_OPTIONS, enabled).apply()
        _settings.value = _settings.value.copy(hideMcqOptions = enabled)
    }

    fun recordQuizResult(lessonNumber: Int, accuracyPercentage: Int, totalQuestions: Int, correctQuestions: Int) {
        val current = _progress.value
        val updatedCompleted = current.completedLessons + lessonNumber
        val previousBest = current.lessonScores[lessonNumber] ?: 0
        val newBest = maxOf(previousBest, accuracyPercentage)
        val updatedScores = current.lessonScores + (lessonNumber to newBest)
        val updatedPracticed = current.totalPracticedQuestions + totalQuestions
        val updatedCorrect = current.totalCorrectAnswers + correctQuestions

        val currentProgress = current.lessonProgressMap[lessonNumber]
        val attempts = (currentProgress?.attemptsCount ?: 0) + 1
        val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())

        val updatedProgressMap = current.lessonProgressMap.toMutableMap()
        updatedProgressMap[lessonNumber] = LessonProgress(
            lessonNumber = lessonNumber,
            isCompleted = true,
            bestScore = newBest,
            attemptsCount = attempts,
            lastPracticedDate = dateStr
        )

        prefs.edit()
            .putStringSet(KEY_COMPLETED_LESSONS, updatedCompleted.map { it.toString() }.toSet())
            .putInt("${KEY_SCORE_PREFIX}_$lessonNumber", newBest)
            .putInt("${KEY_ATTEMPTS_PREFIX}_$lessonNumber", attempts)
            .putString("${KEY_DATE_PREFIX}_$lessonNumber", dateStr)
            .putInt(KEY_TOTAL_PRACTICED, updatedPracticed)
            .putInt(KEY_TOTAL_CORRECT, updatedCorrect)
            .apply()

        _progress.value = current.copy(
            completedLessons = updatedCompleted,
            lessonScores = updatedScores,
            lessonProgressMap = updatedProgressMap,
            totalPracticedQuestions = updatedPracticed,
            totalCorrectAnswers = updatedCorrect
        )
    }

    fun toggleFavorite(vocabId: String) {
        val currentFavorites = _progress.value.favoriteWordIds.toMutableSet()
        if (currentFavorites.contains(vocabId)) {
            currentFavorites.remove(vocabId)
        } else {
            currentFavorites.add(vocabId)
        }

        prefs.edit().putStringSet(KEY_FAVORITES, currentFavorites).apply()
        _progress.value = _progress.value.copy(favoriteWordIds = currentFavorites)
    }

    fun resetAllProgress() {
        val editor = prefs.edit()
            .remove(KEY_COMPLETED_LESSONS)
            .remove(KEY_FAVORITES)
            .remove("wrong_words")
            .remove(KEY_TOTAL_PRACTICED)
            .remove(KEY_TOTAL_CORRECT)

        for (i in savedLessonNumbers()) {
            editor.remove("${KEY_SCORE_PREFIX}_$i")
            editor.remove("${KEY_ATTEMPTS_PREFIX}_$i")
            editor.remove("${KEY_DATE_PREFIX}_$i")
        }
        editor.apply()

        val emptyProgressMap = (1..25).associateWith { LessonProgress(lessonNumber = it) }
        _progress.value = UserProgress(lessonProgressMap = emptyProgressMap)
    }

    fun updateQuestionsPerDrill(count: Int) {
        prefs.edit().putInt(KEY_QUESTIONS_PER_DRILL, count).apply()
        _settings.value = _settings.value.copy(questionsPerDrill = count)
    }

    fun updateSpeechSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_SPEECH_SPEED, speed).apply()
        _settings.value = _settings.value.copy(speechSpeed = speed)
    }

    companion object {
        private const val PREFS_NAME = "nihongo_book_vocab_prefs_v1"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DRILL_DIRECTION = "drill_direction"
        private const val KEY_SHOW_ROMAJI = "show_romaji"
        private const val KEY_SOUND_EFFECTS = "sound_effects"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_AUTO_PLAY_SPEECH = "auto_play_speech"
        private const val KEY_HIDE_MCQ_OPTIONS = "hide_mcq_options"
        private const val KEY_QUESTIONS_PER_DRILL = "questions_per_drill"
        private const val KEY_SPEECH_SPEED = "speech_speed"
        private const val KEY_COMPLETED_LESSONS = "completed_lessons"
        private const val KEY_SCORE_PREFIX = "score_lesson"
        private const val KEY_ATTEMPTS_PREFIX = "attempts_lesson"
        private const val KEY_DATE_PREFIX = "date_lesson"
        private const val KEY_FAVORITES = "favorite_words"
        private const val KEY_TOTAL_PRACTICED = "total_practiced"
        private const val KEY_TOTAL_CORRECT = "total_correct"
    }
}
