package com.fitnex.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitnex.app.domain.WorkoutRecord
import com.fitnex.app.ui.formatClock
import com.fitnex.app.ui.formatHistoryDate
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.EmberRed
import com.fitnex.app.ui.theme.Line
import com.fitnex.app.ui.theme.Muted

@Composable
fun WorkoutRow(record: WorkoutRecord) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(record.bossName, style = MaterialTheme.typography.titleMedium, color = Bone)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${record.pushUps} push-ups · ${formatClock(record.secondsUsed)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (record.victory) "SLAIN" else "RETREAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (record.victory) EmberRed else Muted,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    formatHistoryDate(record.timestampMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
        HorizontalDivider(color = Line, thickness = 1.dp)
    }
}
