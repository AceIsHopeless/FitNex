package com.fitnex.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.data.GameSettings
import com.fitnex.app.ui.feedback.GameFeedback
import com.fitnex.app.ui.theme.Ash
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Charcoal
import com.fitnex.app.ui.theme.EmberRed
import com.fitnex.app.ui.theme.Line
import com.fitnex.app.ui.theme.Muted

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ash)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 26.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (summary.victory) "VICTORY" else "DEFEAT",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 4.sp,
            color = if (summary.victory) EmberRed else Muted,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = summary.bossName,
            style = MaterialTheme.typography.displayLarge,
            color = Bone,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (summary.victory) {
                if (summary.unlockedTitle != null) {
                    "The boss falls and a new name is yours."
                } else {
                    "The boss falls. Your legend grows."
                }
            } else {
                "The boss endures. Rise again."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))

        if (summary.victory) {
            Text("LEVEL", style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    summary.levelBefore.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Muted,
                )
                Text(
                    "  →  ",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Muted,
                )
                Text(
                    summary.levelAfter.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.displayLarge,
                    color = EmberRed,
                )
            }

            summary.unlockedTitle?.let { title ->
                Spacer(Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Charcoal, RoundedCornerShape(3.dp))
                        .border(1.dp, EmberRed.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "TITLE UNLOCKED",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmberRed,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(title.name, style = MaterialTheme.typography.titleMedium, color = Bone)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Equip it in Profile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ResultStat("PUSH-UPS", summary.pushUps.toString())
            ResultStat("DAMAGE", summary.damage.toString())
            ResultStat("TIME", formatClock(summary.secondsUsed))
        }

        Spacer(Modifier.height(30.dp))
        if (summary.victory) {
            Text("NEXT TARGET", style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(6.dp))
            Text(
                summary.nextBossName,
                style = MaterialTheme.typography.headlineMedium,
                color = Bone,
            )
            Spacer(Modifier.height(30.dp))
        }

        Button(
            onClick = if (summary.victory) onDismiss else onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(3.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmberRed,
                contentColor = Color.White,
            ),
        ) {
            Text(
                if (summary.victory) "RETURN TO CAMP" else "TRY AGAIN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
        }

        if (!summary.victory) {
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onDismiss) {
                Text(
                    "RETURN TO CAMP",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun ResultStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = Bone)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
    }
}
