package com.fitnex.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fitnex.app.R
import com.fitnex.app.ui.theme.Ash
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.EmberRed
import com.fitnex.app.ui.theme.Line
import com.fitnex.app.ui.theme.Muted

enum class FitNexTab(val label: String, @param:DrawableRes val icon: Int, val hint: String) {
    HOME("HOME", R.drawable.ic_tab_home, "Player overview"),
    BATTLE("BATTLE", R.drawable.ic_tab_battle, "Next boss"),
    STATS("STATS", R.drawable.ic_tab_stats, "Progression"),
    PROFILE("PROFILE", R.drawable.ic_tab_profile, "Identity"),
}

@Composable
fun FitNexBottomBar(current: FitNexTab, onSelect: (FitNexTab) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ash),
    ) {
        HorizontalDivider(color = Line, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(58.dp),
        ) {
            FitNexTab.entries.forEach { tab ->
                val selected = tab == current
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(2.dp)
                            .background(if (selected) EmberRed else Color.Transparent),
                    )
                    Spacer(Modifier.height(7.dp))
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = null,
                        tint = if (selected) Bone else Muted,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Bone else Muted,
                    )
                }
            }
        }
    }
}
