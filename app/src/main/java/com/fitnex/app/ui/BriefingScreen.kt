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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitnex.app.domain.BossCatalog
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.ui.components.BossArt
import com.fitnex.app.ui.components.Eyebrow
import com.fitnex.app.ui.components.FitIcon
import com.fitnex.app.ui.components.HairlineRule
import com.fitnex.app.ui.components.Metrics
import com.fitnex.app.ui.components.PrimaryAction
import com.fitnex.app.ui.components.SectionHeader
import com.fitnex.app.ui.components.StatStrip
import com.fitnex.app.ui.components.Tag
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.Fog
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Gold
import com.fitnex.app.ui.theme.Hairline

@Composable
fun BriefingScreen(state: PlayerState, onStartBattle: () -> Unit) {
    val boss = state.currentBoss
    val index = BossCatalog.indexOf(boss) + 1
    val total = BossCatalog.all.size
    val alreadySlain = boss.id in state.defeatedBossIds

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Metrics.Gutter,
            end = Metrics.Gutter,
            top = 10.dp,
            bottom = Metrics.BottomBreathing,
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Eyebrow("Briefing", color = Fog)
                    Eyebrow(
                        "${index.toString().padStart(2, '0')} / ${total.toString().padStart(2, '0')}",
                        color = if (boss.isFinal) Ember else FogDim,
                    )
                }
                Spacer(Modifier.height(14.dp))
                HairlineRule()
            }
        }

        item {
            Box {
                BossArt(bossId = boss.id, height = 300.dp)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Tag(
                            text = if (boss.isFinal) "Final gate" else "Tier ${index.toString().padStart(2, '0')}",
                            color = if (boss.isFinal) Ember else Bone.copy(alpha = 0.5f),
                            filled = boss.isFinal,
                        )
                        if (alreadySlain) {
                            Spacer(Modifier.width(8.dp))
                            Tag(text = "Slain before", color = Gold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(boss.name, style = MaterialTheme.typography.displayLarge, color = Bone)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .width(2.dp)
                        .height(46.dp)
                        .background(Ember),
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    boss.lore,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Fog,
                )
            }
        }

        item {
            Column {
                SectionHeader("Vitals")
                Spacer(Modifier.height(18.dp))
                StatStrip(
                    items = listOf(
                        Triple("Health", boss.hp.toString(), Bone),
                        Triple("Time limit", formatClock(boss.timeLimitSeconds), Bone),
                        Triple("Per rep", "2", Ember),
                    ),
                )
            }
        }

        item {
            Column {
                SectionHeader("Rules of engagement")
                Spacer(Modifier.height(16.dp))
                Rule("01", "Set the phone on the floor, screen up, angled at your shoulders.")
                HairlinePair()
                Rule("02", "Keep your whole torso and both hands inside the frame.")
                HairlinePair()
                Rule("03", "Full lockout up top, chest low at the bottom. Partial reps do not count.")
                HairlinePair()
                Rule("04", "One extra minute is offered once if the clock runs out.")
            }
        }

        item {
            Column {
                PrimaryAction(
                    text = if (alreadySlain) "Face it again" else "Begin the hunt",
                    onClick = onStartBattle,
                    icon = FitIcon.Swords,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    FitIcon(FitIcon.Spark, tint = FogDim, iconSize = 13.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (alreadySlain) {
                            "Victory still counts for the record, but its title is already yours."
                        } else {
                            "Slay ${boss.name} to gain a level and claim its title."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = FogDim,
                    )
                }
            }
        }
    }
}

@Composable
private fun Rule(number: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Text(
            number,
            style = MaterialTheme.typography.labelSmall,
            color = Ember,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Bone)
    }
}

@Composable
private fun HairlinePair() {
    HairlineRule(color = Hairline.copy(alpha = 0.6f))
}
