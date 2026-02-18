package com.example.echodairy.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechRecognizerController(
    private val context: Context,
    private val callbacks: Callbacks,
    private val languageTag: String = Locale.getDefault().toLanguageTag()
) {
    data class Callbacks(
        val onPartial: (String) -> Unit,
        val onFinal: (String) -> Unit,
        val onError: (String) -> Unit,
        val onListeningChanged: (Boolean) -> Unit
    )

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private var isListening = false

    init {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                callbacks.onListeningChanged(true)
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onError(error: Int) {
                isListening = false
                callbacks.onListeningChanged(false)
                callbacks.onError(mapError(error))
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = extractText(partialResults)
                if (text.isNotBlank()) callbacks.onPartial(text)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                callbacks.onListeningChanged(false)
                val text = extractText(results)
                if (text.isNotBlank()) callbacks.onFinal(text)
            }
        })
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start() {
        if (isListening) return
        isListening = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
        recognizer.startListening(intent)
    }

    fun stop() {
        if (!isListening) return
        recognizer.stopListening()
    }

    fun cancel() {
        if (!isListening) return
        recognizer.cancel()
        isListening = false
        callbacks.onListeningChanged(false)
    }

    fun destroy() {
        recognizer.destroy()
    }

    private fun extractText(bundle: Bundle?): String {
        val results = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val scores = bundle?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
        if (results.isNullOrEmpty()) return ""
        if (scores == null || scores.size != results.size) return results.firstOrNull().orEmpty()
        val bestIndex = scores.indices.maxByOrNull { scores[it] } ?: 0
        return results.getOrNull(bestIndex).orEmpty()
    }

    private fun mapError(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio error."
            SpeechRecognizer.ERROR_CLIENT -> "Client error."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mic permission denied."
            SpeechRecognizer.ERROR_NETWORK -> "Network error."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy."
            SpeechRecognizer.ERROR_SERVER -> "Server error."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input."
            else -> "Speech error."
        }
    }
}
