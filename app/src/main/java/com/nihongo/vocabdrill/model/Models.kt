package com.nihongo.vocabdrill.model

/**
 * Domain models for Nihongo Vocabulary Drill.
 */

data class VocabItem(
    val id: String,
    val lessonNumber: Int,
    val kanji: String,
    val kana: String,
    val romaji: String,
    val english: String,
    val category: String,
    val exampleJapanese: String = "",
    val exampleEnglish: String = "",
    val isFavorite: Boolean = false
) {
    val displayJapanese: String
        get() = kana
}

data class LessonInfo(
    val lessonNumber: Int,
    val title: String,
    val japaneseTitle: String,
    val description: String,
    val vocabCount: Int,
    val grammarFocus: String = ""
)

data class LessonProgress(
    val lessonNumber: Int,
    val isCompleted: Boolean = false,
    val bestScore: Int = 0,
    val attemptsCount: Int = 0,
    val lastPracticedDate: String = ""
) {
    val gradeRank: String
        get() = when {
            bestScore == 100 -> "S"
            bestScore >= 90 -> "A"
            bestScore >= 75 -> "B"
            bestScore >= 60 -> "C"
            bestScore > 0 -> "D"
            else -> "-"
        }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class DrillDirection(val label: String, val description: String, val badge: String? = null) {
    HIRAGANA_TO_ENGLISH(
        label = "Hiragana → English",
        description = "Show Japanese word, choose English meaning"
    ),
    ENGLISH_TO_HIRAGANA(
        label = "English → Hiragana",
        description = "Show English meaning, choose Japanese word"
    ),
    MIX_BOTH(
        label = "Mix (Both Directions)",
        description = "Alternates questions in both directions",
        badge = "Recommended"
    ),
    KANJI_TO_KANA(
        label = "Kanji → Reading",
        description = "See Kanji, choose the accurate Hiragana reading"
    );

    companion object {
        val JAPANESE_TO_ENGLISH = HIRAGANA_TO_ENGLISH
        val ENGLISH_TO_JAPANESE = ENGLISH_TO_HIRAGANA
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val drillDirection: DrillDirection = DrillDirection.HIRAGANA_TO_ENGLISH,
    val showRomaji: Boolean = true,
    val soundEffects: Boolean = true,
    val vibration: Boolean = false,
    val autoPlaySpeech: Boolean = true,
    val hideMcqOptions: Boolean = false,
    val encouragement: Boolean = true,
    val hideOtherLessons: Boolean = false,
    val currentLessonNumber: Int = 1,
    val questionsPerDrill: Int = 0,
    val speechSpeed: Float = 0.9f
) {
    val showRomajiHints: Boolean get() = showRomaji
    val autoPronounceAudio: Boolean get() = autoPlaySpeech
    val feedbackChimes: Boolean get() = soundEffects
}

data class QuizQuestion(
    val questionIndex: Int,
    val vocabItem: VocabItem,
    val promptText: String,
    val promptSubtext: String? = null,
    val options: List<String>,
    val correctAnswer: String,
    val selectedAnswer: String? = null,
    val isAnswered: Boolean = false,
    val meaningRevealed: Boolean = false,
    val isRecallAnswer: Boolean = false,
    val isCorrect: Boolean = false,
    val currentStreak: Int = 0
)

data class QuizSummary(
    val lessonNumber: Int,
    val lessonTitle: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongItems: List<VocabItem>,
    val timeTakenSeconds: Long,
    val includesRecallScore: Boolean = false,
    val maxStreak: Int = 0
) {
    val accuracyPercentage: Int
        get() = if (totalQuestions > 0) ((correctCount * 100) / totalQuestions) else 0

    val gradeRank: String
        get() = when {
            accuracyPercentage == 100 -> "S"
            accuracyPercentage >= 90 -> "A"
            accuracyPercentage >= 75 -> "B"
            accuracyPercentage >= 60 -> "C"
            else -> "D"
        }

    val feedbackPhrase: String
        get() = when {
            accuracyPercentage == 100 -> "たいへん よくできました！ (Perfect score!)"
            accuracyPercentage >= 90 -> "すばらしい！ (Mastery achieved!)"
            accuracyPercentage >= 75 -> "よくできました！ (Great effort!)"
            accuracyPercentage >= 60 -> "もうすこしです！ (Keep drilling!)"
            else -> "あきらめないで！ (Practice makes progress!)"
        }
}

data class UserProgress(
    val completedLessons: Set<Int> = emptySet(),
    val lessonScores: Map<Int, Int> = emptyMap(),
    val lessonProgressMap: Map<Int, LessonProgress> = emptyMap(),
    val favoriteWordIds: Set<String> = emptySet(),
    val wrongWordIds: Set<String> = emptySet(),
    val totalPracticedQuestions: Int = 0,
    val totalCorrectAnswers: Int = 0
) {
    val overallAccuracy: Int
        get() = if (totalPracticedQuestions > 0) {
            ((totalCorrectAnswers * 100) / totalPracticedQuestions)
        } else {
            0
        }
}

sealed interface Screen {
    data object ChapterSelect : Screen
    data class Quiz(val lessonNumber: Int, val customTitle: String? = null) : Screen
    data class Completion(val summary: QuizSummary) : Screen
    data object FavoritesList : Screen
}
