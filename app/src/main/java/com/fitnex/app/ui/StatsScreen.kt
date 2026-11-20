package com.fitnex.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.domain.repsLastSevenDays
import com.fitnex.app.ui.components.WorkoutRow
import com.fitnex.app.ui.theme.Ash
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Charcoal
import com.fitnex.app.ui.theme.EmberRed
import com.fitnex.app.ui.theme.Line
import com.fitnex.app.ui.theme.Muted
import com.fitnex.app.ui.theme.Steel

@Composable
fun StatsScreen(state: PlayerState) {
    val days = remember(state.history) { state.repsLastSevenDays() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Ash)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text("STATS", style = MaterialTheme.typography.displayLarge, color = Bone)
                Spacer(Modifier.height(4.dp))
                Text(
                    "LIFETIME RECORD",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBlock("LIFETIME PUSH-UPS", state.lifetimePushUps.toString(), Modifier.weight(1f))
                StatBlock("BOSSES DEFEATED", state.bossesDefeated.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBlock("CURRENT LEVEL", state.level.toString(), Modifier.weight(1f))
                StatBlock("RANK", state.rank.title, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBlock("BATTLES WON", state.battleWins.toString(), Modifier.weight(1f))
                StatBlock("BATTLES LOST", state.battlesLost.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBlock(
                    "HIGHEST BOSS",
                    state.highestBossDefeated?.name ?: "None",
                    Modifier.weight(1f),
                )
                StatBlock("BEST BATTLE", "${state.bestPushUpsInBattle} reps", Modifier.weight(1f))
            }
        }

        item { WeeklyChart(days.map { it.label to it.pushUps }) }

        item {
            Text(
                "WORKOUT HISTORY",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (state.history.isEmpty()) {
            item { EmptyStats() }
        } else {
            items(state.history, key = { it.battleId }) { record -> WorkoutRow(record) }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
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
private fun WeeklyChart(days: List<Pair<String, Int>>) {
    val max = (days.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal, RoundedCornerShape(3.dp))
            .border(1.dp, Line, RoundedCornerShape(3.dp))
            .padding(18.dp),
    ) {
        Text("LAST 7 DAYS", style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEach { (label, reps) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = if (reps > 0) reps.toString() else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height((64f * reps / max).coerceAtLeast(2f).dp)
                            .background(if (reps > 0) EmberRed else Steel),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
                }
            }
        }
    }
}

@Composable
private fun EmptyStats() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal, RoundedCornerShape(3.dp))
            .border(1.dp, Line, RoundedCornerShape(3.dp))
            .padding(20.dp),
    ) {
        Text("Nothing recorded yet.", style = MaterialTheme.typography.bodyMedium, color = Bone)
        Spacer(Modifier.height(4.dp))
        Text(
            "Your reps, wins and bests appear here after your first battle.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }
}
