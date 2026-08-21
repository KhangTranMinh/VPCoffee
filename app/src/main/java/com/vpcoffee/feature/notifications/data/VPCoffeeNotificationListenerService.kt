package com.vpcoffee.feature.notifications.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class VPCoffeeNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        Log.d("VPCoffeeListener", "Notification from: ${statusBarNotification.packageName}")
        if (statusBarNotification.packageName !in supportedPackages) return
        val extras = statusBarNotification.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val message = listOf(title, text).filter { it.isNotBlank() }.joinToString(". ")
        Log.d("VPCoffeeListener", "Speaking: $message")
        NotificationSpeechManager.speak(applicationContext, message)
    }

    override fun onDestroy() {
        NotificationSpeechManager.release()
        super.onDestroy()
    }

    private companion object {
        // TODO: Change to "com.mservice.momotransfer" for production
        const val PUSH_TEST_PACKAGE = "com.vpcoffee.pushtest"
        val supportedPackages = setOf("com.mservice.momotransfer", PUSH_TEST_PACKAGE)
    }
}
