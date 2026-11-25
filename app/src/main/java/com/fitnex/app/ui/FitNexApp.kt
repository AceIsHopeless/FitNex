package com.fitnex.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitnex.app.ui.components.Atmosphere
import com.fitnex.app.ui.components.FitNexDock
import com.fitnex.app.ui.components.FitNexTab
import com.fitnex.app.ui.theme.Night

@Composable
fun FitNexApp(viewModel: GameViewModel = viewModel()) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableStateOf(FitNexTab.HOME) }
    var originTab by rememberSaveable { mutableStateOf(FitNexTab.HOME) }

    val battle = viewModel.battleUi
    val summary = viewModel.battleSummary

    Box(Modifier.fillMaxSize().background(Night)) {
        // A fight owns the whole screen: no dock under a camera viewfinder.
        if (battle != null) {
            BattleScreen(viewModel, battle)
        } else if (summary != null) {
            ResultScreen(
                summary = summary,
                settings = settings,
                onRetry = viewModel::startBattle,
                onDismiss = {
                    viewModel.clearSummary()
                    tab = originTab
                },
            )
        } else {
            BackHandler(enabled = tab != FitNexTab.HOME) { tab = FitNexTab.HOME }

            Atmosphere()

            Column(Modifier.fillMaxSize()) {
                // The shell owns the status-bar inset, so scrolling content is
                // clipped below the clock instead of sliding under it.
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .statusBarsPadding(),
                ) {
                    AnimatedContent(
                        targetState = tab,
                        transitionSpec = {
                            val forward = targetState.ordinal > initialState.ordinal
                            val direction = if (forward) 1 else -1
                            (
                                fadeIn(tween(240)) +
                                    slideInHorizontally(tween(320)) { width ->
                                        direction * width / 14
                                    }
                                ).togetherWith(
                                fadeOut(tween(160)) +
                                    slideOutHorizontally(tween(280)) { width ->
                                        -direction * width / 18
                                    },
                            )
                        },
                        label = "tabTransition",
                        modifier = Modifier.fillMaxSize(),
                    ) { current ->
                        when (current) {
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
                }

                FitNexDock(current = tab, onSelect = { tab = it })
            }
        }
    }
}
