package com.fitnex.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.domain.Boss
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.ui.components.BossArt
import com.fitnex.app.ui.components.HealthBar
import com.fitnex.app.ui.components.WorkoutRow
import com.fitnex.app.ui.theme.Ash
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Charcoal
import com.fitnex.app.ui.theme.EmberRed
import com.fitnex.app.ui.theme.Line
import com.fitnex.app.ui.theme.Muted

@Composable
fun HomeScreen(
    state: PlayerState,
    onStartBattle: () -> Unit,
    onOpenBriefing: () -> Unit,
    onOpenStats: () -> Unit,
) {
    val recent = state.history.take(3)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Ash)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { BrandRow() }
        item { PlayerHeader(state) }
        item { BossPanel(state.currentBoss, onOpenBriefing) }
        item { StartBattleButton(onStartBattle) }
        item { StatCells(state) }
        item { RecentHeader(onOpenStats) }
        if (recent.isEmpty()) {
            item { EmptyHistory() }
        } else {
            items(recent, key = { it.battleId }) { record -> WorkoutRow(record) }
        }
    }
}

@Composable
private fun BrandRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "FITNEX",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 5.sp,
            color = Bone,
        )
        Text(
            text = "PUSH-UP RPG",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}

@Composable
private fun PlayerHeader(state: PlayerState) {
    Row(verticalAlignment = Alignment.Bottom) {
        Column {
            Text("LEVEL", style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(2.dp))
            Text(
                text = state.level.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 58.sp,
                color = Bone,
            )
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.padding(bottom = 12.dp)) {
            Text(state.rank.title, style = MaterialTheme.typography.titleMedium, color = Bone)
            Spacer(Modifier.height(3.dp))
            Text(
                text = state.equippedTitle?.name?.uppercase() ?: "NO TITLE EQUIPPED",
                style = MaterialTheme.typography.labelSmall,
                color = if (state.equippedTitle != null) EmberRed else Muted,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "LIFETIME ${state.lifetimePushUps} REPS",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
            )
        }
    }
}

@Composable
private fun BossPanel(boss: Boss, onOpenBriefing: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .clickable(onClick = onOpenBriefing)
            .background(Charcoal)
            .border(1.dp, Line, RoundedCornerShape(3.dp)),
    ) {
        BossArt(bossId = boss.id, height = 168.dp)

        Column(Modifier.padding(start = 18.dp, end = 18.dp, bottom = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("CURRENT TARGET", style = MaterialTheme.typography.labelSmall, color = Muted)
                if (boss.isFinal) {
                    Text("FINAL BOSS", style = MaterialTheme.typography.labelSmall, color = EmberRed)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(boss.name, style = MaterialTheme.typography.displayLarge, color = Bone)
            Spacer(Modifier.height(6.dp))
            Text(boss.lore, style = MaterialTheme.typography.bodyMedium, color = Muted)
            Spacer(Modifier.height(16.dp))
            HealthBar(fraction = 1f)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "HP ${boss.hp} / ${boss.hp}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
                Text(
                    "${formatClock(boss.timeLimitSeconds)} LIMIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "2 DAMAGE PER REP",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberRed,
                )
                Text(
                    "BRIEFING ›",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun StartBattleButton(onStartBattle: () -> Unit) {
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
            "Front camera · elbows tracked live",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatCells(state: PlayerState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCell("LIFETIME PUSH-UPS", state.lifetimePushUps.toString(), Modifier.weight(1f))
        StatCell("BOSSES DEFEATED", state.bossesDefeated.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Charcoal, RoundedCornerShape(3.dp))
            .border(1.dp, Line, RoundedCornerShape(3.dp))
            .padding(16.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = Bone)
    }
}

@Composable
private fun RecentHeader(onOpenStats: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "RECENT ACTIVITY",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            modifier = Modifier.padding(top = 6.dp),
        )
        TextButton(onClick = onOpenStats) {
            Text("VIEW ALL", style = MaterialTheme.typography.labelSmall, color = EmberRed)
        }
    }
}

@Composable
private fun EmptyHistory() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal, RoundedCornerShape(3.dp))
            .border(1.dp, Line, RoundedCornerShape(3.dp))
            .padding(20.dp),
    ) {
        Text("No battles recorded.", style = MaterialTheme.typography.bodyMedium, color = Bone)
        Spacer(Modifier.height(4.dp))
        Text(
            "Start a fight and your legend will be written here.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }
}
