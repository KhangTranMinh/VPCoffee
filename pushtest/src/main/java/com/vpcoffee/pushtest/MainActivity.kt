package com.vpcoffee.pushtest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

private const val TAG = "PushTest"
private const val FCM_API_URL = "https://fcm.googleapis.com/v1/projects/vpcoffee-791be/messages:send"
private const val OAUTH_TOKEN_URL = "https://oauth2.googleapis.com/token"
private const val SCOPE = "https://www.googleapis.com/auth/firebase.messaging"

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d(TAG, if (isGranted) "Notification permission granted" else "Notification permission denied")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                Surface(modifier = Modifier.fillMaxSize()) {
                    PushTestScreen()
                }
            }
        }
    }
}

@Composable
fun PushTestScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
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
                            delay(1000)
                        }

                        try {
                            withContext(Dispatchers.IO) {
                                sendPushNotification(context, fcmToken)
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
    }
}

private fun sendPushNotification(context: android.content.Context, targetToken: String) {
    // 1. Load service account and get access token
    val serviceAccountJson = loadServiceAccount(context)
    val accessToken = getAccessToken(serviceAccountJson)

    // 2. Send FCM HTTP v1 API request
    val url = URL(FCM_API_URL)
    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.doOutput = true

        val amount = (1..999).random()
        val message = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", targetToken)
                put("notification", JSONObject().apply {
                    put("title", "Thông báo")
                    put("body", "Đã nhận ${amount}.000 đồng")
                })
                put("data", JSONObject().apply {
                    put("title", "Thông báo")
                    put("body", "Đã nhận ${amount}.000 đồng")
                })
            })
        }

        Log.d(TAG, "Sending FCM message: ${message.toString(2)}")

        connection.outputStream.use { os ->
            os.write(message.toString().toByteArray())
        }

        val responseCode = connection.responseCode
        val response = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().readText()
        } else {
            connection.errorStream.bufferedReader().readText()
        }

        Log.d(TAG, "FCM Response: $responseCode - $response")

        if (responseCode !in 200..299) {
            throw Exception("FCM API error: $responseCode - $response")
        }
    } finally {
        connection.disconnect()
    }
}

private fun loadServiceAccount(context: android.content.Context): JSONObject {
    val inputStream = context.assets.open("service-account.json")
    val jsonString = inputStream.bufferedReader().readText()
    return JSONObject(jsonString)
}

private fun getAccessToken(serviceAccountJson: JSONObject): String {
    val clientEmail = serviceAccountJson.getString("client_email")
    val privateKeyPem = serviceAccountJson.getString("private_key")

    // Create JWT
    val now = System.currentTimeMillis() / 1000
    val header = JSONObject().apply {
        put("alg", "RS256")
        put("typ", "JWT")
    }
    val payload = JSONObject().apply {
        put("iss", clientEmail)
        put("scope", SCOPE)
        put("aud", OAUTH_TOKEN_URL)
        put("iat", now)
        put("exp", now + 3600)
    }

    val headerBase64 = Base64.encodeToString(header.toString().toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    val payloadBase64 = Base64.encodeToString(payload.toString().toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    val dataToSign = "$headerBase64.$payloadBase64"

    // Sign with private key
    val privateKey = loadPrivateKey(privateKeyPem)
    val signature = Signature.getInstance("SHA256withRSA").apply {
        initSign(privateKey)
        update(dataToSign.toByteArray())
    }.sign()
    val signatureBase64 = Base64.encodeToString(signature, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    val jwt = "$dataToSign.$signatureBase64"

    // Exchange JWT for access token
    val url = URL(OAUTH_TOKEN_URL)
    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.doOutput = true

        val body = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=$jwt"
        connection.outputStream.use { os ->
            os.write(body.toByteArray())
        }

        val responseCode = connection.responseCode
        val response = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().readText()
        } else {
            val error = connection.errorStream.bufferedReader().readText()
            throw Exception("OAuth token error: $responseCode - $error")
        }

        val responseJson = JSONObject(response)
        return responseJson.getString("access_token")
    } finally {
        connection.disconnect()
    }
}

private fun loadPrivateKey(privateKeyPem: String): PrivateKey {
    val privateKeyContent = privateKeyPem
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace("\\s+".toRegex(), "")

    val decoded = Base64.decode(privateKeyContent, Base64.DEFAULT)
    val keySpec = PKCS8EncodedKeySpec(decoded)
    return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
}
