package com.fitnex.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fitnex.app.domain.WorkoutRecord
import com.fitnex.app.ui.formatClock
import com.fitnex.app.ui.formatHistoryDate
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.Fog
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Hairline

/**
 * One entry in the battle ledger: a timeline spine on the left, the encounter in
 * the middle, the verdict on the ending edge. Reads as a record, not a card.
 */
@Composable
fun WorkoutRow(record: WorkoutRecord, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(
            Modifier
                .width(18.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                Modifier
                    .padding(top = 6.dp)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Hairline.copy(alpha = 0.6f)),
            )
            Box(
                Modifier
                    .padding(top = 6.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (record.victory) Ember else FogDim),
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, bottom = 20.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Text(record.bossName, style = MaterialTheme.typography.titleMedium, color = Bone)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${record.pushUps} reps  ·  ${formatClock(record.secondsUsed)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Fog,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    formatHistoryDate(record.timestampMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = FogDim,
                )
            }
            Text(
                if (record.victory) "SLAIN" else "FELLED",
                style = MaterialTheme.typography.labelSmall,
                color = if (record.victory) Ember else FogDim,
                modifier = Modifier.padding(top = 3.dp, end = 2.dp),
            )
        }
    }
}
