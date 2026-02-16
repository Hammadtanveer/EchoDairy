package com.example.echodairy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.echodairy.data.JournalEntry
import com.example.echodairy.vm.HistoryViewModel

@Composable
fun HistoryScreen(paddingValues: PaddingValues, vm: HistoryViewModel) {
    val entries by vm.entries.collectAsState()
    var selected by remember { mutableStateOf<JournalEntry?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(
                    text = "No entries yet.\nCreate your first one from Record.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    EntryCard(entry = entry, onClick = { selected = entry })
                }
            }
        }
    }

    if (selected != null) {
        val entry = selected!!
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = {
                TextButton(onClick = { selected = null }) { Text(text = "Close") }
            },
            title = { Text(text = formatDate(entry.createdAtEpochMs)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Mood: ${entry.mood}")
                    Text(text = entry.text)
                }
            }
        )
    }
}

