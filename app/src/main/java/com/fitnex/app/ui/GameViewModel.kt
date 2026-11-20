package com.fitnex.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitnex.app.data.GameRepository
import com.fitnex.app.data.GameSettings
import com.fitnex.app.data.SettingsRepository
import com.fitnex.app.domain.Battle
import com.fitnex.app.domain.BattleResult
import com.fitnex.app.domain.BattleStatus
import com.fitnex.app.domain.Boss
import com.fitnex.app.domain.BossCatalog
import com.fitnex.app.domain.PlayerState
import com.fitnex.app.domain.Title
import com.fitnex.app.domain.TitleCatalog
import com.fitnex.app.domain.applyBattleResult
import com.fitnex.app.domain.equipTitle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class BattleUi(
    val battleId: String,
    val boss: Boss,
    val level: Int,
    val equippedTitle: Title?,
    val hp: Int,
    val maxHp: Int,
    val secondsRemaining: Int,
    val pushUps: Int,
    val damage: Int,
    val status: BattleStatus,
    val extensionUsed: Boolean,
)

data class BattleSummary(
    val bossName: String,
    val victory: Boolean,
    val pushUps: Int,
    val damage: Int,
    val secondsUsed: Int,
    val levelBefore: Int,
    val levelAfter: Int,
    val nextBossName: String,
    val unlockedTitle: Title? = null,
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application.applicationContext)
    private val settingsRepository = SettingsRepository(application.applicationContext)

    val playerState: StateFlow<PlayerState> = repository.playerState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PlayerState(),
    )

    val settings: StateFlow<GameSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = GameSettings(),
    )

    var battleUi by mutableStateOf<BattleUi?>(null)
        private set

    var battleSummary by mutableStateOf<BattleSummary?>(null)
        private set

    private var battle: Battle? = null
    private var tickerJob: Job? = null

    fun startBattle() {
        if (battleUi != null) return
        battleSummary = null
        val state = playerState.value
        battle = Battle(state.currentBoss)
        refresh()
        startTicker()
    }

    /** Called once per detected, validated push-up. */
    fun registerPushUp() {
        val current = battle ?: return
        if (current.status != BattleStatus.RUNNING) return
        val applied = current.onPushUp()
        if (applied > 0) {
            viewModelScope.launch { repository.addPushUp() }
        }
        refresh()
        if (current.status == BattleStatus.VICTORY) completeBattle()
    }

    fun acceptExtension() {
        val current = battle ?: return
        if (!current.extendRound()) return
        refresh()
        startTicker()
    }

    fun abandonBattle() {
        val current = battle ?: return
        current.abandon()
        completeBattle()
    }

    fun clearSummary() {
        battleSummary = null
    }

    fun equipTitle(titleId: String?) {
        viewModelScope.launch {
            repository.update { state -> state.equipTitle(titleId) }
        }
    }

    fun updateSettings(transform: (GameSettings) -> GameSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    fun resetProgress() {
        viewModelScope.launch { repository.reset() }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(200)
                val current = battle ?: break
                val status = current.tick()
                refresh()
                if (status != BattleStatus.RUNNING) break
            }
        }
    }

    private fun refresh() {
        val current = battle ?: run {
            battleUi = null
            return
        }
        val state = playerState.value
        battleUi = BattleUi(
            battleId = current.battleId,
            boss = current.boss,
            level = state.level,
            equippedTitle = state.equippedTitle,
            hp = current.hp,
            maxHp = current.boss.hp,
            secondsRemaining = current.secondsRemaining,
            pushUps = current.pushUps,
            damage = current.damage,
            status = current.status,
            extensionUsed = current.extensionUsed,
        )
    }

    private fun completeBattle() {
        val current = battle ?: return
        tickerJob?.cancel()

        val victory = current.status == BattleStatus.VICTORY
        val result = BattleResult(
            battleId = current.battleId,
            boss = current.boss,
            pushUps = current.pushUps,
            damage = current.totalDamage,
            secondsUsed = current.elapsedSeconds,
            victory = victory,
        )

        battle = null
        battleUi = null

        viewModelScope.launch {
            var levelBefore = 1
            var titlesBefore: List<String> = emptyList()
            val updated = repository.update { state ->
                levelBefore = state.level
                titlesBefore = state.unlockedTitleIds
                state.applyBattleResult(result)
            }
            val earned = if (victory) {
                TitleCatalog.forBoss(result.boss.id)?.takeIf { it.id !in titlesBefore }
            } else {
                null
            }
            battleSummary = BattleSummary(
                bossName = result.boss.name,
                victory = victory,
                pushUps = result.pushUps,
                damage = result.damage,
                secondsUsed = result.secondsUsed,
                levelBefore = levelBefore,
                levelAfter = updated.level,
                nextBossName = BossCatalog.byIndex(updated.currentBossIndex).name,
                unlockedTitle = earned,
            )
        }
    }
}
