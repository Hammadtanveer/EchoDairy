package com.example.echodairy.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechRecognizerController(
    private val context: Context,
    private val callbacks: Callbacks,
    private val defaultLanguageTag: String = Locale.getDefault().toLanguageTag()
) {
    data class Callbacks(
        val onPartial: (String) -> Unit,
        val onFinal: (String) -> Unit,
        val onError: (String) -> Unit,
        val onListeningChanged: (Boolean) -> Unit,
        val onRmsChanged: (Float) -> Unit
    )

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private var isListening = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var manualStop = false
    private var retryCount = 0
    private val maxRetries = 1
    private var lastLanguageTag: String = defaultLanguageTag
    private var lastPartial: String = ""
    private var pendingFinalize: Runnable? = null
    private var keepListening = false

    init {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                callbacks.onListeningChanged(true)
            }

            override fun onBeginningOfSpeech() {
                pendingFinalize?.let { mainHandler.removeCallbacks(it) }
                pendingFinalize = null
            }
            override fun onRmsChanged(rmsdB: Float) {
                callbacks.onRmsChanged(rmsdB)
            }
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {
                if (lastPartial.isNotBlank()) {
                    val runnable = Runnable {
                        if (lastPartial.isNotBlank()) {
                            val fallback = lastPartial
                            lastPartial = ""
                            callbacks.onFinal(fallback)
                            restartIfNeeded()
                        }
                    }
                    pendingFinalize = runnable
                    mainHandler.postDelayed(runnable, 500L)
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onError(error: Int) {
                isListening = false
                callbacks.onListeningChanged(false)
                callbacks.onRmsChanged(0f)
                pendingFinalize?.let { mainHandler.removeCallbacks(it) }
                pendingFinalize = null
                if (error == SpeechRecognizer.ERROR_NO_MATCH && lastPartial.isNotBlank()) {
                    val fallback = lastPartial
                    lastPartial = ""
                    callbacks.onFinal(fallback)
                    restartIfNeeded()
                    return
                }
                if (!manualStop && retryCount < maxRetries) {
                    if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        retryCount += 1
                        mainHandler.postDelayed({
                            if (!manualStop) {
                                recognizer.cancel()
                                isListening = true
                                recognizer.startListening(buildIntent(lastLanguageTag))
                            }
                        }, 450L)
                        return
                    }
                }
                callbacks.onError(mapError(error))
                restartIfNeeded()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = extractText(partialResults)
                if (text.isNotBlank()) {
                    lastPartial = text
                    callbacks.onPartial(text)
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                callbacks.onListeningChanged(false)
                callbacks.onRmsChanged(0f)
                retryCount = 0
                pendingFinalize?.let { mainHandler.removeCallbacks(it) }
                pendingFinalize = null
                val text = extractText(results)
                if (text.isNotBlank()) {
                    lastPartial = ""
                    callbacks.onFinal(text)
                } else if (lastPartial.isNotBlank()) {
                    val fallback = lastPartial
                    lastPartial = ""
                    callbacks.onFinal(fallback)
                }
                restartIfNeeded()
            }
        })
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start(languageTag: String? = null) {
        if (isListening) return
        isListening = true
        manualStop = false
        keepListening = true
        retryCount = 0
        lastPartial = ""
        lastLanguageTag = languageTag?.takeIf { it.isNotBlank() } ?: defaultLanguageTag
        recognizer.startListening(buildIntent(lastLanguageTag))
    }

    fun stop() {
        if (!isListening) return
        manualStop = true
        keepListening = false
        recognizer.stopListening()
    }

    fun cancel() {
        if (!isListening) return
        manualStop = true
        keepListening = false
        recognizer.cancel()
        isListening = false
        callbacks.onListeningChanged(false)
        callbacks.onRmsChanged(0f)
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

    private fun buildIntent(languageTag: String): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1200)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
        }
    }

    private fun restartIfNeeded() {
        if (!keepListening || manualStop) return
        mainHandler.postDelayed({
            if (!manualStop) {
                isListening = true
                recognizer.startListening(buildIntent(lastLanguageTag))
            }
        }, 300L)
    }
}
