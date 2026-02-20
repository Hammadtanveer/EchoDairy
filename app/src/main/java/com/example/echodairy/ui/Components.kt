package com.example.echodairy.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.echodairy.data.JournalEntry
import com.example.echodairy.data.Mood
import com.example.echodairy.vm.SpeechLanguage
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

@Composable
fun LanguagePicker(
    languages: List<SpeechLanguage>,
    selectedTag: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = languages.firstOrNull { it.tag == selectedTag }?.label ?: "Language"

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "Recognition language", style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(text = selectedLabel)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(text = lang.label) },
                    onClick = {
                        expanded = false
                        onSelect(lang.tag)
                    }
                )
            }
        }
    }
}

@Composable
fun MicWaveform(rmsDb: Float, isListening: Boolean, modifier: Modifier = Modifier) {
    val target = if (isListening) ((rmsDb + 2f) / 12f).coerceIn(0f, 1f) else 0f
    val level by animateFloatAsState(targetValue = target, label = "rms")
    val bars = listOf(0.5f, 0.8f, 1f, 0.8f, 0.5f)
    val baseHeight = 8.dp
    val maxBoost = 26.dp
    val color = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { weight ->
            val height = baseHeight + maxBoost * level * weight
            Spacer(
                modifier = Modifier
                    .width(6.dp)
                    .height(height)
                    .background(color = color, shape = RoundedCornerShape(50))
            )
        }
    }
}

fun formatDate(epochMs: Long): String {
    val sdf = SimpleDateFormat("EEE, dd MMM yyyy • HH:mm", Locale.getDefault())
    return sdf.format(Date(epochMs))
}
