package com.fitnex.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.data.CameraFacing
import com.fitnex.app.data.GameSettings
import com.fitnex.app.domain.BossCatalog
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.domain.TitleCatalog
import com.fitnex.app.ui.components.BossThumb
import com.fitnex.app.ui.components.Eyebrow
import com.fitnex.app.ui.components.FitIcon
import com.fitnex.app.ui.components.HairlineRule
import com.fitnex.app.ui.components.Metrics
import com.fitnex.app.ui.components.RankSigil
import com.fitnex.app.ui.components.SectionHeader
import com.fitnex.app.ui.components.Tag
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.EmberBright
import com.fitnex.app.ui.theme.Fog
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Gold
import com.fitnex.app.ui.theme.Hairline
import com.fitnex.app.ui.theme.Panel
import com.fitnex.app.ui.theme.PanelRaised

@Composable
fun ProfileScreen(
    state: PlayerState,
    settings: GameSettings,
    onEquipTitle: (String?) -> Unit,
    onUpdateSettings: ((GameSettings) -> GameSettings) -> Unit,
    onResetProgress: () -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }

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
                    Eyebrow("Hero", color = Fog)
                    Tag("Lv ${state.level.toString().padStart(2, '0')}", color = Gold)
                }
                Spacer(Modifier.height(16.dp))
                HairlineRule()
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RankSigil(
                    level = state.level,
                    size = 118.dp,
                    progress = rankProgressFor(state.level),
                )
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        state.rank.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Bone,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        (state.equippedTitle?.name ?: "No title equipped").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.equippedTitle != null) Gold else FogDim,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row {
                        Column {
                            Text(
                                state.lifetimePushUps.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Bone,
                            )
                            Eyebrow("Lifetime reps", color = FogDim)
                        }
                        Spacer(Modifier.width(22.dp))
                        Column {
                            Text(
                                "${state.bossesDefeated}/${BossCatalog.all.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Bone,
                            )
                            Eyebrow("Bosses", color = FogDim)
                        }
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader("Campaign", meta = "${state.defeatedBossIds.distinct().size} felled")
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BossCatalog.all.forEach { boss ->
                        val slain = boss.id in state.defeatedBossIds
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                BossThumb(bossId = boss.id, size = 54.dp, dim = !slain)
                                if (!slain) {
                                    FitIcon(FitIcon.Lock, tint = FogDim, iconSize = 14.dp)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                boss.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.5.sp,
                                    letterSpacing = 0.6.sp,
                                ),
                                color = if (slain) Bone else FogDim,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader(
                    title = "Titles",
                    meta = "${state.unlockedTitleIds.size} / ${TitleCatalog.all.size}",
                )
                Spacer(Modifier.height(6.dp))
                TitleCatalog.all.forEach { title ->
                    val unlocked = title.id in state.unlockedTitleIds
                    val equipped = state.equippedTitleId == title.id
                    ProfileTitleRow(
                        name = title.name,
                        description = if (unlocked) {
                            title.description
                        } else {
                            val bossName = BossCatalog.all.first { it.id == title.bossId }.name
                            "Locked · defeat $bossName"
                        },
                        unlocked = unlocked,
                        equipped = equipped,
                        onEquip = { onEquipTitle(if (equipped) null else title.id) },
                    )
                }
            }
        }

        item {
            Column {
                SectionHeader("Preferences")
                Spacer(Modifier.height(16.dp))
                Eyebrow("Camera", color = FogDim)
                Spacer(Modifier.height(8.dp))
                CameraSelector(settings = settings, onSelect = { facing ->
                    onUpdateSettings { it.copy(cameraFacing = facing) }
                })
                Spacer(Modifier.height(20.dp))
                ToggleRow(
                    label = "Haptics",
                    description = "A pulse for every counted rep.",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(hapticsEnabled = enabled) }
                    },
                )
                HairlineRule()
                ToggleRow(
                    label = "Sound",
                    description = "Rep ticks and battle stings.",
                    checked = settings.soundEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(soundEnabled = enabled) }
                    },
                )
            }
        }

        item {
            Column {
                SectionHeader("Data")
                Spacer(Modifier.height(12.dp))
                Text(
                    "Resetting erases your level, titles, history and stats. Settings are kept.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Fog,
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Ember.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                        .clickable { showResetDialog = true }
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FitIcon(FitIcon.Reset, tint = Ember, iconSize = 17.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Reset progress",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ember,
                        modifier = Modifier.weight(1f),
                    )
                    FitIcon(FitIcon.ChevronRight, tint = Ember.copy(alpha = 0.6f), iconSize = 14.dp)
                }
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                HairlineRule()
                Spacer(Modifier.height(16.dp))
                Text(
                    "FITNEX 1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = FogDim,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "All progress stays on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FogDim,
                )
            }
        }
    }

    if (showResetDialog) {
        ResetDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                showResetDialog = false
                onResetProgress()
            },
        )
    }
}

@Composable
private fun ProfileTitleRow(
    name: String,
    description: String,
    unlocked: Boolean,
    equipped: Boolean,
    onEquip: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (equipped) Ember.copy(alpha = 0.5f) else Hairline.copy(alpha = 0.55f),
        label = "titleBorder",
    )
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = unlocked, onClick = onEquip)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, borderColor, CircleShape)
                    .background(if (equipped) Ember.copy(alpha = 0.10f) else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                FitIcon(
                    icon = if (unlocked) FitIcon.Trophy else FitIcon.Lock,
                    tint = when {
                        equipped -> Ember
                        unlocked -> Gold
                        else -> FogDim
                    },
                    iconSize = 15.dp,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (unlocked) Bone else FogDim,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = FogDim,
                )
            }
            if (equipped) {
                Spacer(Modifier.width(10.dp))
                Tag("Equipped", color = Ember)
            } else if (unlocked) {
                Spacer(Modifier.width(10.dp))
                Text(
                    "EQUIP",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ember,
                )
            }
        }
        HairlineRule(color = Hairline.copy(alpha = 0.5f))
    }
}

@Composable
private fun CameraSelector(settings: GameSettings, onSelect: (CameraFacing) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Panel.copy(alpha = 0.9f))
            .border(1.dp, Hairline, RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CameraFacing.entries.forEach { facing ->
            val active = settings.cameraFacing == facing
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (active) Brush.linearGradient(listOf(EmberBright, Ember)) else Brush.linearGradient(
                            listOf(Color.Transparent, Color.Transparent),
                        ),
                    )
                    .clickable { onSelect(facing) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (facing == CameraFacing.FRONT) "FRONT" else "BACK",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) Bone else FogDim,
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = Bone)
            Spacer(Modifier.height(3.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = FogDim)
        }
        FitSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FitSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 3.dp,
        label = "switchKnob",
    )
    val track by animateColorAsState(
        targetValue = if (checked) Ember.copy(alpha = 0.24f) else PanelRaised,
        label = "switchTrack",
    )
    Box(
        modifier = Modifier
            .width(47.dp)
            .height(27.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(track)
            .border(
                1.dp,
                if (checked) Ember.copy(alpha = 0.6f) else Hairline,
                RoundedCornerShape(14.dp),
            )
            .clickable { onCheckedChange(!checked) },
    ) {
        Box(
            Modifier
                .offset(x = knobOffset, y = 3.dp)
                .size(19.dp)
                .clip(CircleShape)
                .background(if (checked) EmberBright else FogDim),
        )
    }
}

@Composable
private fun ResetDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var typed by remember { mutableStateOf("") }
    val confirmed = typed.trim().uppercase() == "RESET"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Eyebrow("Point of no return", color = Ember)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Reset progress",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Bone,
                )
            }
        },
        text = {
            Column {
                Text(
                    "This erases every level, title, record and stat. It cannot be undone. Type RESET to confirm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Fog,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Ember,
                        unfocusedBorderColor = Hairline,
                        focusedTextColor = Bone,
                        unfocusedTextColor = Bone,
                        cursorColor = Ember,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = confirmed) {
                Text(
                    "RESET",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (confirmed) Ember else FogDim,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = MaterialTheme.typography.labelMedium, color = FogDim)
            }
        },
    )
}
