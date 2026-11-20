package com.fitnex.app.ui.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import com.fitnex.app.R
import com.fitnex.app.data.GameSettings
import java.io.Closeable

/**
 * Battle feedback: a short tick per counted rep plus pickup haptics.
 * Both channels are independent and switchable from Profile, and every call
 * degrades quietly when the device has no vibrator or sounds fail to load.
 */
class GameFeedback(context: Context) : Closeable {

    private val appContext = context.applicationContext
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val repSound = soundPool.load(appContext, R.raw.rep_tick, 1)
    private val hitSound = soundPool.load(appContext, R.raw.hit, 1)
    private val victorySound = soundPool.load(appContext, R.raw.victory, 1)
    private val defeatSound = soundPool.load(appContext, R.raw.defeat, 1)

    private val vibrator: Vibrator? = appContext.getSystemService(Vibrator::class.java)

    /** A rep landed. */
    fun rep(settings: GameSettings) {
        if (settings.soundEnabled) play(repSound, 0.45f)
        if (settings.hapticsEnabled) buzz(14L, 70)
    }

    /** The rep that finished the boss. */
    fun finishingHit(settings: GameSettings) {
        if (settings.soundEnabled) play(hitSound, 0.75f)
        if (settings.hapticsEnabled) buzz(45L, 160)
    }

    fun victory(settings: GameSettings) {
        if (settings.soundEnabled) play(victorySound, 0.8f)
        if (settings.hapticsEnabled) {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return
            runCatching {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 90), -1))
            }
        }
    }

    fun defeat(settings: GameSettings) {
        if (settings.soundEnabled) play(defeatSound, 0.7f)
    }

    private fun play(soundId: Int, volume: Float) {
        if (soundId == 0) return
        runCatching { soundPool.play(soundId, volume, volume, 1, 0, 1f) }
    }

    private fun buzz(millis: Long, amplitude: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        runCatching { v.vibrate(VibrationEffect.createOneShot(millis, amplitude)) }
    }

    override fun close() {
        runCatching { soundPool.release() }
    }
}
