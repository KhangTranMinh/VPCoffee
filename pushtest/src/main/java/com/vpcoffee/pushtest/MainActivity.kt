package com.vpcoffee.pushtest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "PushTest"
private const val WORKER_URL =
    "https://send-push-notification.kloverahn.workers.dev/send-push"

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d(
            TAG,
            if (isGranted) "Notification permission granted" else "Notification permission denied"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    PushTestScreen(padding)
                }
            }
        }
    }
}

@Composable
fun PushTestScreen(contentPadding: PaddingValues) {
    val coroutineScope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready") }
    var fcmToken by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    // Get FCM token automatically
    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            fcmToken = if (task.isSuccessful) {
                task.result ?: ""
            } else {
                ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Push Notification Test",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!isSending && fcmToken.isNotBlank()) {
                    isSending = true

                    // Countdown in coroutine
                    coroutineScope.launch {
                        for (i in 3 downTo 1) {
                            status = "Sending in $i..."
                            delay(1000.milliseconds)
                        }

                        try {
                            withContext(Dispatchers.IO) {
                                sendPushNotification(fcmToken)
                            }
                            status = "Sent"
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                            Log.e(TAG, "Failed to send notification", e)
                        } finally {
                            isSending = false
                        }
                    }
                }
            },
            enabled = !isSending && fcmToken.isNotBlank(),
        ) {
            Text("Send Push Notification")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = status,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun sendPushNotification(targetToken: String) {
    val url = URL(WORKER_URL)
    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val amount = (1..999).random()
        val body = JSONObject().apply {
            put("token", targetToken)
            put("title", "Thông báo")
            put("body", "Đã nhận ${amount}.000 đồng")
        }

        Log.d(TAG, "Sending via Cloudflare Worker: $body")

        connection.outputStream.use { os ->
            os.write(body.toString().toByteArray())
        }

        val responseCode = connection.responseCode
        val response = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().readText()
        } else {
            connection.errorStream.bufferedReader().readText()
        }

        Log.d(TAG, "Worker Response: $responseCode - $response")

        if (responseCode !in 200..299) {
            throw Exception("Worker error: $responseCode - $response")
        }
    } finally {
        connection.disconnect()
    }
}
