package com.example.echodairy.vm

import android.app.Application
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echodairy.data.JournalRepository
import com.example.echodairy.data.Mood
import com.example.echodairy.data.TextCleaner
import com.example.echodairy.speech.SpeechRecognizerController
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordUiState(
    val text: String = "",
    val mood: Mood = Mood.NEUTRAL,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val isListening: Boolean = false,
    val partialTranscript: String = "",
    val speechError: String? = null,
    val canListen: Boolean = true,
    val languages: List<SpeechLanguage> = emptyList(),
    val languageTag: String = "",
    val rmsDb: Float = 0f
)

data class SpeechLanguage(val label: String, val tag: String)

private fun buildLanguages(defaultTag: String): List<SpeechLanguage> = listOf(
    SpeechLanguage("Device default", defaultTag),
    SpeechLanguage("English (US)", "en-US"),
    SpeechLanguage("English (India)", "en-IN"),
    SpeechLanguage("Hindi (India)", "hi-IN"),
    SpeechLanguage("Tamil (India)", "ta-IN"),
    SpeechLanguage("Telugu (India)", "te-IN")
)

class RecordViewModel(
    application: Application,
    private val repository: JournalRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(RecordUiState())
    val state: StateFlow<RecordUiState> = _state

    private var controller: SpeechRecognizerController? = null
    private val languageOptions = buildLanguages(Locale.getDefault().toLanguageTag())

    init {
        val available = SpeechRecognizer.isRecognitionAvailable(getApplication())
        _state.update {
            it.copy(
                canListen = available,
                languages = languageOptions,
                languageTag = languageOptions.firstOrNull()?.tag.orEmpty()
            )
        }
    }

    fun onTextChanged(value: String) {
        _state.update { it.copy(text = value, error = null, saved = false, speechError = null) }
    }

    fun onMoodChanged(value: Mood) {
        _state.update { it.copy(mood = value, saved = false) }
    }

    fun onLanguageChanged(tag: String) {
        _state.update { it.copy(languageTag = tag) }
    }

    fun startListening() {
        if (!_state.value.canListen) {
            _state.update { it.copy(speechError = "Speech recognition unavailable.") }
            return
        }
        ensureController()
        _state.update { it.copy(speechError = null, partialTranscript = "") }
        controller?.start(_state.value.languageTag)
    }

    fun stopListening() {
        controller?.stop()
    }

    fun cancelListening() {
        controller?.cancel()
    }

    fun onMicPermissionDenied() {
        _state.update { it.copy(speechError = "Mic permission is required.") }
    }

    fun save() {
        val current = _state.value
        if (current.text.trim().isEmpty()) {
            _state.update { it.copy(error = "Entry is empty.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            runCatching {
                repository.addEntry(text = current.text, mood = current.mood)
            }.onSuccess {
                _state.update { it.copy(text = "", isSaving = false, saved = true) }
            }.onFailure { t ->
                _state.update { it.copy(isSaving = false, error = t.message ?: "Save failed.") }
            }
        }
    }

    fun consumeSaved() {
        _state.update { it.copy(saved = false) }
    }

    override fun onCleared() {
        controller?.destroy()
        controller = null
        super.onCleared()
    }

    private fun ensureController() {
        if (controller != null) return
        controller = SpeechRecognizerController(
            context = getApplication(),
            callbacks = SpeechRecognizerController.Callbacks(
                onPartial = { text ->
                    _state.update { it.copy(partialTranscript = TextCleaner.normalize(text)) }
                },
                onFinal = { text ->
                    _state.update { current ->
                        val combined = TextCleaner.mergeFinal(current.text, current.partialTranscript, text)
                        current.copy(text = combined, partialTranscript = "")
                    }
                },
                onError = { message ->
                    _state.update { it.copy(speechError = message, isListening = false, rmsDb = 0f) }
                },
                onListeningChanged = { listening ->
                    _state.update { it.copy(isListening = listening, rmsDb = if (listening) it.rmsDb else 0f) }
                },
                onRmsChanged = { rms ->
                    _state.update { it.copy(rmsDb = rms) }
                }
            )
        )
    }
}

class RecordViewModelFactory(
    private val application: Application,
    private val repository: JournalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordViewModel(application, repository) as T
    }
}
