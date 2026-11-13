package com.HIVFactsheet.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Read-aloud using Android's built-in Text-to-Speech (no extra dependency).
 * [speakingId] holds the screen id currently being read, exposed as Compose
 * state so the Listen button can reflect play/stop.
 */
object Speaker {
    private var tts: TextToSpeech? = null
    private var ready = false
    private val main = Handler(Looper.getMainLooper())

    var speakingId by mutableStateOf<String?>(null)
        private set

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ready = true
                tts?.language = Locale.getDefault().takeIf {
                    tts?.isLanguageAvailable(it) ?: 0 >= TextToSpeech.LANG_AVAILABLE
                } ?: Locale.ENGLISH
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onError(utteranceId: String?) { main.post { speakingId = null } }
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == LAST) main.post { speakingId = null }
                    }
                })
            }
        }
    }

    /** Start reading [text] for [id], or stop if that id is already playing. */
    fun toggle(id: String, text: String) {
        if (speakingId == id) { stop(); return }
        if (!ready || text.isBlank()) return
        val chunks = chunk(text)
        tts?.stop()
        chunks.forEachIndexed { i, part ->
            val mode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val uttId = if (i == chunks.lastIndex) LAST else "p$i"
            tts?.speak(part, mode, null, uttId)
        }
        speakingId = id
    }

    fun stop() {
        tts?.stop()
        speakingId = null
    }

    /** Stop only if the given screen is the one currently playing. */
    fun stopIfSpeaking(id: String) {
        if (speakingId == id) stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
        speakingId = null
    }

    // TTS has a ~4000-char limit per utterance; split on sentence boundaries.
    private fun chunk(text: String, max: Int = 3500): List<String> {
        if (text.length <= max) return listOf(text)
        val out = ArrayList<String>()
        val sb = StringBuilder()
        for (sentence in text.split(Regex("(?<=[.!?])\\s+"))) {
            if (sb.length + sentence.length > max && sb.isNotEmpty()) {
                out.add(sb.toString()); sb.clear()
            }
            sb.append(sentence).append(' ')
        }
        if (sb.isNotEmpty()) out.add(sb.toString())
        return out
    }

    private const val LAST = "last"
}
