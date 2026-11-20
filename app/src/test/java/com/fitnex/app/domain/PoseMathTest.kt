package com.fitnex.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PoseMathTest {

    @Test
    fun `straight arm measures 180 degrees`() {
        val angle = PoseMath.angleAt(
            PosePoint(0.5f, 0.1f),
            PosePoint(0.5f, 0.3f),
            PosePoint(0.5f, 0.5f),
        )
        assertEquals(180f, angle, 0.5f)
    }

    @Test
    fun `right angle measures 90 degrees`() {
        val angle = PoseMath.angleAt(
            PosePoint(0.2f, 0.3f),
            PosePoint(0.5f, 0.3f),
            PosePoint(0.5f, 0.6f),
        )
        assertEquals(90f, angle, 0.5f)
    }

    @Test
    fun `folded arm measures close to zero`() {
        val angle = PoseMath.angleAt(
            PosePoint(0.5f, 0.0f),
            PosePoint(0.5f, 0.3f),
            PosePoint(0.51f, 0.05f),
        )
        assertTrue("expected small angle but was $angle", angle < 15f)
    }

    @Test
    fun `degenerate geometry returns NaN`() {
        val angle = PoseMath.angleAt(
            PosePoint(0.5f, 0.3f),
            PosePoint(0.5f, 0.3f),
            PosePoint(0.5f, 0.6f),
        )
        assertTrue(angle.isNaN())
    }
}
