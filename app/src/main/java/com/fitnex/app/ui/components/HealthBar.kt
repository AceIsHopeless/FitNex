package com.fitnex.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fitnex.app.ui.theme.BloodRed
import com.fitnex.app.ui.theme.EmberRed
import com.fitnex.app.ui.theme.Steel

@Composable
fun HealthBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    val target = fraction.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 280),
        label = "bossHealth",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Steel, RoundedCornerShape(1.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .background(
                    Brush.horizontalGradient(listOf(BloodRed, EmberRed)),
                    RoundedCornerShape(1.dp),
                ),
        )
    }
}
