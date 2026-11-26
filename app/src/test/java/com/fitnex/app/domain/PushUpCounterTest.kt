package com.fitnex.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class PushUpCounterTest {

    @Test
    fun `one complete push up counts exactly once`() {
        val driver = SideDriver()
        driver.settle()

        assertTrue(driver.rep())
        assertEquals(1, driver.counter.count)

        repeat(10) { driver.top() }
        assertEquals(1, driver.counter.count)
    }

    @Test
    fun `partial push up counts zero times`() {
        val driver = SideDriver()
        driver.settle()

        // Elbows only reach 125 degrees: nowhere near the 105 degree bottom.
        repeat(6) { driver.frame(125f) }
        repeat(4) { driver.top() }

        assertEquals(0, driver.counter.count)
    }

    @Test
    fun `starting in the down position does not count`() {
        val driver = SideDriver()

        repeat(3) { driver.bottom() }
        repeat(12) { driver.top() }

        assertEquals(0, driver.counter.count)
    }

    @Test
    fun `landmark loss discards the rep in progress`() {
        val driver = SideDriver()
        driver.settle()
        repeat(3) { driver.bottom() }
        assertEquals(RepPhase.DOWN, driver.counter.phase)

        repeat(8) {
            driver.counter.onPose(null, driver.now)
            driver.now += FRAME_MS
        }
        assertEquals(RepPhase.IDLE, driver.counter.phase)

        driver.settle()
        assertEquals(0, driver.counter.count)

        assertTrue(driver.rep())
        assertEquals(1, driver.counter.count)
    }

    @Test
    fun `counter reports tracking state`() {
        val counter = PushUpCounter()

        counter.onPose(sidePose(168f), 0L)
        assertTrue(counter.isTracking)

        counter.onPose(null, 200L)
        assertFalse(counter.isTracking)
    }

    @Test
    fun `side view ignores a vertical torso`() {
        val counter = PushUpCounter()
        var now = 0L
        val upright = sidePose(168f).let { pose ->
            pose.copy(
                leftHip = PosePoint(0.40f, 0.90f),
                rightHip = PosePoint(0.42f, 0.90f),
            )
        }

        repeat(14) {
            counter.onPose(upright, now)
            now += FRAME_MS
        }

        assertFalse(counter.isTracking)
        assertEquals(0, counter.count)
    }

    @Test
    fun `front view counts a rep from shoulder to wrist depth`() {
        val driver = FrontDriver()
        driver.settle()

        repeat(3) { driver.frame(0.5f) }
        var counted = false
        repeat(4) { if (driver.frame(1.0f)) counted = true }

        assertTrue(counted)
        assertEquals(1, driver.counter.count)
    }

    @Test
    fun `front view ignores a shallow dip`() {
        val driver = FrontDriver()
        driver.settle()

        repeat(6) { driver.frame(0.7f) }

        assertEquals(0, driver.counter.count)
    }

    @Test
    fun `three valid push ups deal exactly six damage`() {
        val driver = SideDriver()
        driver.settle()
        val battle = Battle(BossCatalog.slime, clock = { 0L })

        repeat(3) {
            assertTrue(driver.rep())
            battle.onPushUp()
        }

        assertEquals(3, driver.counter.count)
        assertEquals(6, battle.damage)
        assertEquals(14, battle.hp)
    }

    // ---------------------------------------------------------------- fixtures

    private class SideDriver {
        val counter = PushUpCounter()
        var now = 0L

        fun frame(angle: Float): Boolean {
            val counted = counter.onPose(sidePose(angle), now)
            now += FRAME_MS
            return counted
        }

        fun settle() = repeat(12) { frame(168f) }
        fun top() = frame(168f)
        fun bottom() = frame(80f)

        fun rep(): Boolean {
            repeat(3) { bottom() }
            var counted = false
            repeat(4) { if (top()) counted = true }
            return counted
        }
    }

    private class FrontDriver {
        val counter = PushUpCounter(PushUpConfig(requestedView = ViewMode.FRONT))
        var now = 0L

        fun frame(signal: Float): Boolean {
            val counted = counter.onPose(frontPose(signal), now)
            now += FRAME_MS
            return counted
        }

        fun settle() = repeat(12) { frame(1.0f) }
    }

    private companion object {
        const val FRAME_MS = 250L
    }
}

/**
 * Side-on athlete facing left: shoulders and hips are level, and the wrist is
 * placed so the shoulder-elbow-wrist joint measures exactly [angle].
 */
private fun sidePose(angle: Float): PushUpPose {
    fun arm(shoulderX: Float): Triple<PosePoint, PosePoint, PosePoint> {
        val shoulder = PosePoint(shoulderX, 0.40f)
        val elbow = PosePoint(shoulderX, 0.50f)
        val radians = Math.toRadians(angle.toDouble())
        // Elbow->shoulder points straight up. Rotating it by `angle` gives
        // elbow->wrist, so the joint's interior angle is exactly `angle`.
        val upX = 0.0
        val upY = -0.10
        val wrist = PosePoint(
            elbow.x + (upX * cos(radians) - upY * sin(radians)).toFloat(),
            elbow.y + (upX * sin(radians) + upY * cos(radians)).toFloat(),
        )
        return Triple(shoulder, elbow, wrist)
    }

    val left = arm(0.40f)
    val right = arm(0.42f)
    return PushUpPose(
        leftShoulder = left.first,
        leftElbow = left.second,
        leftWrist = left.third,
        leftHip = PosePoint(0.70f, 0.40f),
        rightShoulder = right.first,
        rightElbow = right.second,
        rightWrist = right.third,
        rightHip = PosePoint(0.72f, 0.40f),
    )
}

/** Front-on athlete: [signal] is shoulder-to-wrist depth in shoulder widths. */
private fun frontPose(signal: Float): PushUpPose {
    val shoulderY = 0.30f
    val width = 0.30f
    val wristY = shoulderY + signal * width
    return PushUpPose(
        leftShoulder = PosePoint(0.35f, shoulderY),
        rightShoulder = PosePoint(0.65f, shoulderY),
        leftWrist = PosePoint(0.35f, wristY),
        rightWrist = PosePoint(0.65f, wristY),
    )
}
