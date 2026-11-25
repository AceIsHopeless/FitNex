package com.fitnex.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.domain.BossCatalog
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.domain.Rank
import com.fitnex.app.domain.repsLastSevenDays
import com.fitnex.app.ui.components.BossArt
import com.fitnex.app.ui.components.Eyebrow
import com.fitnex.app.ui.components.FitIcon
import com.fitnex.app.ui.components.HairlineRule
import com.fitnex.app.ui.components.HealthBar
import com.fitnex.app.ui.components.Metrics
import com.fitnex.app.ui.components.ProgressRail
import com.fitnex.app.ui.components.RankSigil
import com.fitnex.app.ui.components.SectionHeader
import com.fitnex.app.ui.components.StatStrip
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
fun HomeScreen(
    state: PlayerState,
    onStartBattle: () -> Unit,
    onOpenBriefing: () -> Unit,
    onOpenStats: () -> Unit,
) {
    val boss = state.currentBoss
    val week = remember(state.history) { state.repsLastSevenDays() }
    val weekTotal = week.sumOf { it.pushUps }
    val recent = state.history.take(3)
    val rank = state.rank
    val nextRank = Rank.entries.firstOrNull { it.minLevel > state.level }
    val rankProgress = rankProgressFor(state.level)

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val entrance by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "homeEntrance",
    )

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
        item { BrandBar(rank = rank, level = state.level) }

        item {
            Column(
                Modifier.graphicsLayer {
                    // Never starts fully transparent: a stalled first frame must
                    // still show the hero, not an empty page.
                    alpha = 0.55f + entrance * 0.45f
                    translationY = (1f - entrance) * 24f
                },
            ) {
                IdentityBlock(
                    level = state.level,
                    rankTitle = rank.title,
                    nextRank = nextRank,
                    rankProgress = rankProgress,
                    titleName = state.equippedTitle?.name,
                    lifetimePushUps = state.lifetimePushUps,
                )
            }
        }

        item {
            Column(
                Modifier.graphicsLayer {
                    alpha = 0.55f + entrance * 0.45f
                    translationY = (1f - entrance) * 34f
                },
            ) {
                SectionHeader(
                    title = "The hunt",
                    meta = "${BossCatalog.indexOf(boss) + 1} / ${BossCatalog.all.size}",
                    metaColor = if (boss.isFinal) Ember else FogDim,
                )
                Spacer(Modifier.height(14.dp))
                CampaignPlate(
                    state = state,
                    onClick = onOpenBriefing,
                )
                Spacer(Modifier.height(14.dp))
                EngageButton(onClick = onStartBattle)
            }
        }

        item {
            Column {
                SectionHeader(title = "Momentum", meta = "$weekTotal this week")
                Spacer(Modifier.height(18.dp))
                WeeklyRepChart(days = week)
                Spacer(Modifier.height(22.dp))
                HairlineRule()
                Spacer(Modifier.height(18.dp))
                StatStrip(
                    items = listOf(
                        Triple("Lifetime reps", state.lifetimePushUps.toString(), Bone),
                        Triple(
                            "Bosses",
                            "${state.bossesDefeated}/${BossCatalog.all.size}",
                            Bone,
                        ),
                        Triple("Wins", state.battleWins.toString(), Bone),
                    ),
                )
            }
        }

        item {
            SectionHeader(
                title = "Chronicle",
                meta = if (state.history.isEmpty()) "empty" else "${state.history.size} fights",
                onAction = onOpenStats,
                actionLabel = "All",
            )
        }

        if (recent.isEmpty()) {
            item { EmptyChronicle() }
        } else {
            items(recent, key = { it.battleId }) { record ->
                WorkoutRow(record)
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HairlineRule()
                Spacer(Modifier.height(18.dp))
                Text(
                    "\"Strength is not given. It is taken, rep by rep.\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = FogDim,
                )
            }
        }
    }
}

@Composable
private fun BrandBar(rank: Rank, level: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FITNEX",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 16.sp,
                        letterSpacing = 5.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Bone,
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .width(16.dp)
                        .height(1.dp)
                        .background(Ember),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Tag(text = "Lv $level", color = Gold)
                Spacer(Modifier.width(8.dp))
                Eyebrow(rank.title, color = FogDim)
            }
        }
        Spacer(Modifier.height(16.dp))
        HairlineRule()
    }
}

@Composable
private fun IdentityBlock(
    level: Int,
    rankTitle: String,
    nextRank: Rank?,
    rankProgress: Float,
    titleName: String?,
    lifetimePushUps: Long,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RankSigil(level = level, progress = rankProgress, size = 104.dp)
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Eyebrow(
                if (nextRank == null) "Peak rank" else "Next rank · lvl ${nextRank.minLevel}",
                color = FogDim,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                rankTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = Bone,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                (titleName ?: "No title equipped").uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (titleName != null) Gold else FogDim,
                maxLines = 1,
            )
            Spacer(Modifier.height(14.dp))
            ProgressRail(progress = rankProgress, height = 3.dp)
            Spacer(Modifier.height(8.dp))
            Text(
                "$lifetimePushUps reps carved into the record",
                style = MaterialTheme.typography.bodySmall,
                color = Fog,
            )
        }
    }
}

@Composable
private fun CampaignPlate(state: PlayerState, onClick: () -> Unit) {
    val boss = state.currentBoss
    val cleared = state.bossesDefeated

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Panel)
            .border(1.dp, Hairline, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick),
    ) {
        Box {
            BossArt(bossId = boss.id, height = 268.dp, showScrim = true)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Tag(
                    text = "Target ${(BossCatalog.indexOf(boss) + 1).toString().padStart(2, '0')}" +
                        " / ${BossCatalog.all.size.toString().padStart(2, '0')}",
                    color = Bone.copy(alpha = 0.55f),
                )
                if (boss.isFinal) {
                    Tag(text = "Final gate", color = Ember, filled = true)
                } else if (cleared > 0) {
                    Tag(text = "$cleared slain", color = Gold)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Eyebrow("Current target", color = Fog)
                Spacer(Modifier.height(6.dp))
                Text(boss.name, style = MaterialTheme.typography.displayLarge, color = Bone)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FitIcon(FitIcon.Skull, tint = Ember, iconSize = 13.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${boss.hp} hp",
                        style = MaterialTheme.typography.labelMedium,
                        color = Fog,
                    )
                    Spacer(Modifier.width(14.dp))
                    FitIcon(FitIcon.Clock, tint = FogDim, iconSize = 13.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        formatClock(boss.timeLimitSeconds),
                        style = MaterialTheme.typography.labelMedium,
                        color = Fog,
                    )
                }
            }
        }

        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 16.dp)) {
            Text(
                boss.lore,
                style = MaterialTheme.typography.bodyMedium,
                color = Fog,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(16.dp))
            HealthBar(fraction = 1f, height = 6.dp, segments = 16)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "2 damage per rep",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ember,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Briefing",
                        style = MaterialTheme.typography.labelSmall,
                        color = Fog,
                    )
                    Spacer(Modifier.width(4.dp))
                    FitIcon(FitIcon.ChevronRight, tint = Fog, iconSize = 12.dp)
                }
            }
        }
    }
}

@Composable
private fun EngageButton(onClick: () -> Unit) {
    Column {
        androidx.compose.material3.Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(3.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Ember,
                contentColor = Bone,
            ),
            elevation = null,
            contentPadding = PaddingValues(horizontal = 22.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "BEGIN THE HUNT",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.2.sp),
                    fontWeight = FontWeight.Bold,
                )
                FitIcon(FitIcon.Swords, tint = Bone, iconSize = 19.dp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Camera on the floor · elbows tracked live · nothing leaves the phone",
            style = MaterialTheme.typography.bodySmall,
            color = FogDim,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EmptyChronicle() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Panel.copy(alpha = 0.55f))
            .border(1.dp, Hairline.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
            .padding(20.dp),
    ) {
        FitIcon(FitIcon.Spark, tint = Ember.copy(alpha = 0.8f), iconSize = 18.dp)
        Spacer(Modifier.height(12.dp))
        Text("The chronicle is blank.", style = MaterialTheme.typography.titleMedium, color = Bone)
        Spacer(Modifier.height(4.dp))
        Text(
            "Your first fight writes the opening line.",
            style = MaterialTheme.typography.bodySmall,
            color = Fog,
        )
    }
}
