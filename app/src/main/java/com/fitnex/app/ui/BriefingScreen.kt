package com.fitnex.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.domain.BossCatalog
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.ui.components.BossArt
import com.fitnex.app.ui.theme.Ash
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Charcoal
import com.fitnex.app.ui.theme.EmberRed
import com.fitnex.app.ui.theme.Line
import com.fitnex.app.ui.theme.Muted

@Composable
fun BriefingScreen(state: PlayerState, onStartBattle: () -> Unit) {
    val boss = state.currentBoss
    var visible by remember(boss.id) { mutableStateOf(false) }
    val artAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "bossArtReveal",
    )
    LaunchedEffect(boss.id) { visible = true }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Ash)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("TARGET", style = MaterialTheme.typography.labelMedium, color = Muted)
                Text(
                    (BossCatalog.indexOf(boss) + 1).toString().padStart(2, '0') + " / " +
                        BossCatalog.all.size.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (boss.isFinal) EmberRed else Muted,
                )
            }
        }

        item { BossArt(bossId = boss.id, height = 262.dp, modifier = Modifier.alpha(artAlpha)) }

        item {
            Text(boss.name, style = MaterialTheme.typography.displayLarge, color = Bone)
            Spacer(Modifier.height(6.dp))
            Text(boss.lore, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BriefingStat("HP", boss.hp.toString(), Modifier.weight(1f))
                BriefingStat("TIME", formatClock(boss.timeLimitSeconds), Modifier.weight(1f))
                BriefingStat("PER REP", "2", Modifier.weight(1f))
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Charcoal, RoundedCornerShape(3.dp))
                    .border(1.dp, Line, RoundedCornerShape(3.dp))
                    .padding(18.dp),
            ) {
                Text("POSITIONING", style = MaterialTheme.typography.labelSmall, color = Muted)
                Spacer(Modifier.height(12.dp))
                Rule("01", "Set the phone on the floor, screen up, angled at your shoulders.")
                Rule("02", "Keep your whole torso and both hands inside the frame.")
                Rule("03", "Full lockout at the top, chest low at the bottom. Partial reps do not count.")
            }
        }

        item {
            Column {
                Button(
                    onClick = onStartBattle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(3.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmberRed,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        "START BATTLE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Defeat ${boss.name} and you gain one level and a title.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun BriefingStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Charcoal, RoundedCornerShape(3.dp))
            .border(1.dp, Line, RoundedCornerShape(3.dp))
            .padding(vertical = 14.dp, horizontal = 16.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = Bone)
    }
}

@Composable
private fun Rule(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(number, style = MaterialTheme.typography.labelSmall, color = EmberRed)
        Spacer(Modifier.padding(start = 10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = Bone,
            modifier = Modifier.padding(bottom = 10.dp),
        )
    }
}
