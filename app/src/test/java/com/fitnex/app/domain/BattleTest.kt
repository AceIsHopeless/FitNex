package com.fitnex.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleTest {

    private var now = 0L

    private fun battle(boss: Boss = BossCatalog.slime) = Battle(boss, clock = { now })

    @Test
    fun `boss starts at full health`() {
        val battle = battle()
        assertEquals(20, battle.hp)
        assertEquals(0, battle.damage)
        assertEquals(BattleStatus.RUNNING, battle.status)
        assertEquals(120, battle.secondsRemaining)
    }

    @Test
    fun `every valid push up deals two damage`() {
        val battle = battle()
        assertEquals(2, battle.onPushUp())
        assertEquals(2, battle.damage)
        assertEquals(2, battle.onPushUp())
        assertEquals(4, battle.damage)
    }

    @Test
    fun `timer counts down and expires`() {
        val battle = battle()
        now = 60_000
        assertEquals(60, battle.secondsRemaining)
        assertEquals(BattleStatus.RUNNING, battle.tick())

        now = 120_000
        assertEquals(0, battle.secondsRemaining)
        assertEquals(BattleStatus.TIME_UP, battle.tick())
    }

    @Test
    fun `extra minute restores full health and runs exactly one more minute`() {
        val battle = battle()
        repeat(5) { battle.onPushUp() }
        assertEquals(10, battle.hp)

        now = 120_000
        assertEquals(BattleStatus.TIME_UP, battle.tick())

        assertTrue(battle.extendRound())
        assertEquals(BattleStatus.RUNNING, battle.status)
        assertEquals(20, battle.hp)
        assertEquals(0, battle.damage)
        assertEquals(60, battle.secondsRemaining)
        assertTrue(battle.extensionUsed)

        now = 180_000
        assertEquals(BattleStatus.TIME_UP, battle.tick())
        assertFalse(battle.extendRound())
    }

    @Test
    fun `extension is only offered when the timer expired`() {
        val battle = battle()
        assertFalse(battle.extendRound())
        assertEquals(BattleStatus.RUNNING, battle.status)
    }

    @Test
    fun `damage is capped at boss hp and victory is final`() {
        val battle = battle()
        repeat(50) { battle.onPushUp() }
        assertEquals(BattleStatus.VICTORY, battle.status)
        assertEquals(0, battle.hp)
        assertEquals(20, battle.damage)

        assertEquals(0, battle.onPushUp())
        assertEquals(20, battle.damage)
        assertEquals(BattleStatus.VICTORY, battle.status)
    }

    @Test
    fun `new battle resets boss health`() {
        val first = battle()
        repeat(5) { first.onPushUp() }
        assertEquals(10, first.hp)

        val second = battle()
        assertEquals(20, second.hp)
        assertEquals(0, second.pushUps)
    }

    @Test
    fun `dark knight follows campaign stats`() {
        val battle = battle(BossCatalog.darkKnight)
        assertEquals(100, battle.hp)
        assertEquals(360, battle.secondsRemaining)
    }
}
