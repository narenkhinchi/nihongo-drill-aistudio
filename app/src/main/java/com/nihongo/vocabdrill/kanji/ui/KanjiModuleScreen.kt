package com.nihongo.vocabdrill.kanji.ui

import androidx.compose.runtime.Composable
import com.example.ui.screens.KanjiLearningScreen

@Composable
fun KanjiModuleScreen(
    onBackToVocabulary: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    onToggleTheme: (() -> Unit)? = null
) {
    KanjiLearningScreen(
        onBackToVocabulary = onBackToVocabulary,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme
    )
}
