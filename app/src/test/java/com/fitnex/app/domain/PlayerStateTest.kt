package com.fitnex.app.domain

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStateTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun result(
        battleId: String = "battle-1",
        boss: Boss = BossCatalog.slime,
        pushUps: Int = 10,
        damage: Int = 20,
        victory: Boolean = true,
    ) = BattleResult(
        battleId = battleId,
        boss = boss,
        pushUps = pushUps,
        damage = damage,
        secondsUsed = 90,
        victory = victory,
        timestampMillis = 1_000L,
    )

    @Test
    fun `defeating a boss grants exactly one level`() {
        val after = PlayerState().applyBattleResult(result())
        assertEquals(2, after.level)
        assertEquals(1, after.bossesDefeated)
        assertEquals(1, after.currentBossIndex)
    }

    @Test
    fun `the same battle can never reward twice`() {
        val first = PlayerState().applyBattleResult(result())
        val second = first.applyBattleResult(result())
        assertEquals(first, second)
        assertEquals(2, second.level)
        assertEquals(1, second.history.size)
    }

    @Test
    fun `defeat records history without a level`() {
        val after = PlayerState(level = 3).applyBattleResult(result(victory = false, pushUps = 7, damage = 14))
        assertEquals(3, after.level)
        assertEquals(0, after.bossesDefeated)
        assertEquals(1, after.history.size)
        assertFalse(after.history.first().victory)
        assertEquals(7, after.history.first().pushUps)
    }

    @Test
    fun `campaign advances through the boss ladder`() {
        var state = PlayerState()
        BossCatalog.all.dropLast(1).forEach { boss ->
            state = state.applyBattleResult(result(battleId = "b-${boss.id}", boss = boss))
        }
        assertEquals(4, state.currentBossIndex)
        assertEquals(BossCatalog.darkKnight, state.currentBoss)
        assertEquals(5, state.level)
        assertEquals(Rank.C_CLASS, state.rank)
        assertEquals(4, state.defeatedBossIds.size)
    }

    @Test
    fun `dark knight remains the final boss`() {
        val state = PlayerState(level = 9, currentBossIndex = 4)
        val after = state.applyBattleResult(result(battleId = "final", boss = BossCatalog.darkKnight))
        assertEquals(4, after.currentBossIndex)
        assertEquals("Dark Knight", after.currentBoss.name)
        assertEquals(10, after.level)
        assertEquals(Rank.A_CLASS, after.rank)
    }

    @Test
    fun `boss hp and time limits match the campaign table`() {
        assertEquals(listOf(20, 40, 60, 80, 100), BossCatalog.all.map { it.hp })
        assertEquals(listOf(120, 180, 240, 300, 360), BossCatalog.all.map { it.timeLimitSeconds })
    }

    @Test
    fun `rank thresholds`() {
        assertEquals(Rank.NO_RANK, Rank.forLevel(1))
        assertEquals(Rank.NO_RANK, Rank.forLevel(2))
        assertEquals(Rank.C_CLASS, Rank.forLevel(3))
        assertEquals(Rank.B_CLASS, Rank.forLevel(6))
        assertEquals(Rank.A_CLASS, Rank.forLevel(10))
        assertEquals(Rank.S_CLASS, Rank.forLevel(15))
        assertEquals(Rank.S_CLASS, Rank.forLevel(99))
    }

    @Test
    fun `player state survives a save and load round trip`() {
        var state = PlayerState(level = 4, lifetimePushUps = 128, bossesDefeated = 3)
        state = state.applyBattleResult(result(battleId = "saved", boss = BossCatalog.zombie))

        val encoded = json.encodeToString(PlayerState.serializer(), state)
        val decoded = json.decodeFromString(PlayerState.serializer(), encoded)

        assertEquals(state, decoded)
        assertEquals(128L, decoded.lifetimePushUps)
        assertEquals(1, decoded.history.size)
        assertEquals("zombie", decoded.history.first().bossId)
    }

    @Test
    fun `older saves without newer fields still load`() {
        val decoded = json.decodeFromString(
            PlayerState.serializer(),
            """{"level":7,"lifetimePushUps":250,"bossesDefeated":2}""",
        )
        assertEquals(7, decoded.level)
        assertEquals(250L, decoded.lifetimePushUps)
        assertEquals(0, decoded.currentBossIndex)
        assertTrue(decoded.history.isEmpty())
    }
}
