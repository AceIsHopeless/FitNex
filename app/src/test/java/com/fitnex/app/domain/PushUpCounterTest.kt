package com.fitnex.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PushUpCounterTest {

    private class Driver {
        val counter = PushUpCounter()
        var now = 0L

        fun frame(angle: Float, shoulderY: Float = 0.30f): Boolean {
            val counted = counter.onPose(PushUpPose(angle, angle, shoulderY), now)
            now += FRAME_MS
            return counted
        }

        fun top(shoulderY: Float = 0.30f) = frame(168f, shoulderY)

        fun bottom(shoulderY: Float = 0.52f) = frame(80f, shoulderY)

        fun rep(topY: Float = 0.30f, bottomY: Float = 0.52f) {
            val travel = bottomY - topY
            top(topY)
            top(topY)
            frame(140f, topY + travel * 0.45f)
            frame(115f, topY + travel * 0.80f)
            bottom(bottomY - 0.02f)
            bottom(bottomY)
            frame(120f, topY + travel * 0.55f)
            frame(155f, topY + travel * 0.10f)
            top(topY)
        }

        companion object {
            const val FRAME_MS = 200L
        }
    }

    @Test
    fun `one complete push up counts exactly once`() {
        val driver = Driver()
        driver.rep()
        assertEquals(1, driver.counter.count)

        repeat(10) { driver.top() }
        assertEquals(1, driver.counter.count)
    }

    @Test
    fun `partial push up counts zero times`() {
        val driver = Driver()
        repeat(2) { driver.top() }
        repeat(5) { driver.frame(125f, 0.40f) }
        repeat(3) { driver.top() }
        assertEquals(0, driver.counter.count)
    }

    @Test
    fun `bouncing rep that is too fast counts zero times`() {
        val counter = PushUpCounter()
        var t = 0L
        fun f(angle: Float, shoulderY: Float) {
            counter.onPose(PushUpPose(angle, angle, shoulderY), t)
            t += 40
        }
        repeat(2) { f(168f, 0.30f) }
        f(80f, 0.50f)
        f(80f, 0.52f)
        repeat(3) { f(168f, 0.30f) }
        assertEquals(0, counter.count)
    }

    @Test
    fun `rep without shoulder drop counts zero times`() {
        val driver = Driver()
        driver.rep(topY = 0.30f, bottomY = 0.30f)
        assertEquals(0, driver.counter.count)
    }

    @Test
    fun `landmark loss discards the rep in progress`() {
        val driver = Driver()
        repeat(2) { driver.top() }
        repeat(2) { driver.bottom() }
        assertEquals(RepPhase.DOWN, driver.counter.phase)

        repeat(4) {
            driver.counter.onPose(null, driver.now)
            driver.now += 200
        }
        assertEquals(RepPhase.IDLE, driver.counter.phase)

        repeat(3) { driver.top() }
        assertEquals(0, driver.counter.count)

        driver.rep()
        assertEquals(1, driver.counter.count)
    }

    @Test
    fun `three valid push ups deal exactly six damage`() {
        val driver = Driver()
        val battle = Battle(BossCatalog.slime, clock = { 0L })

        repeat(3) {
            driver.rep()
            battle.onPushUp()
        }

        assertEquals(3, driver.counter.count)
        assertEquals(6, battle.damage)
        assertEquals(14, battle.hp)
    }

    @Test
    fun `starting in the down position does not count`() {
        val driver = Driver()
        repeat(3) { driver.bottom() }
        repeat(3) { driver.top() }
        assertEquals(0, driver.counter.count)
    }

    @Test
    fun `counter reports tracking state`() {
        val counter = PushUpCounter()
        counter.onPose(PushUpPose(168f, 168f, 0.3f), 0L)
        assertTrue(counter.isTracking)
        counter.onPose(null, 200L)
        assertFalse(counter.isTracking)
    }
}
