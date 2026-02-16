package com.example.echodairy.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun moodToString(value: Mood): String = value.name

    @TypeConverter
    fun stringToMood(value: String): Mood = Mood.valueOf(value)
}

