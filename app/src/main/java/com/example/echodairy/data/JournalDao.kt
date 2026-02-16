package com.example.echodairy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert
    suspend fun insert(entry: JournalEntry): Long

    @Query("SELECT * FROM journal_entries ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<JournalEntry>>
}

