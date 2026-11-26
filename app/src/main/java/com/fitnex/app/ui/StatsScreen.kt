package com.fitnex.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.domain.BossCatalog
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.domain.repsLastSevenDays
import com.fitnex.app.ui.components.Eyebrow
import com.fitnex.app.ui.components.FitIcon
import com.fitnex.app.ui.components.HairlineRule
import com.fitnex.app.ui.components.Metrics
import com.fitnex.app.ui.components.SectionHeader
import com.fitnex.app.ui.components.Tag
import com.fitnex.app.ui.components.WeeklyRepChart
import com.fitnex.app.ui.components.WorkoutRow
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.Fog
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Gold
import com.fitnex.app.ui.theme.Hairline
import com.fitnex.app.ui.theme.Panel

@Composable
fun StatsScreen(state: PlayerState) {
    val days = remember(state.history) { state.repsLastSevenDays() }
    val weekTotal = days.sumOf { it.pushUps }
    val winRate = if (state.battleAttempts > 0) {
        (state.battleWins * 100) / state.battleAttempts
    } else {
        0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Metrics.Gutter,
            end = Metrics.Gutter,
            top = 10.dp,
            bottom = Metrics.BottomBreathing,
        ),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Eyebrow("The record", color = Fog)
                    Tag(state.rank.title, color = Gold)
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        state.lifetimePushUps.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp, lineHeight = 60.sp),
                        color = Bone,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "reps",
                        style = MaterialTheme.typography.titleMedium,
                        color = Fog,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "carved into the record since day one",
                    style = MaterialTheme.typography.bodySmall,
                    color = FogDim,
                )
                Spacer(Modifier.height(18.dp))
                HairlineRule()
            }
        }

        item {
            Column {
                SectionHeader("Ledger")
                Spacer(Modifier.height(6.dp))
                LedgerRow("Bosses defeated", "${state.bossesDefeated} / ${BossCatalog.all.size}")
                LedgerRow("Battles won", state.battleWins.toString())
                LedgerRow("Battles lost", state.battlesLost.toString())
                LedgerRow("Win rate", if (state.battleAttempts == 0) "—" else "$winRate%")
                LedgerRow("Best battle", "${state.bestPushUpsInBattle} reps")
                LedgerRow("Highest boss felled", state.highestBossDefeated?.name ?: "None yet")
            }
        }

        item {
            Column {
                SectionHeader("Rhythm", meta = "$weekTotal this week")
                Spacer(Modifier.height(18.dp))
                if (days.all { it.pushUps == 0 }) {
                    EmptyRhythm()
                } else {
                    WeeklyRepChart(days = days)
                }
            }
        }

        item {
            SectionHeader(
                title = "Chronicle",
                meta = if (state.history.isEmpty()) "empty" else "${state.history.size} fights",
            )
        }

        if (state.history.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Panel.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                ) {
                    FitIcon(FitIcon.Chart, tint = Ember.copy(alpha = 0.8f), iconSize = 18.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Nothing recorded yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Bone,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Reps, wins and bests appear here after your first battle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Fog,
                    )
                }
            }
        } else {
            items(state.history, key = { it.battleId }) { record ->
                WorkoutRow(record)
            }
        }
    }
}

@Composable
private fun LedgerRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Fog)
            Text(value, style = MaterialTheme.typography.titleMedium, color = Bone)
        }
        HairlineRule(color = Hairline.copy(alpha = 0.55f))
    }
}

@Composable
private fun EmptyRhythm() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Hairline.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            "No reps this week. The rhythm starts with one.",
            style = MaterialTheme.typography.bodySmall,
            color = FogDim,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}
