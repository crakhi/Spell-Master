package com.spell.master.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

/**
 * Tiny synthesizer for the game's sound cues (whistle on timeout, chime on correct,
 * buzz on wrong, tick while the timer runs) so the app doesn't need to ship or
 * download any audio assets. [isMuted] is a single global switch (persisted in
 * SharedPreferences) covering every sound in the app.
 */
object SoundEffects {
    private const val SAMPLE_RATE = 44100
    private const val PREFS_NAME = "spell_master_prefs"
    private const val KEY_MUTED = "sound_muted"

    @Volatile private var muted: Boolean = false

    /** Call once at app startup so the persisted mute preference is loaded before anything plays. */
    fun init(context: Context) {
        muted = prefs(context).getBoolean(KEY_MUTED, false)
    }

    fun isMuted(): Boolean = muted

    fun setMuted(context: Context, value: Boolean) {
        muted = value
        prefs(context).edit().putBoolean(KEY_MUTED, value).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun playWhistle() = withContext(Dispatchers.Default) {
        if (muted) return@withContext
        playBuffer(buildSweep(startHz = 1000.0, endHz = 2400.0, durationMs = 500) +
            buildSweep(startHz = 2400.0, endHz = 1400.0, durationMs = 400))
    }

    // Correct answers get a soft, quiet confirmation -- the visuals (confetti, stars)
    // already carry the celebration, so the sound shouldn't be the loud one. Kept in the
    // same frequency neighborhood as the wrong-buzz tone below so phone-speaker frequency
    // response can't cancel out the amplitude difference -- only volume should differ.
    suspend fun playCorrectChime() = withContext(Dispatchers.Default) {
        if (muted) return@withContext
        playBuffer(buildTone(900.0, 110, amplitude = 0.12))
    }

    // Wrong answers get the loud, unmistakable alert tone, so the kid actually notices
    // and looks at the correction instead of missing it.
    suspend fun playWrongBuzz() = withContext(Dispatchers.Default) {
        if (muted) return@withContext
        playBuffer(buildTone(900.0, 130, amplitude = 0.9) + buildTone(700.0, 160, amplitude = 0.9))
    }

    /** Short clock-like tick, once per second while a question's timer is running.
     * Sharper and a touch louder in the last 10 seconds to reinforce the visual urgency. */
    suspend fun playTick(urgent: Boolean = false) = withContext(Dispatchers.Default) {
        if (muted) return@withContext
        val freq = if (urgent) 1500.0 else 1100.0
        val amplitude = if (urgent) 0.4 else 0.28
        playBuffer(buildTone(freq, 35, amplitude = amplitude))
    }

    private fun buildTone(freqHz: Double, durationMs: Int, amplitude: Double = 0.5): ShortArray =
        buildSweep(freqHz, freqHz, durationMs, amplitude)

    private fun buildSweep(startHz: Double, endHz: Double, durationMs: Int, amplitude: Double = 0.55): ShortArray {
        val sampleCount = (SAMPLE_RATE * durationMs / 1000.0).toInt().coerceAtLeast(1)
        val samples = ShortArray(sampleCount)
        val fadeSamples = (SAMPLE_RATE * 0.015).toInt().coerceAtLeast(1)
        var phase = 0.0
        for (i in 0 until sampleCount) {
            val freq = startHz + (endHz - startHz) * (i.toDouble() / sampleCount)
            phase += 2 * PI * freq / SAMPLE_RATE
            val envelope = when {
                i < fadeSamples -> i.toDouble() / fadeSamples
                i > sampleCount - fadeSamples -> (sampleCount - i).toDouble() / fadeSamples
                else -> 1.0
            }
            samples[i] = (sin(phase) * amplitude * envelope * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun playBuffer(samples: ShortArray) {
        if (samples.isEmpty()) return
        val bufferSizeBytes = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeBytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.setNotificationMarkerPosition(samples.size - 1)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(t: AudioTrack) {
                t.release()
            }

            override fun onPeriodicNotification(t: AudioTrack) = Unit
        })
        track.play()
    }
}
