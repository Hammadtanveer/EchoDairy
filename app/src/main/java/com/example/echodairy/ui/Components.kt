package com.example.echodairy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.echodairy.data.JournalEntry
import com.example.echodairy.data.Mood
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoodChips(selected: Mood, onSelect: (Mood) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == Mood.HAPPY,
            onClick = { onSelect(Mood.HAPPY) },
            label = { Text(text = "Happy") }
        )
        FilterChip(
            selected = selected == Mood.NEUTRAL,
            onClick = { onSelect(Mood.NEUTRAL) },
            label = { Text(text = "Neutral") }
        )
        FilterChip(
            selected = selected == Mood.SAD,
            onClick = { onSelect(Mood.SAD) },
            label = { Text(text = "Sad") }
        )
    }
}

@Composable
fun EntryCard(entry: JournalEntry, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = formatDate(entry.createdAtEpochMs), style = MaterialTheme.typography.titleSmall)
            Text(text = "Mood: ${entry.mood}", style = MaterialTheme.typography.labelMedium)
            Text(text = entry.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

fun formatDate(epochMs: Long): String {
    val sdf = SimpleDateFormat("EEE, dd MMM yyyy • HH:mm", Locale.getDefault())
    return sdf.format(Date(epochMs))
}
