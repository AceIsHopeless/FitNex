package com.fitnex.app.domain

/** Where the athlete is inside a single repetition. */
enum class RepPhase { IDLE, UP, DOWN }

/**
 * One pose observation, reduced to what the rep detector needs.
 * Angles are elbow angles in degrees; null means the landmark was not usable.
 * [shoulderY] is the normalized (0 = top of frame) shoulder height.
 */
data class PushUpPose(
    val leftElbowAngle: Float? = null,
    val rightElbowAngle: Float? = null,
    val shoulderY: Float? = null,
)

data class PushUpConfig(
    /** Elbow angle (degrees) that counts as the bottom of a push-up. */
    val downAngle: Float = 95f,
    /** Elbow angle (degrees) that counts as the locked-out top of a push-up. */
    val upAngle: Float = 150f,
    /** Frames that must agree before a phase change is accepted. */
    val minFramesInPhase: Int = 2,
    /** A rep faster than this is treated as a bounce, not a completed rep. */
    val minRepDurationMs: Long = 350,
    /** A gap in landmarks longer than this discards the half-finished rep. */
    val maxLandmarkGapMs: Long = 800,
    /** Minimum time between two counted reps. */
    val cooldownMs: Long = 300,
    /** If both arms are visible and differ by more than this, the frame is rejected. */
    val maxArmAsymmetryDeg: Float = 55f,
    /** How far the shoulders must sink (normalized) for a rep to count. */
    val minShoulderDrop: Float = 0.012f,
)

/**
 * State machine that counts only complete push-ups.
 *
 * A rep is counted when all of the following hold:
 *  1. the athlete was locked out at the top (elbows >= [PushUpConfig.upAngle]),
 *  2. the elbows folded to/below [PushUpConfig.downAngle] for at least
 *     [PushUpConfig.minFramesInPhase] frames,
 *  3. the athlete then locked out again after at least [PushUpConfig.minRepDurationMs],
 *  4. the shoulders visibly moved down between the top and the bottom,
 *  5. no landmarks were lost during the movement and the cooldown has elapsed.
 *
 * Anything else (partial range, bouncing, arm-only movement, tracking loss) is
 * ignored, so the count never inflates.
 */
class PushUpCounter(private val config: PushUpConfig = PushUpConfig()) {

    var count: Int = 0
        private set

    var phase: RepPhase = RepPhase.IDLE
        private set

    var isTracking: Boolean = false
        private set

    /** Human readable hint for the current form state. */
    var feedback: String = "Get into position"
        private set

    private var lastTimestampMs: Long? = null
    private var phaseEnteredMs: Long = 0L
    private var candidateFrames: Int = 0
    private var missedFrames: Int = 0
    private var lastCountMs: Long? = null
    private var topShoulderY: Float? = null
    private var bottomShoulderY: Float? = null

    /**
     * Feed one pose sample. Returns true when this sample completed a valid rep.
     */
    fun onPose(pose: PushUpPose?, timestampMs: Long): Boolean {
        val previous = lastTimestampMs
        lastTimestampMs = timestampMs

        if (previous != null && timestampMs - previous > config.maxLandmarkGapMs) {
            // Tracking was interrupted; never stitch a rep across the gap.
            resetCycle()
        }

        val angle = combinedAngle(pose)
        if (angle == null) {
            isTracking = false
            missedFrames++
            if (missedFrames >= MISSED_FRAMES_TO_RESET) resetCycle()
            feedback = "No body detected"
            return false
        }

        missedFrames = 0
        isTracking = true
        val shoulderY = pose?.shoulderY
        var counted = false

        when (phase) {
            RepPhase.IDLE -> {
                if (angle >= config.upAngle) {
                    candidateFrames++
                    trackTop(shoulderY)
                    if (candidateFrames >= config.minFramesInPhase) {
                        enterPhase(RepPhase.UP, timestampMs, shoulderY)
                    }
                } else {
                    candidateFrames = 0
                }
            }

            RepPhase.UP -> {
                // Only a locked-out arm defines the top reference; recording the
                // shoulder mid-descent would shrink the measured range.
                if (angle >= config.upAngle) trackTop(shoulderY)
                if (angle <= config.downAngle) {
                    candidateFrames++
                    if (candidateFrames >= config.minFramesInPhase) {
                        enterPhase(RepPhase.DOWN, timestampMs, shoulderY)
                    }
                } else {
                    candidateFrames = 0
                }
            }

            RepPhase.DOWN -> {
                if (shoulderY != null && angle <= config.downAngle) {
                    bottomShoulderY = maxOf(bottomShoulderY ?: shoulderY, shoulderY)
                }
                if (angle >= config.upAngle) {
                    candidateFrames++
                    if (candidateFrames >= config.minFramesInPhase) {
                        val duration = timestampMs - phaseEnteredMs
                        val cooledDown = lastCountMs?.let { timestampMs - it >= config.cooldownMs } ?: true
                        val fullDepth = shouldersDroppedEnough()
                        if (duration >= config.minRepDurationMs && cooledDown && fullDepth) {
                            count++
                            lastCountMs = timestampMs
                            counted = true
                        }
                        enterPhase(RepPhase.UP, timestampMs, shoulderY)
                    }
                } else {
                    candidateFrames = 0
                }
            }
        }

        feedback = when {
            !isTracking -> "No body detected"
            phase == RepPhase.DOWN -> "Drive up"
            phase == RepPhase.UP -> "Lower your chest"
            else -> "Get into position"
        }
        return counted
    }

    fun reset() {
        count = 0
        lastCountMs = null
        lastTimestampMs = null
        isTracking = false
        feedback = "Get into position"
        resetCycle()
    }

    private fun resetCycle() {
        phase = RepPhase.IDLE
        candidateFrames = 0
        missedFrames = 0
        topShoulderY = null
        bottomShoulderY = null
        isTracking = false
        lastTimestampMs = null
    }

    private fun enterPhase(newPhase: RepPhase, timestampMs: Long, shoulderY: Float?) {
        phase = newPhase
        phaseEnteredMs = timestampMs
        candidateFrames = 0
        when (newPhase) {
            RepPhase.UP -> {
                topShoulderY = shoulderY
                bottomShoulderY = null
            }
            RepPhase.DOWN -> bottomShoulderY = shoulderY
            RepPhase.IDLE -> {
                topShoulderY = null
                bottomShoulderY = null
            }
        }
    }

    private fun trackTop(shoulderY: Float?) {
        if (shoulderY == null) return
        topShoulderY = topShoulderY?.let { minOf(it, shoulderY) } ?: shoulderY
    }

    private fun shouldersDroppedEnough(): Boolean {
        val top = topShoulderY ?: return true
        val bottom = bottomShoulderY ?: return true
        return bottom - top >= config.minShoulderDrop
    }

    private fun combinedAngle(pose: PushUpPose?): Float? {
        if (pose == null) return null
        val left = pose.leftElbowAngle?.takeIf { !it.isNaN() }
        val right = pose.rightElbowAngle?.takeIf { !it.isNaN() }
        return when {
            left != null && right != null ->
                if (kotlin.math.abs(left - right) > config.maxArmAsymmetryDeg) null else (left + right) / 2f
            else -> left ?: right
        }
    }

    private companion object {
        const val MISSED_FRAMES_TO_RESET = 3
    }
}
