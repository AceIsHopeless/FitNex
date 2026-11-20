package com.fitnex.app.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.fitnex.app.domain.PoseMath
import com.fitnex.app.domain.PosePoint
import com.fitnex.app.domain.PushUpPose
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.io.Closeable

/**
 * Wraps the MediaPipe pose landmarker.
 *
 * Results arrive on a MediaPipe worker thread; callers must marshal to the
 * thread they need. [onPose] receives null when no usable body was found, which
 * lets the rep counter discard a half-finished repetition.
 */
class PoseDetector(
    context: Context,
    private val onPose: (PushUpPose?, Long) -> Unit,
) : Closeable {

    private var landmarker: PoseLandmarker? = null

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_ASSET)
            .setDelegate(Delegate.CPU)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, _ ->
                onPose(toPushUpPose(result), result.timestampMs())
            }
            .setErrorListener { error ->
                Log.e(TAG, "Pose landmarker error: ${error.message}", error)
            }
            .build()

        landmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun detect(bitmap: Bitmap, timestampMs: Long) {
        val detector = landmarker ?: return
        runCatching { detector.detectAsync(BitmapImageBuilder(bitmap).build(), timestampMs) }
            .onFailure { Log.w(TAG, "Frame skipped: ${it.message}") }
    }

    override fun close() {
        runCatching { landmarker?.close() }
        landmarker = null
    }

    private fun toPushUpPose(result: PoseLandmarkerResult): PushUpPose? {
        val landmarks = result.landmarks().firstOrNull() ?: return null

        fun point(index: Int): PosePoint? {
            val landmark = landmarks.getOrNull(index) ?: return null
            val visibility = landmark.visibility().orElse(0f)
            if (visibility < MIN_VISIBILITY) return null
            return PosePoint(landmark.x(), landmark.y(), visibility)
        }

        val leftShoulder = point(LEFT_SHOULDER)
        val leftElbow = point(LEFT_ELBOW)
        val leftWrist = point(LEFT_WRIST)
        val rightShoulder = point(RIGHT_SHOULDER)
        val rightElbow = point(RIGHT_ELBOW)
        val rightWrist = point(RIGHT_WRIST)

        val leftAngle = if (leftShoulder != null && leftElbow != null && leftWrist != null) {
            PoseMath.angleAt(leftShoulder, leftElbow, leftWrist).takeIf { !it.isNaN() }
        } else {
            null
        }
        val rightAngle = if (rightShoulder != null && rightElbow != null && rightWrist != null) {
            PoseMath.angleAt(rightShoulder, rightElbow, rightWrist).takeIf { !it.isNaN() }
        } else {
            null
        }

        val shoulders = listOfNotNull(leftShoulder?.y, rightShoulder?.y)
        val shoulderY = if (shoulders.isEmpty()) null else shoulders.average().toFloat()

        if (leftAngle == null && rightAngle == null) return null
        return PushUpPose(leftAngle, rightAngle, shoulderY)
    }

    private companion object {
        const val TAG = "FitNexPose"
        const val MODEL_ASSET = "pose_landmarker_lite.task"
        const val MIN_VISIBILITY = 0.4f

        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16
    }
}
