package com.example.echodairy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val createdAtEpochMs: Long,
    val mood: Mood,
    val text: String
)

