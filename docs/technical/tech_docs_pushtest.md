# PushTest — Technical Documentation

> Companion Android app for testing FCM push notifications.
> Last updated: 2026-08-26

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Sending Push Notifications](#3-sending-push-notifications)
4. [Receiving Push Notifications](#4-receiving-push-notifications)
5. [FCM Token Handling](#5-fcm-token-handling)
6. [Dependencies](#6-dependencies)
7. [Build & Deployment](#7-build--deployment)
8. [Known Limitations](#8-known-limitations)

---

## 1. Overview

**PushTest** is a standalone Android application (`com.vpcoffee.pushtest`) designed to test Firebase Cloud Messaging (FCM) push notifications end-to-end. It serves as a self-contained test harness for the VPCoffee notification pipeline.

**Flow:**
```
User taps button → App sends FCM token to Cloudflare Worker → Worker calls FCM API → FCM delivers notification → App displays notification
```

| Property | Value |
|----------|-------|
| Package | `com.vpcoffee.pushtest` |
| App Name | VPCoffe Push Test |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 37 |
| Version | 1.0.0 (versionCode 6) |

---

## 2. Architecture

### Key Classes

| Class | File | Responsibility |
|-------|------|----------------|
| `MainActivity` | `MainActivity.kt` | Single-activity Compose UI. Handles notification permission, fetches FCM token, renders test screen, orchestrates send flow. |
| `PushTestScreen` | `MainActivity.kt` | Compose UI. Manages `status`, `fcmToken`, `isSending` state. |
| `sendPushNotification()` | `MainActivity.kt` | HTTP POST to Cloudflare Worker with FCM token and payload. |
| `PushNotificationService` | `PushNotificationService.kt` | FCM message receiver. Extends `FirebaseMessagingService`. Displays system notifications. |

### Data Flow

```
┌─────────────┐     HTTPS POST      ┌─────────────────────┐     FCM v1 API     ┌─────────┐
│  PushTest   │ ──────────────────► │  Cloudflare Worker  │ ─────────────────► │   FCM   │
│  (Android)  │  {token,title,body} │  /send-push         │  {message:{...}}   │  Server │
└─────────────┘                     └─────────────────────┘                    └────┬────┘
       ▲                                                                             │
       │                              Push Notification                               │
       └─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Sending Push Notifications

### Entry Point
`sendPushNotification(targetToken: String)` in `MainActivity.kt` (lines 168-205)

### Flow
1. User taps "Send Push Notification" button
2. 3-second countdown runs in a coroutine
3. On `Dispatchers.IO`, opens `HttpURLConnection` to Cloudflare Worker:
   ```
   POST https://send-push-notification.kloverahn.workers.dev/send-push
   ```
4. Sends JSON body:
   ```json
   {
     "token": "<FCM registration token>",
     "title": "Thông báo",
     "body": "Đã nhận {random 1-999}.000 đồng"
   }
   ```
5. Reads response; throws exception if status is not 2xx

### Key Details
- The app does NOT call the FCM API directly
- All authentication with Firebase is handled server-side by the Cloudflare Worker
- No service account credentials exist in the app

---

## 4. Receiving Push Notifications

### Service
`PushNotificationService` extends `FirebaseMessagingService`

### Registration
Declared in `AndroidManifest.xml` with intent filter `com.google.firebase.MESSAGING_EVENT` (not exported).

### Methods

#### `onNewToken(token: String)`
- Called by FCM when the device's registration token is refreshed
- Currently only logs the new token
- **Limitation:** Does not persist or send the updated token to a server

#### `onMessageReceived(message: RemoteMessage)`
1. Creates notification channel (`push_test_channel`)
2. Checks for `notification` payload first (FCM "notification" key)
3. Falls back to `data` payload if no notification payload present
4. Calls `showNotification(title, body)` to display system notification

### Notification Display
- Channel ID: `push_test_channel`
- Channel name: Push Test Notifications
- Icon: system `star_on`
- Priority: `PRIORITY_DEFAULT`
- Auto-cancel: true
- Unique ID: `System.currentTimeMillis().toInt()` (stacks notifications)

---

## 5. FCM Token Handling

### Token Acquisition
- On first composition of `PushTestScreen`, `LaunchedEffect(Unit)` calls `FirebaseMessaging.getInstance().token`
- Result stored in `fcmToken` Compose state variable
- Button disabled until valid token available: `enabled = !isSending && fcmToken.isNotBlank()`

### Token Usage
- Sent as `token` field in JSON body to Cloudflare Worker
- No local persistence or server-side registration

### Token Refresh
- `onNewToken` in `PushNotificationService` logs the refreshed token
- **Limitation:** Does NOT propagate back to UI state
- If token rotates while app is running, `fcmToken` state becomes stale until app restart

---

## 6. Dependencies

### Plugins
| Plugin | Purpose |
|--------|---------|
| `android.application` | Android app build |
| `kotlin.compose` | Jetpack Compose compiler |
| `google.services` | Google Services (processes `google-services.json`) |

### Libraries
| Library | Purpose |
|---------|---------|
| `androidx.core.ktx` | Kotlin extensions for Android core |
| `androidx.lifecycle.runtime.ktx` | Lifecycle-aware coroutine support |
| `androidx.activity.compose` | Compose integration with ComponentActivity |
| `androidx.compose.bom` | Compose Bill of Materials |
| `androidx.compose.ui` | Compose UI toolkit |
| `androidx.compose.material3` | Material 3 design |
| `firebase.messaging` | Firebase Cloud Messaging SDK |
| `com.google.android.material:material:1.12.0` | Notification compat |

---

## 7. Build & Deployment

### Local Build
```bash
# Build and install on device
./gradlew :pushtest:installDebug
adb shell am start -n com.vpcoffee.pushtest/.MainActivity
```

### Fastlane Deployment
```bash
bundle exec fastlane deploy_pushtest
```

**What Fastlane does:**
1. Auto-increments `versionCode`
2. Builds debug APK
3. Deploys to Firebase App Distribution (internal group)
4. Copies APK to repo root for GitHub Pages
5. Commits version bump with `[skip apk deploy]` tag
6. Pushes to GitHub

---

## 8. Known Limitations

| Issue | Description |
|-------|-------------|
| **Stale token risk** | If `onNewToken` fires, the updated token is only logged, not reflected in UI. App continues using old token until restart. |
| **No server-side token persistence** | Token is fetched at runtime and sent with each request. No local storage or server registration. |
| **Channel created on every message** | `createNotificationChannel()` is called inside `onMessageReceived` each time. Android deduplicates, but it would be more idiomatic to create once at startup. |
| **No data-only message handling in Worker path** | Worker sends notification payload. Service handles data-only fallback for resilience. |
