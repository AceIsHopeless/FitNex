package com.fitnex.app.domain

import java.util.UUID
import kotlin.math.min

enum class BattleStatus { RUNNING, VICTORY, TIME_UP, ABANDONED }

const val DAMAGE_PER_PUSH_UP = 2
const val EXTENSION_SECONDS = 60

/**
 * A single battle attempt against one boss.
 *
 * The battle is pure Kotlin with an injectable clock so the timer rules can be
 * unit tested: every valid push-up deals [DAMAGE_PER_PUSH_UP] damage, running
 * out of time allows exactly one extra minute, and accepting it restarts the
 * fight with the boss back at full health.
 */
class Battle(
    val boss: Boss,
    val battleId: String = UUID.randomUUID().toString(),
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private val battleStartedAt: Long = clock()
    private var roundStartedAt: Long = battleStartedAt
    private var roundLimitMs: Long = boss.timeLimitSeconds * 1000L

    var damage: Int = 0
        private set

    var totalDamage: Int = 0
        private set

    var pushUps: Int = 0
        private set

    var status: BattleStatus = BattleStatus.RUNNING
        private set

    var extensionUsed: Boolean = false
        private set

    val hp: Int get() = (boss.hp - damage).coerceAtLeast(0)

    val remainingMs: Long
        get() = (roundLimitMs - (clock() - roundStartedAt)).coerceAtLeast(0L)

    val secondsRemaining: Int
        get() = ((remainingMs + 999L) / 1000L).toInt()

    /** Total seconds spent in the fight, including any extension round. */
    val elapsedSeconds: Int
        get() = ((clock() - battleStartedAt) / 1000L).toInt()

    /** Registers one valid push-up. Returns the damage actually applied. */
    fun onPushUp(): Int {
        if (status != BattleStatus.RUNNING) return 0
        pushUps++
        val applied = min(DAMAGE_PER_PUSH_UP, hp)
        damage += applied
        totalDamage += applied
        if (hp == 0) status = BattleStatus.VICTORY
        return applied
    }

    /** Advances the timer. Call frequently while the battle is on screen. */
    fun tick(): BattleStatus {
        if (status == BattleStatus.RUNNING && remainingMs <= 0L) {
            status = BattleStatus.TIME_UP
        }
        return status
    }

    /**
     * Accepts the extra minute: the boss is restored to full health and the
     * player gets [EXTENSION_SECONDS] more seconds. Allowed only once.
     */
    fun extendRound(): Boolean {
        if (status != BattleStatus.TIME_UP || extensionUsed) return false
        extensionUsed = true
        damage = 0
        roundStartedAt = clock()
        roundLimitMs = EXTENSION_SECONDS * 1000L
        status = BattleStatus.RUNNING
        return true
    }

    fun abandon() {
        if (status == BattleStatus.RUNNING || status == BattleStatus.TIME_UP) {
            status = BattleStatus.ABANDONED
        }
    }
}
