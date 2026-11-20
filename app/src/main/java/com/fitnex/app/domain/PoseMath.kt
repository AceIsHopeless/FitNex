package com.fitnex.app.domain

import kotlin.math.acos
import kotlin.math.hypot

/** A single body landmark in normalized image coordinates (0..1). */
data class PosePoint(
    val x: Float,
    val y: Float,
    val visibility: Float = 1f,
)

object PoseMath {

    /**
     * Angle in degrees at [joint] between the segments [joint] -> [a] and [joint] -> [b].
     * 180 degrees means the limb is fully straight, 0 means fully folded.
     * Returns NaN when the geometry is degenerate.
     */
    fun angleAt(a: PosePoint, joint: PosePoint, b: PosePoint): Float {
        val v1x = a.x - joint.x
        val v1y = a.y - joint.y
        val v2x = b.x - joint.x
        val v2y = b.y - joint.y
        val n1 = hypot(v1x, v1y)
        val n2 = hypot(v2x, v2y)
        if (n1 < 1e-6f || n2 < 1e-6f) return Float.NaN
        val cos = ((v1x * v2x + v1y * v2y) / (n1 * n2)).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cos).toDouble()).toFloat()
    }
}
