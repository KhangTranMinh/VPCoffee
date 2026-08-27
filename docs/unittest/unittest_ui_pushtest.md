# PushTest — UI Tests

> UI test documentation for the PushTest companion app.
> Last updated: 2026-08-26

---

## Overview

| Test Type | Framework | Runs On | Purpose |
|-----------|-----------|---------|---------|
| UI Tests | Compose Testing | Device/Emulator | Test user interactions |

---

## Test Infrastructure

### Dependencies
| Library | Purpose |
|---------|---------|
| Compose UI Test JUnit4 | Compose testing framework |
| Compose UI Test Manifest | Test activity for Compose |
| Espresso | Android UI testing |
| AndroidX JUnit | Test runner |

### Run Tests
```bash
# Requires connected device or emulator
./gradlew :pushtest:connectedDebugAndroidTest
```

---

## Test Files

### `PushTestScreenTest.kt`
Tests for the push notification test screen.

| Test Case | Description |
|-----------|-------------|
| `screen_showsTitle` | Verifies "Push Notification Test" is displayed |
| `screen_showsStatus` | Verifies "Ready" status is displayed |
| `screen_showsVersion` | Verifies version info is displayed |

---

## Test Coverage Summary

| Category | Tests | Technique |
|----------|-------|-----------|
| PushTestScreen | 3 | Compose Testing |
| **Total** | **3** | |

---

## Techniques Used

| Technique | Purpose |
|-----------|---------|
| **Compose Testing** | `createComposeRule()`, semantic matchers |

---

## Notes

- UI tests require a connected device or emulator
- Tests verify static UI elements (title, status, version)
- Button interaction tests are skipped due to Firebase dependency
