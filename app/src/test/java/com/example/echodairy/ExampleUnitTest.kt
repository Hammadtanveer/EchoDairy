package com.example.echodairy

import com.example.echodairy.data.TextCleaner
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun textCleaner_normalizes_and_capitalizes() {
        val input = "   hello   world  "
        val result = TextCleaner.clean(input)
        assertEquals("Hello world", result)
    }
}