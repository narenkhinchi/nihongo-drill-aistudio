package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Static Light Palette constants (for non-composable Theme definitions)
internal val LightBrandPurple = Color(0xFF5034FF)
internal val LightBrandPurpleLight = Color(0xFFECE8FF)
internal val LightBrandPurpleVeryLight = Color(0xFFF3F1FF)
internal val LightBrandPurpleDark = Color(0xFF381ED9)

internal val LightMainBackground = Color(0xFFF7F7FB)
internal val LightCardSurface = Color(0xFFFFFFFF)
internal val LightPrimaryText = Color(0xFF171717)
internal val LightSecondaryText = Color(0xFF777783)
internal val LightBorderOutline = Color(0xFFE8E8EF)
internal val LightBorderSubtle = Color(0xFFF0F0F5)

// Dark Palette constants aligned with Vocab's Deep Midnight Indigo theme
internal val DarkBrandPurple = Color(0xFF6B54FF)
internal val DarkBrandPurpleLight = Color(0xFF1F224A)
internal val DarkBrandPurpleVeryLight = Color(0xFF161D35)
internal val DarkBrandPurpleDark = Color(0xFFC4B5FD)

internal val DarkMainBackground = Color(0xFF090D16)
internal val DarkCardSurface = Color(0xFF0F172A)
internal val DarkCardSurfaceElevated = Color(0xFF17233D)
internal val DarkPrimaryText = Color(0xFFF8FAFC)
internal val DarkSecondaryText = Color(0xFF94A3B8)
internal val DarkBorderOutline = Color(0xFF263859)
internal val DarkBorderSubtle = Color(0xFF1E293B)

val isKanjiDarkTheme: Boolean
  @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.5f

// Theme-aware Kanji Module Colors (automatically follow NihongoVocabTheme Light/Dark mode)
val BrandPurple: Color
  @Composable get() = if (isKanjiDarkTheme) DarkBrandPurple else LightBrandPurple

val BrandPurpleLight: Color
  @Composable get() = if (isKanjiDarkTheme) DarkBrandPurpleLight else LightBrandPurpleLight

val BrandPurpleVeryLight: Color
  @Composable get() = if (isKanjiDarkTheme) DarkBrandPurpleVeryLight else LightBrandPurpleVeryLight

val BrandPurpleDark: Color
  @Composable get() = if (isKanjiDarkTheme) DarkBrandPurpleDark else LightBrandPurpleDark

val MainBackground: Color
  @Composable get() = if (isKanjiDarkTheme) DarkMainBackground else LightMainBackground

val CardSurface: Color
  @Composable get() = if (isKanjiDarkTheme) DarkCardSurface else LightCardSurface

val CardSurfaceElevated: Color
  @Composable get() = if (isKanjiDarkTheme) DarkCardSurfaceElevated else Color(0xFFFAFAFD)

val PrimaryText: Color
  @Composable get() = if (isKanjiDarkTheme) DarkPrimaryText else LightPrimaryText

val SecondaryText: Color
  @Composable get() = if (isKanjiDarkTheme) DarkSecondaryText else LightSecondaryText

val BorderOutline: Color
  @Composable get() = if (isKanjiDarkTheme) DarkBorderOutline else LightBorderOutline

val BorderSubtle: Color
  @Composable get() = if (isKanjiDarkTheme) DarkBorderSubtle else LightBorderSubtle

val SoftPurpleBorder: Color
  @Composable get() = if (isKanjiDarkTheme) Color(0xFF343B6E) else Color(0xFFDCD6FE)

val SoftPurplePillBg: Color
  @Composable get() = if (isKanjiDarkTheme) Color(0xFF1C2342) else Color(0xFFEEEBFA)

val AccentStrokeRed = Color(0xFFE53935)
val SuccessGreen = Color(0xFF2E7D32)
val WarningOrange = Color(0xFFED6C02)
