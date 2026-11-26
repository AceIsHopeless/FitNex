package com.fitnex.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.R
import com.fitnex.app.domain.Boss
import com.fitnex.app.domain.BossCatalog
import com.fitnex.app.domain.DailyReps
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.domain.Rank
import com.fitnex.app.domain.repsLastSevenDays
import com.fitnex.app.ui.components.BossArt
import com.fitnex.app.ui.components.Eyebrow
import com.fitnex.app.ui.components.FitIcon
import com.fitnex.app.ui.components.GlassCard
import com.fitnex.app.ui.components.HairlineRule
import com.fitnex.app.ui.components.HealthBar
import com.fitnex.app.ui.components.Metrics
import com.fitnex.app.ui.components.PrimaryAction
import com.fitnex.app.ui.components.ProgressRail
import com.fitnex.app.ui.components.SectionHeader
import com.fitnex.app.ui.components.StatStrip
import com.fitnex.app.ui.components.Tag
import com.fitnex.app.ui.components.WeeklyRepChart
import com.fitnex.app.ui.components.WorkoutRow
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.EmberBright
import com.fitnex.app.ui.theme.Fog
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Gold
import com.fitnex.app.ui.theme.Hairline
import com.fitnex.app.ui.theme.Night
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
            top = 14.dp,
            bottom = Metrics.BottomBreathing,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(
                Modifier.graphicsLayer {
                    // Never starts fully transparent: a stalled first frame must
                    // still show the hero, not an empty page.
                    alpha = 0.55f + entrance * 0.45f
                    translationY = (1f - entrance) * 18f
                },
            ) {
                GreetingHeader(
                    level = state.level,
                    rankTitle = rank.title,
                    titleName = state.equippedTitle?.name,
                    lifetimePushUps = state.lifetimePushUps,
                    nextRank = nextRank,
                    rankProgress = rankProgress,
                )
            }
        }

        item { HouseRulesRow(onClick = onOpenBriefing) }

        item {
            SectionHeader(
                title = "The hunt",
                meta = "${BossCatalog.indexOf(boss) + 1} / ${BossCatalog.all.size}",
                metaColor = if (boss.isFinal) Ember else FogDim,
            )
        }

        item { HuntCard(state = state, onStartBattle = onStartBattle, onOpenBriefing = onOpenBriefing) }

        item {
            SectionHeader(
                title = "Campaign",
                meta = "${state.bossesDefeated} of ${BossCatalog.all.size} felled",
            )
        }

        item { CampaignGrid(state = state, onOpenBriefing = onOpenBriefing) }

        item { MomentumCard(week = week, weekTotal = weekTotal, state = state) }

        item {
            SectionHeader(
                title = "Chronicle",
                meta = if (state.history.isEmpty()) "empty" else "${state.history.size} fights",
                onAction = onOpenStats,
                actionLabel = "See all",
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
                Spacer(Modifier.height(6.dp))
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

/** Greeting block: wordmark, rank, equipped title and the lifetime-rep pill. */
@Composable
private fun GreetingHeader(
    level: Int,
    rankTitle: String,
    titleName: String?,
    lifetimePushUps: Long,
    nextRank: Rank?,
    rankProgress: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Image(
                painter = painterResource(R.drawable.fitnex_mark),
                contentDescription = "FitNex",
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(24.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text("Hello,", style = MaterialTheme.typography.bodyMedium, color = Fog)
            Text(
                rankTitle,
                style = MaterialTheme.typography.displayLarge,
                color = Bone,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Level ${level.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FogDim,
                )
                Box(
                    Modifier
                        .padding(horizontal = 7.dp)
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(FogDim),
                )
                Text(
                    (titleName ?: "No title equipped").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (titleName != null) Gold else FogDim,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        RepPill(lifetimePushUps)
    }

    Spacer(Modifier.height(18.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Eyebrow(
            if (nextRank == null) "Peak rank reached" else "Next rank · lvl ${nextRank.minLevel}",
            color = FogDim,
        )
        Eyebrow("${(rankProgress * 100).toInt()}%", color = FogDim)
    }
    Spacer(Modifier.height(8.dp))
    ProgressRail(progress = rankProgress, height = 4.dp)
}

@Composable
private fun RepPill(lifetimePushUps: Long) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(Panel)
            .border(1.dp, Ember.copy(alpha = 0.35f), shape)
            .padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Ember.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            FitIcon(FitIcon.Spark, tint = EmberBright, iconSize = 13.dp)
        }
        Spacer(Modifier.width(9.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                lifetimePushUps.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = Bone,
            )
            Text(
                "reps",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, letterSpacing = 1.sp),
                color = FogDim,
            )
        }
    }
}

@Composable
private fun HouseRulesRow(onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Panel.copy(alpha = 0.94f))
            .border(1.dp, Hairline, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Ember.copy(alpha = 0.16f))
                .border(1.dp, Ember.copy(alpha = 0.28f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            FitIcon(FitIcon.Shield, tint = EmberBright, iconSize = 20.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("House rules", style = MaterialTheme.typography.titleLarge, color = Bone)
            Spacer(Modifier.height(2.dp))
            Text(
                "How to frame your set before the hunt",
                style = MaterialTheme.typography.bodySmall,
                color = FogDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        FitIcon(FitIcon.ChevronRight, tint = Fog, iconSize = 16.dp)
    }
}

@Composable
private fun HuntCard(state: PlayerState, onStartBattle: () -> Unit, onOpenBriefing: () -> Unit) {
    val boss = state.currentBoss
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Panel.copy(alpha = 0.92f))
            .border(1.dp, Hairline, shape),
    ) {
        Box {
            BossArt(bossId = boss.id, height = 208.dp, cornerRadius = 0.dp, showScrim = true)
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
                    color = Bone.copy(alpha = 0.75f),
                )
                if (boss.isFinal) {
                    Tag(text = "Final gate", color = Ember, filled = true)
                } else if (state.bossesDefeated > 0) {
                    Tag(text = "${state.bossesDefeated} slain", color = Gold)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Eyebrow("Current target", color = EmberBright)
                Spacer(Modifier.height(5.dp))
                Text(
                    boss.name,
                    style = MaterialTheme.typography.displayLarge,
                    color = Bone,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FitIcon(FitIcon.Skull, tint = Ember, iconSize = 13.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "${boss.hp} hp",
                    style = MaterialTheme.typography.labelSmall,
                    color = Fog,
                )
                Spacer(Modifier.width(14.dp))
                FitIcon(FitIcon.Clock, tint = FogDim, iconSize = 13.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    formatClock(boss.timeLimitSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = Fog,
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    "2 damage per rep",
                    style = MaterialTheme.typography.labelSmall,
                    color = FogDim,
                )
            }
            Spacer(Modifier.height(14.dp))
            HealthBar(fraction = 1f, height = 8.dp, segments = 20)
            Spacer(Modifier.height(18.dp))
            PrimaryAction(
                text = "Begin the hunt",
                onClick = onStartBattle,
                icon = FitIcon.Swords,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenBriefing)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Read the briefing",
                    style = MaterialTheme.typography.labelSmall,
                    color = Fog,
                )
                Spacer(Modifier.width(4.dp))
                FitIcon(FitIcon.ChevronRight, tint = Fog, iconSize = 12.dp)
            }
        }
    }
}

/** Boss ladder as a two-column card grid, portrait over a faded scrim. */
@Composable
private fun CampaignGrid(state: PlayerState, onOpenBriefing: () -> Unit) {
    val target = state.currentBoss
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BossCatalog.all.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { boss ->
                    val isTarget = boss.id == target.id
                    CampaignCell(
                        boss = boss,
                        slain = boss.id in state.defeatedBossIds,
                        target = isTarget,
                        onClick = if (isTarget) onOpenBriefing else null,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CampaignCell(
    boss: Boss,
    slain: Boolean,
    target: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Panel.copy(alpha = 0.94f))
            .border(
                1.dp,
                if (target) Ember.copy(alpha = 0.45f) else Hairline,
                shape,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Box {
            BossArt(
                bossId = boss.id,
                height = 112.dp,
                cornerRadius = 0.dp,
                showScrim = true,
                dim = if (slain) 0.28f else 0f,
            )
            if (!slain && !target) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Night.copy(alpha = 0.62f))
                        .border(1.dp, Hairline, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    FitIcon(FitIcon.Lock, tint = FogDim, iconSize = 14.dp)
                }
            }
            if (slain) {
                Tag(
                    text = "Slain",
                    color = Gold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
        }
        Column(Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) {
            Text(
                boss.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (slain || target) Bone else Fog,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                when {
                    target -> "Target · ${boss.hp} hp"
                    slain -> "Cleared"
                    else -> "Locked"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, letterSpacing = 1.1.sp),
                color = when {
                    target -> Ember
                    slain -> Gold
                    else -> FogDim
                },
            )
        }
    }
}

@Composable
private fun MomentumCard(week: List<DailyReps>, weekTotal: Int, state: PlayerState) {
    GlassCard {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text("Momentum", style = MaterialTheme.typography.titleLarge, color = Bone)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Reps over the last seven days",
                        style = MaterialTheme.typography.bodySmall,
                        color = FogDim,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        weekTotal.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Bone,
                    )
                    Eyebrow("this week", color = FogDim)
                }
            }
            Spacer(Modifier.height(16.dp))
            WeeklyRepChart(days = week)
            Spacer(Modifier.height(6.dp))
            HairlineRule()
            Spacer(Modifier.height(16.dp))
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
}

@Composable
private fun EmptyChronicle() {
    GlassCard {
        Column(Modifier.padding(20.dp)) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ember.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                FitIcon(FitIcon.Spark, tint = EmberBright, iconSize = 18.dp)
            }
            Spacer(Modifier.height(14.dp))
            Text("The chronicle is blank.", style = MaterialTheme.typography.titleMedium, color = Bone)
            Spacer(Modifier.height(4.dp))
            Text(
                "Your first fight writes the opening line.",
                style = MaterialTheme.typography.bodySmall,
                color = Fog,
            )
        }
    }
}
