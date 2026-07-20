package com.spell.master.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

/**
 * Tiny synthesizer for the game's sound cues (whistle on timeout, chime on correct,
 * buzz on wrong) so the app doesn't need to ship or download any audio assets.
 */
object SoundEffects {
    private const val SAMPLE_RATE = 44100

    suspend fun playWhistle() = withContext(Dispatchers.Default) {
        playBuffer(buildSweep(startHz = 1000.0, endHz = 2400.0, durationMs = 500) +
            buildSweep(startHz = 2400.0, endHz = 1400.0, durationMs = 400))
    }

    // Correct answers get a soft, quiet confirmation -- the visuals (confetti, stars)
    // already carry the celebration, so the sound shouldn't be the loud one.
    suspend fun playCorrectChime() = withContext(Dispatchers.Default) {
        playBuffer(buildTone(660.0, 160, amplitude = 0.28) + buildTone(880.0, 200, amplitude = 0.22))
    }

    // Wrong answers get the more noticeable alert tone, so the kid actually notices
    // and looks at the correction instead of missing it.
    suspend fun playWrongBuzz() = withContext(Dispatchers.Default) {
        playBuffer(buildTone(520.0, 140, amplitude = 0.6) + buildTone(520.0, 140, amplitude = 0.6))
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
