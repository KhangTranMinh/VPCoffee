package com.vpcoffee.feature.debug.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DebugLogViewModelTest {

    // Note: DebugLogViewModel is hard to unit test because it directly calls
    // Runtime.getRuntime().exec("logcat") in init{}. We test the pure functions
    // via reflection or by extracting them. For now, we test the data classes
    // and the logic we can access.

    @Test
    fun `LogEntry data class holds values correctly`() {
        val entry = LogEntry(
            timestamp = "08-18 14:30:15.123",
            pid = "1234",
            level = "D",
            tag = "MainActivity",
            message = "onCreate called",
        )
        assertEquals("08-18 14:30:15.123", entry.timestamp)
        assertEquals("1234", entry.pid)
        assertEquals("D", entry.level)
        assertEquals("MainActivity", entry.tag)
        assertEquals("onCreate called", entry.message)
    }

    @Test
    fun `LogLevel enum has correct labels`() {
        assertEquals("Verbose", LogLevel.V.label)
        assertEquals("Debug", LogLevel.D.label)
        assertEquals("Info", LogLevel.I.label)
        assertEquals("Warn", LogLevel.W.label)
        assertEquals("Error", LogLevel.E.label)
        assertEquals("All", LogLevel.A.label)
    }

    @Test
    fun `LogLevel enum has all expected values`() {
        assertEquals(6, LogLevel.entries.size)
    }

    // Test the regex pattern used in parseLine
    private val regex = Regex("""^(\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+\d+\s+([VDIWEA])\s+(.+?):\s*(.*)$""")

    @Test
    fun `regex parses valid logcat line`() {
        val line = "08-18 14:30:15.123  1234  5678 D MainActivity: onCreate called"
        val match = regex.matchEntire(line)
        assertNotNull(match)
        assertEquals("08-18 14:30:15.123", match!!.groupValues[1])
        assertEquals("1234", match.groupValues[2])
        assertEquals("D", match.groupValues[3])
        assertEquals("MainActivity", match.groupValues[4])
        assertEquals("onCreate called", match.groupValues[5])
    }

    @Test
    fun `regex parses error level`() {
        val line = "08-18 14:30:15.123  1234  5678 E NetworkError: Connection failed"
        val match = regex.matchEntire(line)
        assertNotNull(match)
        assertEquals("E", match!!.groupValues[3])
        assertEquals("NetworkError", match.groupValues[4])
        assertEquals("Connection failed", match.groupValues[5])
    }

    @Test
    fun `regex parses verbose level`() {
        val line = "08-18 14:30:15.123  1234  5678 V DebugTag: verbose message"
        val match = regex.matchEntire(line)
        assertNotNull(match)
        assertEquals("V", match!!.groupValues[3])
    }

    @Test
    fun `regex rejects invalid line`() {
        val line = "invalid logcat line"
        val match = regex.matchEntire(line)
        assertNull(match)
    }

    @Test
    fun `regex rejects line with wrong date format`() {
        val line = "2024-08-18 14:30:15.123  1234  5678 D Tag: message"
        val match = regex.matchEntire(line)
        assertNull(match)
    }

    @Test
    fun `regex handles message with colon`() {
        val line = "08-18 14:30:15.123  1234  5678 D Tag: key: value: more"
        val match = regex.matchEntire(line)
        assertNotNull(match)
        assertEquals("key: value: more", match!!.groupValues[5])
    }

    @Test
    fun `regex handles empty message`() {
        val line = "08-18 14:30:15.123  1234  5678 D Tag: "
        val match = regex.matchEntire(line)
        assertNotNull(match)
        assertEquals("", match!!.groupValues[5])
    }
}
