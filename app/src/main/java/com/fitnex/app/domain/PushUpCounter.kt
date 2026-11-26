package com.fitnex.app.domain

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

/** Where the athlete is inside a single repetition. */
enum class RepPhase { IDLE, UP, DOWN }

/** Which side of the athlete the camera is looking at. */
enum class ViewMode { AUTO, FRONT, SIDE }

/**
 * One pose observation, reduced to the landmarks the rep detector needs.
 * Landmarks the detector was not confident about are null.
 *
 * Coordinates are normalized to the upright image (0..1).
 */
data class PushUpPose(
    val leftShoulder: PosePoint? = null,
    val leftElbow: PosePoint? = null,
    val leftWrist: PosePoint? = null,
    val leftHip: PosePoint? = null,
    val rightShoulder: PosePoint? = null,
    val rightElbow: PosePoint? = null,
    val rightWrist: PosePoint? = null,
    val rightHip: PosePoint? = null,
) {
    val leftElbowAngle: Float? get() = angleOf(leftShoulder, leftElbow, leftWrist)
    val rightElbowAngle: Float? get() = angleOf(rightShoulder, rightElbow, rightWrist)
    val shoulderY: Float?
        get() {
            val ys = listOfNotNull(leftShoulder?.y, rightShoulder?.y)
            return if (ys.isEmpty()) null else ys.average().toFloat()
        }
}

data class PushUpConfig(
    /** Force [ViewMode.FRONT] or [ViewMode.SIDE], or let the counter decide. */
    val requestedView: ViewMode = ViewMode.AUTO,
    /** 0..1: higher reacts faster, lower is steadier. */
    val smoothing: Float = 0.45f,
    /** SIDE view: elbow angle that marks the bottom of the rep. */
    val sideDownAngle: Float = 105f,
    /** SIDE view: elbow angle that marks the locked-out top. */
    val sideUpAngle: Float = 150f,
    /** Body must be closer to horizontal than this, or the frame is ignored. */
    val maxTorsoTiltDeg: Float = 50f,
    /** FRONT view: signal below this fraction of the top value is the bottom. */
    val frontDownFraction: Float = 0.60f,
    /** FRONT view: signal above this fraction of the top value is the top. */
    val frontUpFraction: Float = 0.85f,
    /** Shoulder width / torso length above this reads as a front view. */
    val frontRatio: Float = 1.2f,
    /** ... and below this reads as a side view. */
    val sideRatio: Float = 0.6f,
    /** Frames in a row at the new view before AUTO switches. */
    val viewSwitchFrames: Int = 10,
    /** Frames at the top before counting starts or restarts. */
    val steadyFrames: Int = 12,
    /** Ignore up/down flips faster than this. */
    val minPhaseMs: Long = 200L,
    /** Lose the athlete this long and the half-finished rep is discarded. */
    val lostTimeoutMs: Long = 1500L,
    /** How much better the other arm must score before SIDE view switches. */
    val switchMargin: Float = 0.3f,
)

/**
 * Counts push-ups from pose landmarks.
 *
 * SIDE view watches the elbow angle (shoulder-elbow-wrist) of the arm facing
 * the camera: a rep goes below [PushUpConfig.sideDownAngle] and returns above
 * [PushUpConfig.sideUpAngle].
 *
 * FRONT view cannot use the elbow angle (the arm folds toward the camera), so
 * it watches how far the shoulders sink toward the wrists, in units of
 * shoulder width. The top value is learned while holding the top position.
 *
 * [ViewMode.AUTO] picks FRONT or SIDE from the ratio of shoulder width to torso
 * length. A rep only counts after the athlete has settled at the top, so
 * starting on the floor or losing the body mid-rep never inflates the count.
 *
 * Not thread-safe: feed every pose from one thread.
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

    /** The view currently in use; never [ViewMode.AUTO]. */
    var view: ViewMode = if (config.requestedView == ViewMode.FRONT) ViewMode.FRONT else ViewMode.SIDE
        private set

    private var viewVotes = 0
    private var smoothed: Float? = null
    private var topRef = 0f
    private var steadyFrames = 0
    private var preferLeftArm = true
    private var lastPhaseChangeMs = 0L
    private var lastInPositionMs = 0L

    /**
     * Feed one pose sample. [timestampMs] must be a monotonic clock
     * (SystemClock.elapsedRealtime()). Returns true when this sample completed
     * a valid rep.
     */
    fun onPose(pose: PushUpPose?, timestampMs: Long): Boolean {
        if (pose == null) {
            isTracking = false
            if (timestampMs - lastInPositionMs > config.lostTimeoutMs) resetTracking()
            feedback = message()
            return false
        }

        updateView(pose)

        val raw = if (view == ViewMode.SIDE) sideSignal(pose) else frontSignal(pose)
        if (raw == null) {
            isTracking = false
            if (timestampMs - lastInPositionMs > config.lostTimeoutMs) resetTracking()
            feedback = message()
            return false
        }

        lastInPositionMs = timestampMs
        isTracking = true
        val value = smoothed?.let { it + config.smoothing * (raw - it) } ?: raw
        smoothed = value
        val counted = step(value, timestampMs)
        feedback = message()
        return counted
    }

    fun reset() {
        count = 0
        lastInPositionMs = 0L
        viewVotes = 0
        view = if (config.requestedView == ViewMode.FRONT) ViewMode.FRONT else ViewMode.SIDE
        resetTracking()
        feedback = message()
    }

    // ---------------------------------------------------------------- state machine

    private fun step(value: Float, nowMs: Long): Boolean {
        when (phase) {
            RepPhase.IDLE -> {
                // Require a steady top position before counting starts.
                val ready = if (view == ViewMode.SIDE) {
                    value > config.sideUpAngle
                } else {
                    if (topRef == 0f) topRef = value
                    val steady = abs(value - topRef) < 0.08f * topRef
                    topRef += 0.1f * (value - topRef)
                    steady
                }
                steadyFrames = if (ready) steadyFrames + 1 else 0
                if (steadyFrames >= config.steadyFrames) {
                    phase = RepPhase.UP
                    lastPhaseChangeMs = nowMs
                }
            }

            RepPhase.UP -> {
                trackTop(value)
                if (value < downThreshold() && nowMs - lastPhaseChangeMs > config.minPhaseMs) {
                    phase = RepPhase.DOWN
                    lastPhaseChangeMs = nowMs
                }
            }

            RepPhase.DOWN -> {
                if (value > upThreshold() && nowMs - lastPhaseChangeMs > config.minPhaseMs) {
                    phase = RepPhase.UP
                    lastPhaseChangeMs = nowMs
                    count++
                    return true
                }
            }
        }
        return false
    }

    private fun downThreshold(): Float =
        if (view == ViewMode.SIDE) config.sideDownAngle else config.frontDownFraction * topRef

    private fun upThreshold(): Float =
        if (view == ViewMode.SIDE) config.sideUpAngle else config.frontUpFraction * topRef

    /** FRONT view: follow the top value as the athlete settles or shifts. */
    private fun trackTop(value: Float) {
        if (view != ViewMode.FRONT) return
        topRef = when {
            value > topRef -> topRef + 0.2f * (value - topRef)
            value > config.frontUpFraction * topRef -> topRef + 0.02f * (value - topRef)
            else -> topRef
        }
    }

    private fun resetTracking() {
        phase = RepPhase.IDLE
        smoothed = null
        topRef = 0f
        steadyFrames = 0
    }

    // ---------------------------------------------------------------- view detection

    private fun updateView(pose: PushUpPose) {
        if (config.requestedView != ViewMode.AUTO) return

        val ls = pose.leftShoulder ?: return
        val rs = pose.rightShoulder ?: return
        val lh = pose.leftHip ?: return
        val rh = pose.rightHip ?: return

        val shoulderWidth = dist(ls, rs)
        val torso = dist(mid(ls, rs), mid(lh, rh))
        if (torso < MIN_DISTANCE) return

        // Front: the torso is foreshortened and the shoulders look wide.
        // Side: the shoulders overlap and the torso reads long.
        val ratio = shoulderWidth / torso
        val candidate = when {
            ratio > config.frontRatio -> ViewMode.FRONT
            ratio < config.sideRatio -> ViewMode.SIDE
            else -> return // in-between: keep the current view
        }

        if (candidate == view) {
            viewVotes = 0
        } else if (++viewVotes >= config.viewSwitchFrames) {
            view = candidate
            viewVotes = 0
            resetTracking()
        }
    }

    // ---------------------------------------------------------------- signals

    private class Arm(
        val shoulder: PosePoint,
        val elbow: PosePoint,
        val wrist: PosePoint,
        val hip: PosePoint,
        val score: Float,
    )

    private fun arm(
        shoulder: PosePoint?,
        elbow: PosePoint?,
        wrist: PosePoint?,
        hip: PosePoint?,
    ): Arm? {
        if (shoulder == null || elbow == null || wrist == null || hip == null) return null
        return Arm(shoulder, elbow, wrist, hip, shoulder.visibility + elbow.visibility + wrist.visibility)
    }

    /**
     * SIDE view: elbow angle in degrees (~170 at the top, ~70-90 at the bottom),
     * or null when this is not a push-up pose.
     */
    private fun sideSignal(pose: PushUpPose): Float? {
        val left = arm(pose.leftShoulder, pose.leftElbow, pose.leftWrist, pose.leftHip)
        val right = arm(pose.rightShoulder, pose.rightElbow, pose.rightWrist, pose.rightHip)

        val arm = when {
            left == null -> right
            right == null -> left
            preferLeftArm -> if (right.score > left.score + config.switchMargin) right else left
            else -> if (left.score > right.score + config.switchMargin) left else right
        } ?: return null
        preferLeftArm = arm === left

        // The torso must be roughly horizontal, or the athlete is standing,
        // kneeling or walking past the camera.
        val dx = abs(arm.shoulder.x - arm.hip.x)
        val dy = abs(arm.shoulder.y - arm.hip.y)
        val tilt = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() // 0 = horizontal
        if (tilt > config.maxTorsoTiltDeg) return null

        return angleDeg(arm.shoulder, arm.elbow, arm.wrist)
    }

    /**
     * FRONT view: vertical distance from shoulders to wrists, in units of
     * shoulder width. Large at the top, small at the bottom.
     */
    private fun frontSignal(pose: PushUpPose): Float? {
        val ls = pose.leftShoulder ?: return null
        val rs = pose.rightShoulder ?: return null
        val lw = pose.leftWrist ?: return null
        val rw = pose.rightWrist ?: return null

        val width = dist(ls, rs)
        if (width < MIN_DISTANCE) return null

        val shoulderY = (ls.y + rs.y) / 2f
        val wristY = (lw.y + rw.y) / 2f
        return (wristY - shoulderY) / width
    }

    // ---------------------------------------------------------------- helpers

    private fun message(): String = when {
        !isTracking -> if (view == ViewMode.SIDE) {
            "Get into push-up position"
        } else {
            "Keep shoulders and wrists in view"
        }
        phase == RepPhase.IDLE -> "Hold the top position"
        phase == RepPhase.UP -> "Go down"
        else -> "Push up"
    }

    private companion object {
        const val MIN_DISTANCE = 1e-4f
    }
}

// -------------------------------------------------------------------- top-level helpers

private fun angleOf(a: PosePoint?, joint: PosePoint?, b: PosePoint?): Float? {
    if (a == null || joint == null || b == null) return null
    return PoseMath.angleAt(a, joint, b).takeIf { !it.isNaN() }
}

private fun dist(a: PosePoint, b: PosePoint): Float = hypot(a.x - b.x, a.y - b.y)

private fun mid(a: PosePoint, b: PosePoint) = PosePoint((a.x + b.x) / 2f, (a.y + b.y) / 2f)

/** Angle at point [b] formed by a-b-c, in degrees (0..180). */
private fun angleDeg(a: PosePoint, b: PosePoint, c: PosePoint): Float {
    val rad = atan2((c.y - b.y).toDouble(), (c.x - b.x).toDouble()) -
        atan2((a.y - b.y).toDouble(), (a.x - b.x).toDouble())
    var deg = abs(Math.toDegrees(rad)).toFloat()
    if (deg > 180f) deg = 360f - deg
    return deg
}
