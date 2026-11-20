package com.fitnex.app.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/**
 * Feeds upright camera frames to the pose detector at a bounded rate so live
 * feedback never lags and the CPU is not saturated.
 *
 * The analysis pipeline is configured for RGBA_8888 output; each frame is
 * copied into a bitmap and rotated by the sensor orientation before it reaches
 * MediaPipe, which keeps the pose model upright in portrait mode.
 */
class PushUpAnalyzer(
    private val detector: PoseDetector,
    private val frameIntervalMs: Long = 66L,
) : ImageAnalysis.Analyzer {

    private var lastSubmittedMs = 0L

    override fun analyze(image: ImageProxy) {
        try {
            val now = SystemClock.uptimeMillis()
            if (now - lastSubmittedMs < frameIntervalMs) return
            lastSubmittedMs = now
            detector.detect(image.toUprightBitmap(), now)
        } catch (throwable: Throwable) {
            Log.w(TAG, "Dropping frame: ${throwable.message}")
        } finally {
            image.close()
        }
    }

    private fun ImageProxy.toUprightBitmap(): Bitmap {
        val rgba = toRgbaBitmap()
        val rotation = imageInfo.rotationDegrees
        if (rotation == 0) return rgba
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotated = Bitmap.createBitmap(rgba, 0, 0, rgba.width, rgba.height, matrix, false)
        if (rotated != rgba) rgba.recycle()
        return rotated
    }

    private fun ImageProxy.toRgbaBitmap(): Bitmap {
        val plane = planes[0]
        val buffer = plane.buffer
        val width = width
        val height = height
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride
        val pixels = IntArray(width * height)
        val row = ByteArray(rowStride)
        buffer.rewind()
        for (y in 0 until height) {
            val toRead = minOf(rowStride, buffer.remaining())
            if (toRead <= 0) break
            buffer.get(row, 0, toRead)
            var offset = 0
            for (x in 0 until width) {
                if (offset + 3 >= toRead) break
                val r = row[offset].toInt() and 0xFF
                val g = row[offset + 1].toInt() and 0xFF
                val b = row[offset + 2].toInt() and 0xFF
                val a = row[offset + 3].toInt() and 0xFF
                pixels[y * width + x] = (a shl 24) or (r shl 16) or (g shl 8) or b
                offset += pixelStride
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private companion object {
        const val TAG = "FitNexAnalyzer"
    }
}
