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

    fun mergeFinal(existing: String, partial: String, final: String): String {
        val cleanedFinal = normalize(final)
        if (cleanedFinal.isEmpty()) return existing
        val cleanedPartial = normalize(partial)
        val base = existing.trimEnd()
        val resolved = when {
            cleanedPartial.isNotEmpty() && cleanedFinal.startsWith(cleanedPartial, ignoreCase = true) -> cleanedFinal
            cleanedPartial.isNotEmpty() && cleanedPartial.startsWith(cleanedFinal, ignoreCase = true) -> cleanedPartial
            else -> cleanedFinal
        }
        if (base.isNotEmpty() && base.endsWith(resolved, ignoreCase = true)) {
            return ensureSentenceEnd(base)
        }
        val combined = if (base.isEmpty()) resolved else "$base $resolved"
        return ensureSentenceEnd(combined)
    }
}
