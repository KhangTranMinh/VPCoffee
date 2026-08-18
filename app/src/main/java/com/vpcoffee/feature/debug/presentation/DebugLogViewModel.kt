package com.vpcoffee.feature.debug.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LogEntry(
    val timestamp: String,
    val pid: String,
    val level: String,
    val tag: String,
    val message: String,
)

enum class LogLevel(val label: String) { V("Verbose"), D("Debug"), I("Info"), W("Warn"), E("Error"), A("All") }

class DebugLogViewModel : ViewModel() {

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _filterLevel = MutableStateFlow(LogLevel.A)
    val filterLevel: StateFlow<LogLevel> = _filterLevel.asStateFlow()

    private var logcatProcess: Process? = null

    init {
        startCapture()
    }

    fun startCapture() {
        stopCapture()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val pid = android.os.Process.myPid()
                    val process = Runtime.getRuntime().exec(arrayOf("logcat", "-v", "threadtime", "--pid=$pid", "*:V"))
                    logcatProcess = process
                    process.inputStream.bufferedReader().useLines { lines ->
                        lines.forEach { line ->
                            val entry = parseLine(line) ?: return@forEach
                            val current = _logs.value.toMutableList()
                            current.add(entry)
                            // Keep last 2000 entries to avoid memory issues
                            _logs.value = if (current.size > 2000) current.takeLast(2000) else current
                        }
                    }
                } catch (_: Exception) {
                    // Process killed or IO error — expected on stop
                }
            }
        }
    }

    fun stopCapture() {
        logcatProcess?.destroyForcibly()
        logcatProcess = null
    }

    fun clearLogs() {
        _logs.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Runtime.getRuntime().exec(arrayOf("logcat", "-c")).waitFor()
            } catch (_: Exception) { }
        }
    }

    fun setFilterLevel(level: LogLevel) {
        _filterLevel.value = level
    }

    fun getFilteredLogs(): List<LogEntry> {
        val level = _filterLevel.value
        return if (level == LogLevel.A) _logs.value
        else _logs.value.filter { it.level == level.name }
    }

    fun getLogsAsText(): String {
        return _logs.value.joinToString("\n") { entry ->
            "${entry.timestamp} ${entry.pid}/${entry.level} ${entry.tag}: ${entry.message}"
        }
    }

    override fun onCleared() {
        stopCapture()
        super.onCleared()
    }

    private fun parseLine(line: String): LogEntry? {
        // Format: "MM-DD HH:MM:SS.mmm  PID  TID LEVEL TAG: message"
        // Example: "08-18 14:30:15.123  1234  5678 D MainActivity: onCreate called"
        val regex = Regex("""^(\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+\d+\s+([VDIWEA])\s+(.+?):\s*(.*)$""")
        val match = regex.matchEntire(line) ?: return null
        return LogEntry(
            timestamp = match.groupValues[1],
            pid = match.groupValues[2],
            level = match.groupValues[3],
            tag = match.groupValues[4],
            message = match.groupValues[5],
        )
    }
}