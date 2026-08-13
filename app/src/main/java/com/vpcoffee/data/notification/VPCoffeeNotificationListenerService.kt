package com.vpcoffee.data.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class VPCoffeeNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        if (statusBarNotification.packageName !in supportedPackages) return
        val extras = statusBarNotification.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val message = listOf(title, text).filter { it.isNotBlank() }.joinToString(". ")
        NotificationSpeechManager.speak(applicationContext, message)
    }

    override fun onDestroy() {
        NotificationSpeechManager.release()
        super.onDestroy()
    }

    private companion object {
        val supportedPackages = setOf("com.mservice.momotransfer")
    }
}
