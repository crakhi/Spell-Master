package com.spell.master.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Reads a word out loud letter-by-letter and then as a whole once an answer is
 * submitted -- e.g. "A... D... U... L... T... Adult" -- so kids connect the
 * spelling to the sound. Android's TTS engine doesn't expose bass/treble, only
 * pitch and speech rate; we lean on speech rate (slower for letters than for the
 * word) since that's what actually controls how clearly each sound is heard.
 */
object SpellingSpeaker {
    private var tts: TextToSpeech? = null

    @Volatile private var ready = false
    private val callCounter = AtomicInteger(0)

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ready = true
            }
        }
    }

    /**
     * Spells [word] out letter by letter (slow, deliberate pace) then speaks the
     * whole word at a slightly gentler-than-normal pace. No-ops while muted, but
     * still calls [onDone] so callers gating UI on completion don't get stuck.
     * [onDone] fires on a TTS callback thread, not necessarily the main thread.
     */
    fun speakWordAndSpelling(word: String, onDone: () -> Unit = {}) {
        val engine = tts
        val clean = word.trim()
        if (engine == null || !ready || clean.isEmpty() || SoundEffects.isMuted()) {
            onDone()
            return
        }

        engine.stop()

        val wordUtteranceId = "word_${clean}_${callCounter.incrementAndGet()}"
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                if (utteranceId == wordUtteranceId) onDone()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (utteranceId == wordUtteranceId) onDone()
            }
        })

        engine.setPitch(DEFAULT_PITCH)
        engine.setSpeechRate(LETTER_SPEECH_RATE)
        var first = true
        clean.uppercase(Locale.US).forEachIndexed { index, letter ->
            if (letter.isLetter()) {
                val queueMode = if (first) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                first = false
                engine.speak(letter.toString(), queueMode, null, "letter_$index")
                engine.playSilentUtterance(LETTER_PAUSE_MS, TextToSpeech.QUEUE_ADD, null)
            }
        }

        engine.playSilentUtterance(WORD_PAUSE_MS, TextToSpeech.QUEUE_ADD, null)
        engine.setSpeechRate(WORD_SPEECH_RATE)
        engine.speak(clean, TextToSpeech.QUEUE_ADD, null, wordUtteranceId)
    }

    private const val DEFAULT_PITCH = 1.0f
    private const val LETTER_SPEECH_RATE = 0.95f
    private const val WORD_SPEECH_RATE = 0.85f
    private const val LETTER_PAUSE_MS = 160L
    private const val WORD_PAUSE_MS = 350L
}
