package com.fitnex.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.ui.theme.Abyss
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.EmberBright
import com.fitnex.app.ui.theme.EmberDeep
import com.fitnex.app.ui.theme.Fog
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Gold
import com.fitnex.app.ui.theme.Hairline
import com.fitnex.app.ui.theme.Night
import com.fitnex.app.ui.theme.Panel

/** Standard page gutter, shared by every scrollable screen. */
object Metrics {
    val Gutter = 20.dp
    val BottomBreathing = 132.dp
}

/**
 * Atmospheric ground: an ember pool of light behind the header and a cold teal
 * counter-glow low on the page, the way a game lobby lights its hero panel.
 */
@Composable
fun Atmosphere(modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Night)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Ember.copy(alpha = 0.30f), Color.Transparent),
                        center = Offset(size.width * 0.18f, size.height * 0.01f),
                        radius = size.minDimension * 1.15f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Abyss.copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(size.width * 1.02f, size.height * 0.82f),
                        radius = size.minDimension * 1.05f,
                    ),
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.62f to Night.copy(alpha = 0.28f),
                        1f to Night.copy(alpha = 0.85f),
                    ),
                )
            },
    ) {
        content()
    }
}

@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = FogDim,
    style: TextStyle = MaterialTheme.typography.labelSmall,
) {
    Text(text = text.uppercase(), modifier = modifier, style = style, color = color)
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    meta: String? = null,
    metaColor: Color = FogDim,
    onAction: (() -> Unit)? = null,
    actionLabel: String = "VIEW ALL",
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = Bone,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        if (meta != null) {
            Spacer(Modifier.width(10.dp))
            Text(meta.uppercase(), style = MaterialTheme.typography.labelSmall, color = metaColor)
        }
        if (onAction != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onAction)
                    .padding(start = 10.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelSmall, color = Ember)
                Spacer(Modifier.width(2.dp))
                FitIcon(FitIcon.ChevronRight, tint = Ember, iconSize = 12.dp)
            }
        }
    }
}

@Composable
fun HairlineRule(modifier: Modifier = Modifier, color: Color = Hairline) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

/**
 * Rounded panel used for every grouped block: a slightly lifted surface with a
 * hairline rim and an optional warm bloom bleeding in from the top edge.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 22.dp,
    glow: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Panel.copy(alpha = 0.94f))
            .border(1.dp, if (glow == null) Hairline else glow.copy(alpha = 0.30f), shape),
        content = content,
    )
}

@Composable
fun ProgressRail(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    accent: Color = Ember,
    track: Color = Hairline,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 520),
        label = "progressRail",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(track, RoundedCornerShape(height / 2)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height)
                .background(
                    Brush.horizontalGradient(listOf(accent.copy(alpha = 0.6f), accent)),
                    RoundedCornerShape(height / 2),
                ),
        )
    }
}

/**
 * Primary action: one ember slab, sharp corners, wide-tracked label, trailing
 * glyph. Presses dip it a hair so the tap feels physical.
 */
@Composable
fun PrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: FitIcon? = FitIcon.ArrowRight,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 900f),
        label = "primaryPress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(EmberBright, Ember, Color(0xFF8E2216)),
                ),
            )
            .border(1.dp, Bone.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Bone,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = FogDim,
            ),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
            elevation = null,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.4.sp),
                    fontWeight = FontWeight.Bold,
                )
                if (icon != null) FitIcon(icon, tint = Bone.copy(alpha = 0.92f), iconSize = 18.dp)
            }
        }
    }
}

@Composable
fun SecondaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: FitIcon? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 900f),
        label = "secondaryPress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Hairline, RoundedCornerShape(18.dp)),
    ) {
        Button(
            onClick = onClick,
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Bone,
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            elevation = null,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    FitIcon(icon, tint = Fog, iconSize = 16.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                    color = Fog,
                )
            }
        }
    }
}

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Ember,
    filled: Boolean = false,
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (filled) color else color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = if (filled) 0.9f else 0.30f), shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.3.sp),
            color = if (filled) Night else color,
        )
    }
}

@Composable
fun StatValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    valueColor: Color = Bone,
    labelColor: Color = FogDim,
) {
    Column(modifier = modifier) {
        Text(value, style = valueStyle, color = valueColor, maxLines = 1)
        Spacer(Modifier.height(5.dp))
        Eyebrow(label, color = labelColor)
    }
}

/** Stats separated by hairlines instead of a wall of tiny cards. */
@Composable
fun StatStrip(
    modifier: Modifier = Modifier,
    items: List<Triple<String, String, Color>>,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        items.forEachIndexed { index, (label, value, tint) ->
            Column(Modifier.weight(1f)) {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = tint,
                    maxLines = 1,
                )
                Spacer(Modifier.height(5.dp))
                Eyebrow(label, color = FogDim)
            }
            if (index != items.lastIndex) {
                Box(
                    Modifier
                        .padding(horizontal = 14.dp)
                        .width(1.dp)
                        .height(34.dp)
                        .background(Hairline),
                )
            }
        }
    }
}

/** Rank medallion: ticked bezel, ember progress arc, level in the core. */
@Composable
fun RankSigil(
    level: Int,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    accent: Color = Ember,
    progress: Float = 0f,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2f
            drawCircle(accent.copy(alpha = 0.07f), radius = radius, center = center)
            drawCircle(Hairline, radius = radius - 1f, center = center, style = Stroke(1f))
            drawCircle(
                accent.copy(alpha = 0.30f),
                radius = radius - 7.dp.toPx(),
                center = center,
                style = Stroke(1f),
            )
            val ticks = 48
            repeat(ticks) { index ->
                val major = index % 12 == 0
                val angle = Math.toRadians((index * (360f / ticks) - 90f).toDouble())
                val outer = radius - 3.dp.toPx()
                val inner = outer - if (major) 6.dp.toPx() else 3.dp.toPx()
                drawLine(
                    color = if (major) accent.copy(alpha = 0.55f) else Hairline,
                    start = Offset(
                        center.x + (outer * Math.cos(angle)).toFloat(),
                        center.y + (outer * Math.sin(angle)).toFloat(),
                    ),
                    end = Offset(
                        center.x + (inner * Math.cos(angle)).toFloat(),
                        center.y + (inner * Math.sin(angle)).toFloat(),
                    ),
                    strokeWidth = if (major) 1.6.dp.toPx() else 1f,
                )
            }
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(1.5.dp.toPx(), 1.5.dp.toPx()),
                size = Size(this.size.width - 3.dp.toPx(), this.size.height - 3.dp.toPx()),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = level.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = (size.value * 0.30f).sp),
                color = Bone,
            )
            Eyebrow("Level", color = FogDim)
        }
    }
}

@Composable
fun IconPlate(
    icon: FitIcon,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = Bone,
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.38f))
            .border(1.dp, Hairline.copy(alpha = 0.9f), CircleShape)
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        FitIcon(icon, tint = tint, iconSize = 18.dp)
    }
}

@Composable
fun GoldRule(modifier: Modifier = Modifier, color: Color = Gold) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color.copy(alpha = 0.32f)))
}

/** Thin vertical divider used between inline stats. */
@Composable
fun VerticalHairline(height: Dp = 32.dp, modifier: Modifier = Modifier) {
    Box(modifier.padding(horizontal = 14.dp).width(1.dp).height(height).background(Hairline))
}

@Composable
fun CenteredQuote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
        color = Fog.copy(alpha = 0.75f),
        textAlign = TextAlign.Center,
    )
}
