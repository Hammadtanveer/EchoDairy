package com.example.echodairy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.echodairy.vm.RecordViewModel

@Composable
fun RecordScreen(paddingValues: PaddingValues, vm: RecordViewModel) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) vm.consumeSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Speak your day. Let it echo as a journal.",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = state.text,
            onValueChange = vm::onTextChanged,
            modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
            label = { Text(text = "Today's entry") },
            placeholder = { Text(text = "Type for now. Speech-to-text comes next.") }
        )

        MoodChips(selected = state.mood, onSelect = vm::onMoodChanged)

        if (state.error != null) {
            Text(text = state.error ?: "", color = MaterialTheme.colorScheme.error)
        } else if (state.saved) {
            Text(text = "Saved.", color = MaterialTheme.colorScheme.primary)
        }

        Button(
            onClick = vm::save,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (state.isSaving) "Saving..." else "Save entry")
        }
    }
}

