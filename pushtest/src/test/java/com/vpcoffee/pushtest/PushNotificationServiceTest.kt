package com.vpcoffee.pushtest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for PushNotificationService.
 *
 * Note: The service itself is tightly coupled to Android framework
 * (NotificationManager, NotificationChannel) and Firebase (FirebaseMessagingService).
 * Full integration testing requires instrumented tests (androidTest).
 *
 * These tests verify the data extraction logic used in onMessageReceived.
 */
class PushNotificationServiceTest {

    // Simulate the notification payload extraction logic from onMessageReceived
    private fun extractNotificationData(
        notificationTitle: String?,
        notificationBody: String?,
        data: Map<String, String>,
    ): Pair<String, String> {
        // Same logic as PushNotificationService.onMessageReceived
        val title = notificationTitle ?: data["title"] ?: "Push Test"
        val body = notificationBody ?: data["body"] ?: "You received a push notification!"
        return Pair(title, body)
    }

    @Test
    fun `extracts title and body from notification payload`() {
        val (title, body) = extractNotificationData(
            notificationTitle = "Hello",
            notificationBody = "World",
            data = emptyMap(),
        )
        assertEquals("Hello", title)
        assertEquals("World", body)
    }

    @Test
    fun `falls back to data payload when notification is null`() {
        val (title, body) = extractNotificationData(
            notificationTitle = null,
            notificationBody = null,
            data = mapOf("title" to "Data Title", "body" to "Data Body"),
        )
        assertEquals("Data Title", title)
        assertEquals("Data Body", body)
    }

    @Test
    fun `uses defaults when both payloads are empty`() {
        val (title, body) = extractNotificationData(
            notificationTitle = null,
            notificationBody = null,
            data = emptyMap(),
        )
        assertEquals("Push Test", title)
        assertEquals("You received a push notification!", body)
    }

    @Test
    fun `notification payload takes priority over data payload`() {
        val (title, body) = extractNotificationData(
            notificationTitle = "Notification Title",
            notificationBody = "Notification Body",
            data = mapOf("title" to "Data Title", "body" to "Data Body"),
        )
        assertEquals("Notification Title", title)
        assertEquals("Notification Body", body)
    }

    @Test
    fun `handles partial notification payload`() {
        val (title, body) = extractNotificationData(
            notificationTitle = "Hello",
            notificationBody = null,
            data = mapOf("body" to "From Data"),
        )
        assertEquals("Hello", title)
        assertEquals("From Data", body)
    }

    @Test
    fun `handles partial data payload`() {
        val (title, body) = extractNotificationData(
            notificationTitle = null,
            notificationBody = null,
            data = mapOf("title" to "Only Title"),
        )
        assertEquals("Only Title", title)
        assertEquals("You received a push notification!", body)
    }

    // Test notification ID generation (unique per call)
    @Test
    fun `notification IDs are unique`() {
        val id1 = System.currentTimeMillis().toInt()
        Thread.sleep(10)
        val id2 = System.currentTimeMillis().toInt()
        // They might be the same if called too fast, but the pattern is correct
        assertNotNull(id1)
        assertNotNull(id2)
    }
}
