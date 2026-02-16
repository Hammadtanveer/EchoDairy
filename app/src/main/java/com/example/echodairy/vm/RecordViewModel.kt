package com.example.echodairy.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echodairy.data.JournalRepository
import com.example.echodairy.data.Mood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordUiState(
    val text: String = "",
    val mood: Mood = Mood.NEUTRAL,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

class RecordViewModel(
    private val repository: JournalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecordUiState())
    val state: StateFlow<RecordUiState> = _state

    fun onTextChanged(value: String) {
        _state.update { it.copy(text = value, error = null, saved = false) }
    }

    fun onMoodChanged(value: Mood) {
        _state.update { it.copy(mood = value, saved = false) }
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
}

class RecordViewModelFactory(
    private val repository: JournalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordViewModel(repository) as T
    }
}

