package com.vpcoffee.feature.debug.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpcoffee.R

private val levelColors = mapOf(
    "V" to Color(0xFF9E9E9E), // grey
    "D" to Color(0xFF2196F3), // blue
    "I" to Color(0xFF4CAF50), // green
    "W" to Color(0xFFFFC107), // yellow
    "E" to Color(0xFFF44336), // red
    "A" to Color(0xFF9C27B0), // purple
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogScreen(viewModel: DebugLogViewModel, onBack: () -> Unit) {
    val logs by viewModel.logs.collectAsState()
    val filterLevel by viewModel.filterLevel.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val filteredLogs = remember(logs, filterLevel) { viewModel.getFilteredLogs() }
    val listState = rememberLazyListState()

    // Auto-scroll to top when new logs arrive
    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.debug_log_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(viewModel.getLogsAsText()))
                        Toast.makeText(context, context.getString(R.string.debug_log_copied), Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.debug_log_copy))
                    }
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.debug_log_clear))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LogLevel.entries.forEach { level ->
                    FilterChip(
                        selected = filterLevel == level,
                        onClick = { viewModel.setFilterLevel(level) },
                        label = { Text(level.label, fontSize = 11.sp) },
                    )
                }
            }

            if (filteredLogs.isEmpty()) {
                Text(
                    text = stringResource(R.string.debug_log_no_logs),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    items(filteredLogs, key = { "${it.timestamp}-${it.pid}-${it.message.hashCode()}" }) { entry ->
                        LogLine(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogLine(entry: LogEntry) {
    val levelColor = levelColors[entry.level] ?: Color.Gray
    Text(
        text = "${entry.timestamp} ${entry.pid}/${entry.level} ${entry.tag}: ${entry.message}",
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = levelColor,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 4.dp),
    )
}
