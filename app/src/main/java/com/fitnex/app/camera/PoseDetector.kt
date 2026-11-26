package com.fitnex.app.camera

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.fitnex.app.domain.PosePoint
import com.fitnex.app.domain.PushUpPose
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.io.Closeable

/**
 * Wraps ML Kit's accurate pose detector.
 *
 * The accurate model handles athletes lying close to the floor much better
 * than the lite model, which is where rep tracking used to fall apart.
 *
 * ML Kit delivers results on the main thread. [onPose] receives null when no
 * usable body was found, which lets the rep counter discard a half-finished
 * repetition. The analyzer's [ImageProxy] is closed as soon as ML Kit finishes
 * with it, or CameraX stops delivering frames.
 */
class PoseDetector(
    context: Context,
    private val onPose: (PushUpPose?, Long) -> Unit,
) : Closeable {

    private val detector = PoseDetection.getClient(
        AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build(),
    )

    @Volatile
    private var closed = false

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    fun detect(image: ImageProxy) {
        if (closed) {
            image.close()
            return
        }

        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        val timestampMs = SystemClock.elapsedRealtime()
        try {
            val input = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            detector.process(input)
                .addOnSuccessListener { pose -> onPose(pose.toPushUpPose(), timestampMs) }
                .addOnFailureListener { error ->
                    Log.w(TAG, "Pose frame skipped: ${error.message}")
                    onPose(null, timestampMs)
                }
                .addOnCompleteListener {
                    image.close() // always close, or CameraX stops delivering frames
                }
        } catch (throwable: Throwable) {
            Log.w(TAG, "Dropping frame: ${throwable.message}")
            image.close()
        }
    }

    override fun close() {
        closed = true
        runCatching { detector.close() }
    }

    private fun Pose.toPushUpPose(): PushUpPose = PushUpPose(
        leftShoulder = landmark(PoseLandmark.LEFT_SHOULDER),
        leftElbow = landmark(PoseLandmark.LEFT_ELBOW),
        leftWrist = landmark(PoseLandmark.LEFT_WRIST),
        leftHip = landmark(PoseLandmark.LEFT_HIP),
        rightShoulder = landmark(PoseLandmark.RIGHT_SHOULDER),
        rightElbow = landmark(PoseLandmark.RIGHT_ELBOW),
        rightWrist = landmark(PoseLandmark.RIGHT_WRIST),
        rightHip = landmark(PoseLandmark.RIGHT_HIP),
    )

    private fun Pose.landmark(type: Int): PosePoint? {
        val landmark = getPoseLandmark(type) ?: return null
        if (landmark.inFrameLikelihood < MIN_LIKELIHOOD) return null
        val position = landmark.position
        return PosePoint(position.x, position.y, landmark.inFrameLikelihood)
    }

    private companion object {
        const val TAG = "FitNexPose"
        const val MIN_LIKELIHOOD = 0.5f
    }
}
