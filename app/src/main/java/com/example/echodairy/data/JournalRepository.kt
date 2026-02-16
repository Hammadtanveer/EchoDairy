package com.example.echodairy.data

import kotlinx.coroutines.flow.Flow

class JournalRepository(
    private val dao: JournalDao
) {
    fun observeEntries(): Flow<List<JournalEntry>> = dao.observeAll()

    suspend fun addEntry(text: String, mood: Mood, nowEpochMs: Long = System.currentTimeMillis()): Long {
        val cleaned = TextCleaner.clean(text)
        return dao.insert(
            JournalEntry(
                createdAtEpochMs = nowEpochMs,
                mood = mood,
                text = cleaned
            )
        )
    }
}

