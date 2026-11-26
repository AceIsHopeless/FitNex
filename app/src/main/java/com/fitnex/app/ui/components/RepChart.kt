package com.fitnex.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.domain.DailyReps
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.EmberBright
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Hairline

/**
 * Seven-day rep rhythm. Today's column sits brighter than the rest, quiet zero
 * days stay as stubs so the week still reads as a week.
 */
@Composable
fun WeeklyRepChart(
    days: List<DailyReps>,
    modifier: Modifier = Modifier,
    area: Dp = 84.dp,
) {
    if (days.isEmpty()) return
    val peak = (days.maxOfOrNull { it.pushUps } ?: 0).coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth().height(area + 40.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        days.forEachIndexed { index, day ->
            val isToday = index == days.lastIndex
            val target = if (day.pushUps > 0) {
                (area.value * day.pushUps.toFloat() / peak.toFloat()).coerceAtLeast(5f).dp
            } else {
                3.dp
            }
            val barHeight by animateDpAsState(
                targetValue = target,
                animationSpec = tween(durationMillis = 620, delayMillis = index * 40),
                label = "repBar",
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (day.pushUps > 0) day.pushUps.toString() else "·",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday && day.pushUps > 0) Bone else FogDim,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(area)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Hairline.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (day.pushUps > 0) {
                                    Brush.verticalGradient(
                                        listOf(
                                            if (isToday) EmberBright else Ember.copy(alpha = 0.75f),
                                            if (isToday) Ember else Ember.copy(alpha = 0.25f),
                                        ),
                                    )
                                } else {
                                    Brush.verticalGradient(listOf(Hairline, Hairline))
                                },
                            ),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = day.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                    color = if (isToday) Bone else FogDim,
                )
            }
        }
    }
}
