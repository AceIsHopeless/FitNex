package com.fitnex.app.camera

import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/**
 * Throttles camera frames before they reach ML Kit so live feedback stays
 * responsive and the CPU is not saturated. Frames are dropped without
 * allocation; [PoseDetector] owns closing the frames it accepts.
 */
class PushUpAnalyzer(
    private val detector: PoseDetector,
    private val frameIntervalMs: Long = 66L,
) : ImageAnalysis.Analyzer {

    private var lastSubmittedMs = 0L

    override fun analyze(image: ImageProxy) {
        val now = SystemClock.uptimeMillis()
        if (now - lastSubmittedMs < frameIntervalMs) {
            image.close()
            return
        }
        lastSubmittedMs = now
        detector.detect(image)
    }
}
