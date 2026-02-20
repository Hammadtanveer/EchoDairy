package com.example.echodairy.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun moodToString(value: Mood): String = value.name

    @TypeConverter
    fun stringToMood(value: String): Mood = Mood.valueOf(value)

    @TypeConverter
    fun nullableMoodToString(value: Mood?): String? = value?.name

    @TypeConverter
    fun stringToNullableMood(value: String?): Mood? = value?.let { Mood.valueOf(it) }

    @TypeConverter
    fun listToString(value: List<String>?): String = value?.joinToString("|") ?: ""

    @TypeConverter
    fun stringToList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
