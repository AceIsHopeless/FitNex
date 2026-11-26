package com.fitnex.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.EmberDeep
import com.fitnex.app.ui.theme.Hairline

/**
 * Segmented boss health. Each notch drains individually so a rep visibly bites
 * into the bar instead of nudging one smooth pill, and the leading segment
 * drains partially for a continuous read.
 */
@Composable
fun HealthBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    segments: Int = 20,
    accent: Color = Ember,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 320),
        label = "bossHealth",
    )
    Row(
        modifier = modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(segments) { index ->
            val segmentFill = (animated * segments - index).coerceIn(0f, 1f)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Hairline.copy(alpha = 0.55f)),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(segmentFill)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.verticalGradient(listOf(accent, EmberDeep)),
                        ),
                )
            }
        }
    }
}
