package com.example.echodairy.data

object TextCleaner {
    fun clean(raw: String): String {
        val normalized = normalize(raw)
        if (normalized.isEmpty()) return ""
        val first = normalized.first().uppercaseChar()
        return first + normalized.drop(1)
    }

    fun normalize(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        return trimmed.replace(Regex("\\s+"), " ")
    }

    fun appendFinal(existing: String, segment: String): String {
        val cleanedSegment = normalize(segment)
        if (cleanedSegment.isEmpty()) return existing
        val base = existing.trimEnd()
        val combined = if (base.isEmpty()) cleanedSegment else "$base $cleanedSegment"
        return ensureSentenceEnd(combined)
    }

    fun ensureSentenceEnd(text: String): String {
        val trimmed = text.trimEnd()
        if (trimmed.isEmpty()) return ""
        val last = trimmed.last()
        return if (last == '.' || last == '!' || last == '?') trimmed else "$trimmed."
    }
}
