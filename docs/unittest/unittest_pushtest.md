# PushTest — Unit Tests

> Unit test documentation for the PushTest companion app.
> Last updated: 2026-08-26

---

## Test Infrastructure

### Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| JUnit | 4.13.2 | Test framework |
| MockK | 1.13.8 | Kotlin mocking framework |
| kotlinx-coroutines-test | 1.8.1 | Coroutine testing support |

### Run Tests
```bash
./gradlew :pushtest:testDebugUnitTest
```

---

## Test Files

### `PushNotificationServiceTest.kt`
Tests for the push notification message handling logic.

| Test Case | Description |
|-----------|-------------|
| `extracts title and body from notification payload` | Standard FCM notification |
| `falls back to data payload when notification is null` | Data-only message |
| `uses defaults when both payloads are empty` | Default values |
| `notification payload takes priority over data payload` | Priority handling |
| `handles partial notification payload` | Mixed payload |
| `handles partial data payload` | Partial data |
| `notification IDs are unique` | ID generation |

---

## Test Coverage Summary

| Component | Tests | Status |
|-----------|-------|--------|
| Message payload extraction | 6 tests | ✅ |
| Notification ID generation | 1 test | ✅ |
| **Total** | **7 tests** | ✅ |

---

## Limitations

### What's NOT Tested

| Component | Reason |
|-----------|--------|
| `PushNotificationService.onMessageReceived()` | Tightly coupled to Android `NotificationManager` and `NotificationChannel`. Requires instrumented tests or Robolectric. |
| `PushNotificationService.createNotificationChannel()` | Android framework dependency |
| `PushNotificationService.showNotification()` | Android framework dependency |
| `MainActivity.sendPushNotification()` | Raw `HttpURLConnection` calls, no abstraction layer |
| `MainActivity.PushTestScreen` | Compose UI, requires Compose testing framework |

### Recommendations for Future Testing

1. **Refactor `MainActivity`** — Extract HTTP client into an interface for mocking
2. **Add Robolectric** — Enable local Android framework simulation
3. **Add Compose UI tests** — Test the `PushTestScreen` composable
4. **Add instrumented tests** — Full integration tests for `PushNotificationService`
