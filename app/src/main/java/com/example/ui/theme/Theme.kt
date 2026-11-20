package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = LightBrandPurple,
  onPrimary = Color.White,
  primaryContainer = LightBrandPurpleLight,
  onPrimaryContainer = LightBrandPurpleDark,
  secondary = LightBrandPurple,
  onSecondary = Color.White,
  secondaryContainer = LightBrandPurpleLight,
  onSecondaryContainer = LightBrandPurpleDark,
  tertiary = LightBrandPurpleDark,
  background = LightMainBackground,
  onBackground = LightPrimaryText,
  surface = LightCardSurface,
  onSurface = LightPrimaryText,
  surfaceVariant = LightMainBackground,
  onSurfaceVariant = LightSecondaryText,
  outline = LightBorderOutline,
  outlineVariant = LightBorderSubtle,
)

private val DarkColorScheme = darkColorScheme(
  primary = DarkBrandPurple,
  onPrimary = Color.White,
  primaryContainer = DarkBrandPurpleLight,
  onPrimaryContainer = DarkBrandPurpleDark,
  secondary = DarkBrandPurple,
  onSecondary = Color.White,
  background = DarkMainBackground,
  onBackground = DarkPrimaryText,
  surface = DarkCardSurface,
  onSurface = DarkPrimaryText,
  surfaceVariant = DarkCardSurfaceElevated,
  onSurfaceVariant = DarkSecondaryText,
  outline = DarkBorderOutline,
  outlineVariant = DarkBorderSubtle,
)

@Composable
fun NihongoDrillTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // The app design handoff specifies the clean light theme with #5034FF
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
