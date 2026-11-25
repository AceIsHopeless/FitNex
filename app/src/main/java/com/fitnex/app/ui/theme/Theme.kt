@file:OptIn(ExperimentalTextApi::class)

package com.fitnex.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.R

// ---------------------------------------------------------------------------
// Palette. Near-black ground, warm bone text, one restrained ember accent and a
// whisper of old gold reserved for progression (rank, rewards, titles).
// ---------------------------------------------------------------------------
val Night = Color(0xFF07070A)
val Panel = Color(0xFF0E1014)
val PanelRaised = Color(0xFF161A21)
val Hairline = Color(0xFF252932)
val Bone = Color(0xFFF2EFE8)
val Fog = Color(0xFF92939E)
val FogDim = Color(0xFF5D5F69)
val Ember = Color(0xFFC6402F)
val EmberBright = Color(0xFFE45B4A)
val EmberDeep = Color(0xFF5C1418)
val Gold = Color(0xFFC4A268)
val Jade = Color(0xFF7FA88B)

// Keep the original names working for any code that still references them.
val Ash = Night
val Charcoal = Panel
val Steel = PanelRaised
val Line = Hairline
val Muted = Fog
val EmberRed = Ember
val BloodRed = EmberDeep

private val DisplayFamily = FontFamily(
    Font(
        R.font.cinzel,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.cinzel,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.cinzel,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

private val TextFamily = FontFamily(
    Font(
        R.font.inter,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.inter,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.inter,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.inter,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

private val FitNexColors = darkColorScheme(
    primary = Ember,
    onPrimary = Bone,
    primaryContainer = EmberDeep,
    onPrimaryContainer = Bone,
    secondary = Gold,
    onSecondary = Night,
    tertiary = Jade,
    background = Night,
    onBackground = Bone,
    surface = Panel,
    onSurface = Bone,
    surfaceVariant = PanelRaised,
    onSurfaceVariant = Fog,
    outline = Hairline,
    outlineVariant = Hairline,
    error = EmberBright,
    onError = Bone,
)

private val FitNexTypography = Typography(
    // Cinzel: engraving-style serif for names, ranks and heroic numerals.
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.5.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 27.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.4.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.3.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.4.sp,
    ),
    // Inter: everything structural.
    titleLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    // Caption system: wide tracking, uppercase, never larger than 11sp so the
    // eye reads them as labels rather than content.
    labelLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.8.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        lineHeight = 13.sp,
        letterSpacing = 2.1.sp,
    ),
)

private val FitNexShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun FitNexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FitNexColors,
        typography = FitNexTypography,
        shapes = FitNexShapes,
        content = content,
    )
}
