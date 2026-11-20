package com.fitnex.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitnex.app.ui.components.FitNexBottomBar
import com.fitnex.app.ui.components.FitNexTab
import com.fitnex.app.ui.theme.Ash

@Composable
fun FitNexApp(viewModel: GameViewModel = viewModel()) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableStateOf(FitNexTab.HOME) }
    var originTab by rememberSaveable { mutableStateOf(FitNexTab.HOME) }

    val battle = viewModel.battleUi
    val summary = viewModel.battleSummary

    // A fight owns the whole screen: no tab bar under a camera viewfinder.
    if (battle != null) {
        BattleScreen(viewModel, battle)
        return
    }

    if (summary != null) {
        ResultScreen(
            summary = summary,
            settings = settings,
            onRetry = viewModel::startBattle,
            onDismiss = {
                viewModel.clearSummary()
                tab = originTab
            },
        )
        return
    }

    BackHandler(enabled = tab != FitNexTab.HOME) { tab = FitNexTab.HOME }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ash),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (tab) {
                FitNexTab.HOME -> HomeScreen(
                    state = playerState,
                    onStartBattle = {
                        originTab = FitNexTab.HOME
                        viewModel.startBattle()
                    },
                    onOpenBriefing = { tab = FitNexTab.BATTLE },
                    onOpenStats = { tab = FitNexTab.STATS },
                )

                FitNexTab.BATTLE -> BriefingScreen(
                    state = playerState,
                    onStartBattle = {
                        originTab = FitNexTab.BATTLE
                        viewModel.startBattle()
                    },
                )

                FitNexTab.STATS -> StatsScreen(state = playerState)

                FitNexTab.PROFILE -> ProfileScreen(
                    state = playerState,
                    settings = settings,
                    onEquipTitle = viewModel::equipTitle,
                    onUpdateSettings = viewModel::updateSettings,
                    onResetProgress = viewModel::resetProgress,
                )
            }
        }
        FitNexBottomBar(current = tab, onSelect = { tab = it })
    }
}
