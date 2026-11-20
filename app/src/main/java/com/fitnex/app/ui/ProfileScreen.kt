package com.fitnex.app.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.data.CameraFacing
import com.fitnex.app.data.GameSettings
import com.fitnex.app.domain.BossCatalog
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.domain.TitleCatalog
import com.fitnex.app.ui.theme.Ash
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Charcoal
import com.fitnex.app.ui.theme.EmberRed
import com.fitnex.app.ui.theme.Line
import com.fitnex.app.ui.theme.Muted
import com.fitnex.app.ui.theme.Steel

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
        modifier = Modifier
            .fillMaxSize()
            .background(Ash)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Text("PROFILE", style = MaterialTheme.typography.displayLarge, color = Bone) }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .border(1.dp, EmberRed.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.level.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Bone,
                        )
                        Text("LEVEL", style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                }
                Spacer(Modifier.padding(start = 16.dp))
                Column {
                    Text("HERO", style = MaterialTheme.typography.titleMedium, color = Bone)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        state.rank.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = EmberRed,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        state.equippedTitle?.name?.uppercase() ?: "NO TITLE EQUIPPED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStat("PUSH-UPS", state.lifetimePushUps.toString(), Modifier.weight(1f))
                MiniStat("BOSSES", state.bossesDefeated.toString(), Modifier.weight(1f))
                MiniStat("WINS", state.battleWins.toString(), Modifier.weight(1f))
            }
        }

        item {
            Text(
                "TITLES",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        items(TitleCatalog.all.size) { index ->
            val title = TitleCatalog.all[index]
            val unlocked = title.id in state.unlockedTitleIds
            val equipped = state.equippedTitleId == title.id
            TitleRow(
                name = title.name,
                description = if (unlocked) {
                    title.description
                } else {
                    "Locked · defeat ${BossCatalog.all.first { it.id == title.bossId }.name}"
                },
                unlocked = unlocked,
                equipped = equipped,
                onEquip = { onEquipTitle(if (equipped) null else title.id) },
            )
        }

        item {
            Text(
                "SETTINGS",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Charcoal, RoundedCornerShape(3.dp))
                    .border(1.dp, Line, RoundedCornerShape(3.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    "CAMERA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Spacer(Modifier.height(10.dp))
                CameraSelector(settings) { facing ->
                    onUpdateSettings { it.copy(cameraFacing = facing) }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Line, thickness = 1.dp)
                ToggleRow(
                    label = "Haptics",
                    description = "A pulse for every counted rep.",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(hapticsEnabled = enabled) }
                    },
                )
                HorizontalDivider(color = Line, thickness = 1.dp)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Charcoal, RoundedCornerShape(3.dp))
                    .border(1.dp, Line, RoundedCornerShape(3.dp))
                    .padding(16.dp),
            ) {
                Text("DATA", style = MaterialTheme.typography.labelSmall, color = Muted)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Reset erases your level, titles, history and stats. Settings are kept.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.padding(0.dp),
                ) {
                    Text(
                        "RESET PROGRESS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmberRed,
                    )
                }
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
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Charcoal, RoundedCornerShape(3.dp))
            .border(1.dp, Line, RoundedCornerShape(3.dp))
            .padding(14.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = Bone)
    }
}

@Composable
private fun TitleRow(
    name: String,
    description: String,
    unlocked: Boolean,
    equipped: Boolean,
    onEquip: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal, RoundedCornerShape(3.dp))
            .border(1.dp, if (equipped) EmberRed.copy(alpha = 0.6f) else Line, RoundedCornerShape(3.dp))
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                color = if (unlocked) Bone else Muted,
            )
            Spacer(Modifier.height(3.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Muted)
            if (equipped) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "EQUIPPED",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberRed,
                )
            }
        }
        if (unlocked) {
            TextButton(onClick = onEquip) {
                Text(
                    if (equipped) "UNEQUIP" else "EQUIP",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (equipped) Muted else EmberRed,
                )
            }
        }
    }
}

@Composable
private fun CameraSelector(settings: GameSettings, onSelect: (CameraFacing) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(Steel)
            .border(1.dp, Line, RoundedCornerShape(2.dp)),
    ) {
        CameraFacing.entries.forEach { facing ->
            val active = settings.cameraFacing == facing
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(facing) }
                    .background(if (active) EmberRed else Color.Transparent)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (facing == CameraFacing.FRONT) "FRONT" else "BACK",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) Color.White else Muted,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = Bone)
            Spacer(Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmberRed,
                checkedBorderColor = EmberRed,
                uncheckedThumbColor = Muted,
                uncheckedTrackColor = Steel,
                uncheckedBorderColor = Line,
            ),
        )
    }
}

@Composable
private fun ResetDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var typed by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        title = {
            Text("RESET PROGRESS", style = MaterialTheme.typography.labelMedium, color = EmberRed)
        },
        text = {
            Column {
                Text(
                    "This erases every level, title, record and stat. It cannot be undone. " +
                        "Type RESET to confirm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmberRed,
                        unfocusedBorderColor = Line,
                        focusedTextColor = Bone,
                        unfocusedTextColor = Bone,
                        cursorColor = EmberRed,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = typed.trim().uppercase() == "RESET",
            ) {
                Text(
                    "RESET",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (typed.trim().uppercase() == "RESET") EmberRed else Muted,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
        },
    )
}
