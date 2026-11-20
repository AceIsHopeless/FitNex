package com.fitnex.app.domain

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ProgressionTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun result(
        battleId: String,
        boss: Boss = BossCatalog.slime,
        pushUps: Int = 10,
        victory: Boolean = true,
        at: Long = 0L,
    ) = BattleResult(
        battleId = battleId,
        boss = boss,
        pushUps = pushUps,
        damage = pushUps * 2,
        secondsUsed = 60,
        victory = victory,
        timestampMillis = at,
    )

    @Test
    fun `first kill unlocks that boss title`() {
        val after = PlayerState().applyBattleResult(result("a"))
        assertEquals(listOf("slime_slayer"), after.unlockedTitleIds)
        assertEquals("Slime Slayer", after.unlockedTitles.single().name)
    }

    @Test
    fun `replaying a boss never duplicates its title`() {
        var state = PlayerState().applyBattleResult(result("a"))
        state = state.applyBattleResult(result("b"))
        // Each separate kill still grants its own level; the title is granted once.
        assertEquals(1, state.unlockedTitleIds.size)
        assertEquals(3, state.level)
    }

    @Test
    fun `defeat unlocks no title`() {
        val after = PlayerState().applyBattleResult(result("a", victory = false))
        assertEquals(0, after.unlockedTitleIds.size)
        assertNull(after.equippedTitle)
    }

    @Test
    fun `equipping a locked title is ignored`() {
        val state = PlayerState().equipTitle("vampire_hunter")
        assertNull(state.equippedTitleId)
        assertNull(state.equippedTitle)
    }

    @Test
    fun `equipped title persists and can be cleared`() {
        val state = PlayerState()
            .applyBattleResult(result("a"))
            .equipTitle("slime_slayer")

        val decoded = json.decodeFromString(
            PlayerState.serializer(),
            json.encodeToString(PlayerState.serializer(), state),
        )
        assertEquals("Slime Slayer", decoded.equippedTitle?.name)

        assertNull(decoded.equipTitle(null).equippedTitleId)
    }

    @Test
    fun `a loss counts an attempt but not a win`() {
        val after = PlayerState().applyBattleResult(result("a", victory = false))
        assertEquals(1, after.battleAttempts)
        assertEquals(0, after.battleWins)
        assertEquals(1, after.battlesLost)
        assertEquals(10, after.bestPushUpsInBattle)
    }

    @Test
    fun `best battle keeps the maximum`() {
        var state = PlayerState().applyBattleResult(result("a", pushUps = 12))
        state = state.applyBattleResult(result("b", pushUps = 5, victory = false))
        assertEquals(12, state.bestPushUpsInBattle)
        assertEquals(2, state.battleAttempts)
    }

    @Test
    fun `totals do not shrink when history is capped`() {
        var state = PlayerState()
        repeat(50) { index ->
            state = state.applyBattleResult(
                result("battle-$index", boss = BossCatalog.darkKnight, victory = false),
            )
        }
        assertEquals(50, state.battleAttempts)
        assertEquals(40, state.history.size)
        assertEquals(0, state.battleWins)
    }

    @Test
    fun `highest boss defeated tracks the furthest win`() {
        var state = PlayerState().applyBattleResult(result("a", boss = BossCatalog.slime))
        state = state.applyBattleResult(result("b", boss = BossCatalog.skeleton))
        assertEquals("Skeleton", state.highestBossDefeated?.name)
    }

    @Test
    fun `weekly chart buckets reps by local day`() {
        val zone = ZoneId.of("UTC")
        val today = LocalDate.of(2026, 3, 10)
        val now = today.atStartOfDay(zone).toInstant().toEpochMilli() + 12 * 3_600_000L
        val history = listOf(
            record(today, pushUps = 8, zone = zone),
            record(today, pushUps = 4, zone = zone),
            record(today.minusDays(1), pushUps = 7, zone = zone),
            record(today.minusDays(9), pushUps = 99, zone = zone),
        )

        val days = repsByDay(history, dayCount = 7, nowMillis = now, zone = zone)

        assertEquals(7, days.size)
        assertEquals(12, days.last().pushUps)
        assertEquals(7, days[5].pushUps)
        assertEquals(0, days.first().pushUps)
        assertEquals(19, days.sumOf { it.pushUps })
    }

    @Test
    fun `empty history produces a flat week`() {
        val days = repsByDay(emptyList(), dayCount = 7, nowMillis = 0L, zone = ZoneId.of("UTC"))
        assertEquals(7, days.size)
        assertEquals(0, days.sumOf { it.pushUps })
    }

    @Test
    fun `title catalogue covers every boss`() {
        BossCatalog.all.forEach { boss ->
            assertNotNull("Missing title for ${boss.name}", TitleCatalog.forBoss(boss.id))
        }
        assertEquals(BossCatalog.all.size, TitleCatalog.all.size)
    }

    private fun record(day: LocalDate, pushUps: Int, zone: ZoneId) = WorkoutRecord(
        battleId = "battle-$day-$pushUps",
        bossId = "slime",
        bossName = "Slime",
        pushUps = pushUps,
        damage = pushUps * 2,
        secondsUsed = 60,
        victory = true,
        timestampMillis = day.atTime(10, 0).atZone(zone).toInstant().toEpochMilli(),
    )
}
