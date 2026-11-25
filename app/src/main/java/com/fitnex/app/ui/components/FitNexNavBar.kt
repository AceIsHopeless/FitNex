package com.fitnex.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.EmberBright
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Hairline
import com.fitnex.app.ui.theme.Panel

enum class FitNexTab(val label: String, val icon: FitIcon) {
    HOME("Camp", FitIcon.Home),
    BATTLE("Hunt", FitIcon.Swords),
    STATS("Record", FitIcon.Chart),
    PROFILE("Hero", FitIcon.Helm),
}

/** Floating dock: one quiet slab above the page, ember only on the active tab. */
@Composable
fun FitNexDock(
    current: FitNexTab,
    onSelect: (FitNexTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Panel.copy(alpha = 0.97f))
                .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            FitNexTab.entries.forEach { tab ->
                DockItem(
                    tab = tab,
                    selected = tab == current,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    tab: FitNexTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) EmberBright else FogDim,
        animationSpec = tween(220),
        label = "dockTint",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) Bone else FogDim,
        animationSpec = tween(220),
        label = "dockLabel",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
        label = "dockScale",
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Ember.copy(alpha = 0.13f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FitIcon(
            icon = tab.icon,
            tint = tint,
            iconSize = 20.dp,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = tab.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                letterSpacing = 1.1.sp,
            ),
            color = labelColor,
        )
    }
}
