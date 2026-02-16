package com.example.echodairy.data

object TextCleaner {
    fun clean(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        val normalized = trimmed.replace(Regex("\\s+"), " ")
        val first = normalized.first().uppercaseChar()
        return first + normalized.drop(1)
    }
}

