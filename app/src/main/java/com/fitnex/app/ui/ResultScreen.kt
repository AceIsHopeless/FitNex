package com.fitnex.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.data.GameSettings
import com.fitnex.app.domain.BossCatalog
import com.fitnex.app.ui.components.BossArt
import com.fitnex.app.ui.components.Eyebrow
import com.fitnex.app.ui.components.FitIcon
import com.fitnex.app.ui.components.GoldRule
import com.fitnex.app.ui.components.HairlineRule
import com.fitnex.app.ui.components.PrimaryAction
import com.fitnex.app.ui.components.SecondaryAction
import com.fitnex.app.ui.components.StatStrip
import com.fitnex.app.ui.feedback.GameFeedback
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.Fog
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Gold
import com.fitnex.app.ui.theme.Hairline
import com.fitnex.app.ui.theme.Night
import com.fitnex.app.ui.theme.Panel

@Composable
fun ResultScreen(
    summary: BattleSummary,
    settings: GameSettings,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val feedback = remember { GameFeedback(context) }

    DisposableEffect(feedback) {
        onDispose { feedback.close() }
    }

    LaunchedEffect(summary) {
        if (summary.victory) feedback.victory(settings) else feedback.defeat(settings)
    }

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val reveal by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 620),
        label = "resultReveal",
    )
    val bossId = BossCatalog.all.firstOrNull { it.name == summary.bossName }?.id

    Box(Modifier.fillMaxSize().background(Night)) {
        if (bossId != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = 0.28f * reveal },
            ) {
                BossArt(bossId = bossId, height = 460.dp, showScrim = false, dim = 0.15f)
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        0f to Night.copy(alpha = 0.55f),
                        0.45f to Night.copy(alpha = 0.85f),
                        1f to Night,
                    ),
                ),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 30.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            item {
                Column(
                    Modifier.graphicsLayer {
                        alpha = 0.5f + reveal * 0.5f
                        translationY = (1f - reveal) * 30f
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FitIcon(
                            if (summary.victory) FitIcon.Trophy else FitIcon.Skull,
                            tint = if (summary.victory) Gold else FogDim,
                            iconSize = 16.dp,
                        )
                        Spacer(Modifier.width(9.dp))
                        Eyebrow(
                            if (summary.victory) "Victory" else "Defeat",
                            color = if (summary.victory) Gold else FogDim,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        summary.bossName,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp, lineHeight = 46.sp),
                        color = Bone,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (summary.victory) {
                            if (summary.unlockedTitle != null) {
                                "The gate opens. A new name is carved above it."
                            } else {
                                "The beast falls. The record grows longer."
                            }
                        } else {
                            "The beast endures. Sharpen yourself and return."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Fog,
                    )
                    Spacer(Modifier.height(18.dp))
                    HairlineRule()
                }
            }

            if (summary.victory) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Eyebrow("Level ascended", color = FogDim)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    summary.levelBefore.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = FogDim,
                                )
                                Spacer(Modifier.width(12.dp))
                                FitIcon(
                                    FitIcon.ArrowRight,
                                    tint = Ember,
                                    iconSize = 16.dp,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    summary.levelAfter.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Ember,
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Eyebrow("Reps", color = FogDim)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                summary.pushUps.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                color = Bone,
                            )
                        }
                    }
                }

                summary.unlockedTitle?.let { title ->
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Panel, RoundedCornerShape(3.dp))
                                .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                                .padding(18.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FitIcon(FitIcon.Trophy, tint = Gold, iconSize = 14.dp)
                                Spacer(Modifier.width(8.dp))
                                Eyebrow("Title unlocked", color = Gold)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                title.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Bone,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                title.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Fog,
                            )
                            Spacer(Modifier.height(12.dp))
                            GoldRule()
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Equip it from your hero page.",
                                style = MaterialTheme.typography.labelSmall,
                                color = FogDim,
                            )
                        }
                    }
                }

                item {
                    Column {
                        Eyebrow("Next target", color = FogDim)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            summary.nextBossName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Bone,
                        )
                    }
                }
            }

            item {
                StatStrip(
                    items = listOf(
                        Triple("Reps", summary.pushUps.toString(), Bone),
                        Triple("Damage", summary.damage.toString(), Ember),
                        Triple("Time", formatClock(summary.secondsUsed), Bone),
                    ),
                )
            }

            item {
                Column {
                    if (summary.victory) {
                        PrimaryAction(text = "Return to camp", onClick = onDismiss, icon = FitIcon.Home)
                    } else {
                        PrimaryAction(text = "Try again", onClick = onRetry, icon = FitIcon.Swords)
                        Spacer(Modifier.height(10.dp))
                        SecondaryAction(text = "Return to camp", onClick = onDismiss, icon = FitIcon.Home)
                    }
                }
            }
        }
    }
}
