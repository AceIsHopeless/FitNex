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
// Palette. Near-black ground lit by two glows: a warm ember pool behind the
// header and a cold teal counter-glow low on the page. Bone text, gold only for
// progression. No violet anywhere.
// ---------------------------------------------------------------------------
val Night = Color(0xFF07080B)
val Panel = Color(0xFF10131A)
val PanelRaised = Color(0xFF191D26)
val Hairline = Color(0xFF272C38)
val Bone = Color(0xFFF5F2EC)
val Fog = Color(0xFF9B9DA9)
val FogDim = Color(0xFF62646F)
val Ember = Color(0xFFE0452C)
val EmberBright = Color(0xFFFF6B47)
val EmberDeep = Color(0xFF3C0E0B)
val Gold = Color(0xFFE9C27C)
val Jade = Color(0xFF63C3A1)

/** Cold counter-glow used by the page atmosphere. Deliberately teal, not violet. */
val Abyss = Color(0xFF123C49)

// Keep the original names working for any code that still references them.
val Ash = Night
val Charcoal = Panel
val Steel = PanelRaised
val Line = Hairline
val Muted = Fog
val EmberRed = Ember
val BloodRed = EmberDeep

/** Engraved wordmark face; used only for the FITNEX lockup. */
private val WordmarkFamily = FontFamily(
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

val Wordmark = WordmarkFamily

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
    // Heavy, tightly tracked sans numerals and names: the loud game headline.
    displayLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.5.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
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
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    // Caption system: uppercase, tracked, never larger than 12 sp so the eye
    // reads them as labels rather than content.
    labelLarge = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = TextFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 9.5.sp,
        lineHeight = 13.sp,
        letterSpacing = 1.6.sp,
    ),
)

// Rounded, card-first geometry matches the glossy game-dashboard language.
private val FitNexShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
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
