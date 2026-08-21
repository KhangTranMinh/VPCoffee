package com.vpcoffee.pushtest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "PushTestService"
        private const val CHANNEL_ID = "push_test_channel"
        private const val CHANNEL_NAME = "Push Test Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // Send the new token to your server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        // Create notification channel for Android 8+
        createNotificationChannel()

        // Show notification
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "Push Test",
                body = notification.body ?: "You received a push notification!"
            )
        } ?: run {
            // If no notification payload, check data payload
            val title = message.data["title"] ?: "Push Test"
            val body = message.data["body"] ?: "You received a push notification!"
            showNotification(title, body)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for push test notifications"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Material Design 3 Primary color (#6750A4)
        val purpleColor = 0xFF6750A4.toInt()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle(title)
            .setContentText(body)
            .setColor(purpleColor)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
