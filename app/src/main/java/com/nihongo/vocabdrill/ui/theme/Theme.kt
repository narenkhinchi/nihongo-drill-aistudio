package com.nihongo.vocabdrill.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.nihongo.vocabdrill.model.ThemeMode

// Japanese Indigo Dark Theme
private val IndigoDarkColorScheme = darkColorScheme(
    primary = IndigoBlueLight,
    onPrimary = Color(0xFF0A101D),
    primaryContainer = IndigoBlueDark,
    onPrimaryContainer = Color.White,
    secondary = SkyCyanAccent,
    onSecondary = Color(0xFF081628),
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = CorrectGreen,
    onTertiary = Color.Black,
    tertiaryContainer = CorrectGreenBg,
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = IndigoDarkBg,
    onBackground = Slate50,
    surface = IndigoSurface,
    onSurface = Slate50,
    surfaceVariant = IndigoSurfaceCard,
    onSurfaceVariant = Slate300,
    outline = Slate600,
    outlineVariant = Color(0xFF263859),
    error = WrongCrimson,
    onError = Color.White
)

// Japanese Indigo Light Theme
private val IndigoLightColorScheme = lightColorScheme(
    primary = IndigoBlueVivid,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = IndigoBlueDark,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF0F4F8),
    onSurfaceVariant = Slate700,
    outline = Slate300,
    outlineVariant = Slate200,
    error = WrongCrimson,
    onError = Color.White
)

@Composable
fun NihongoVocabTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> IndigoDarkColorScheme
        else -> IndigoLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
