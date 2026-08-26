# VPCoffee — Technical Documentation

> Point-of-sale Android application for coffee shops.
> Last updated: 2026-08-26

---

## Table of Contents

1. [Overview](#1-overview)
2. [Features](#2-features)
3. [Architecture](#3-architecture)
4. [Screens & Navigation](#4-screens--navigation)
5. [Database Schema](#5-database-schema)
6. [Dependencies](#6-dependencies)
7. [Build & Deployment](#7-build--deployment)
8. [Permissions](#8-permissions)
9. [Key Technical Details](#9-key-technical-details)

---

## 1. Overview

**VPCoffee** is a native Android POS application built with Kotlin and Jetpack Compose. It enables coffee shop staff to manage a drink catalog, take orders via a grid-based interface, track sales with reporting, and receive audible notifications for incoming MoMo mobile payments.

| Property | Value |
|----------|-------|
| Package | `com.vpcoffee` |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 37 |
| Language | Kotlin 2.2.10 |
| UI Framework | Jetpack Compose (BOM 2026.02.01) |
| Database | Room 2.8.3 |
| Currency | Vietnamese Dong (VND) |

---

## 2. Features

### Point of Sale
- 2-column grid of drink items with images and prices
- Tap to add to cart with quantity controls
- Cart dialog for reviewing and adjusting orders
- "Done" button saves the order to the database

### Catalog Management
- Add, edit, and delete drinks
- Each drink has a name, price (VND), and optional photo
- Photo support: camera capture or gallery selection with square cropping
- Images stored in app's internal `files/images/` directory

### Orders & Reporting
- View completed orders filtered by day, week, or month
- Total income summary card
- Per-drink sales breakdown
- Order detail dialog
- CSV export via email (Send button in top bar)
- Tracks which orders have been exported (`sentAt` column)

### Notification Speech
- Listens for system notifications from MoMo (`com.mservice.momotransfer`) and the PushTest companion app
- Reads notifications aloud using Android Text-to-Speech
- Runs as a persistent foreground service

### Debug Log Viewer
- Hidden screen activated by shaking the device (accelerometer-based, 3+ shakes within 500ms, magnitude > 25g)
- Displays live logcat output with level filtering (V/D/I/W/E/A)
- Copy to clipboard and clear functionality

---

## 3. Architecture

### Pattern: Clean Architecture with Feature-based Packaging

```
com.vpcoffee/
├── core/
│   ├── data/local/          -- AppDatabase (Room), migrations
│   ├── di/                  -- AppContainer (manual dependency injection)
│   └── ui/                  -- CurrencyFormatter
├── feature/
│   ├── catalog/
│   │   ├── data/local/      -- DrinkEntity, DrinkDao
│   │   ├── data/image/      -- SquareImageProcessor
│   │   ├── data/repository/ -- DrinkRepositoryImpl
│   │   ├── domain/model/    -- Drink
│   │   ├── domain/repository/ -- DrinkRepository (interface)
│   │   └── presentation/    -- CatalogScreen, CatalogViewModel
│   ├── orders/
│   │   ├── data/local/      -- OrderEntity, OrderItemEntity, OrderDao
│   │   ├── data/repository/ -- OrderRepositoryImpl
│   │   ├── domain/model/    -- Order, OrderItem
│   │   ├── domain/repository/ -- OrderRepository (interface)
│   │   └── presentation/    -- ReportsScreen, ReportsViewModel
│   ├── sales/
│   │   └── presentation/    -- PointOfSaleScreen, PointOfSaleViewModel
│   ├── notifications/
│   │   └── data/            -- VPCoffeeNotificationListenerService, NotificationSpeechService, NotificationSpeechManager
│   ├── debug/
│   │   └── presentation/    -- DebugLogScreen, DebugLogViewModel
│   └── settings/
│       └── presentation/    -- SettingsScreen
└── ui/theme/                -- Color, Theme, Type (Material3 purple theme)
```

### Key Patterns

| Pattern | Implementation |
|---------|---------------|
| **MVVM** | Each feature has a ViewModel exposing `StateFlow` to Compose screens via `collectAsStateWithLifecycle()` |
| **Repository** | Domain interfaces with data-layer implementations |
| **Manual DI** | `AppContainer` creates Room database and repositories; ViewModels receive dependencies via `ViewModelProvider.Factory` |
| **Reactive Data** | Room DAOs return `Flow<List<Entity>>`, mapped to domain models in repositories, exposed as `StateFlow` in ViewModels |
| **Single-Activity** | No Fragments; all UI is Compose with `Scaffold`, `NavigationBar`, and `Dialog` composables |

---

## 4. Screens & Navigation

The app uses a single `MainActivity` with Jetpack Compose navigation via a bottom `NavigationBar`.

| Tab | Screen | ViewModel | Purpose |
|-----|--------|-----------|---------|
| **Sale** | `PointOfSaleScreen` | `PointOfSaleViewModel` | Drink grid, cart, order completion |
| **Catalog** | `CatalogScreen` | `CatalogViewModel` | Drink CRUD with image support |
| **Reports** | `ReportsScreen` | `ReportsViewModel` | Order history, income stats, CSV export |
| **Settings** | `SettingsScreen` | — | Notification access, email config, version |

**Hidden:** `DebugLogScreen` / `DebugLogViewModel` — activated by device shake.

---

## 5. Database Schema

**Database:** `vpcoffee.db`, version 3

### Tables

#### `drinks`
| Column | Type | Description |
|--------|------|-------------|
| `id` | Long (PK, auto) | Unique drink ID |
| `name` | String | Drink name |
| `price` | Int | Price in VND |
| `imageUri` | String? | Path to image file |

#### `orders`
| Column | Type | Description |
|--------|------|-------------|
| `id` | Long (PK, auto) | Unique order ID |
| `totalAmount` | Int | Total order amount in VND |
| `timestamp` | Long | Order creation time (epoch ms) |
| `sentAt` | Long? | Export timestamp (null = not exported) |

#### `order_items`
| Column | Type | Description |
|--------|------|-------------|
| `orderId` | Long (FK) | References `orders.id` |
| `drinkId` | Long (FK) | References `drinks.id` |
| `quantity` | Int | Number of this drink in the order |
| `priceAtOrder` | Int | Price at time of order (snapshot) |

**Primary key:** Composite (`orderId` + `drinkId`)

### Migrations
- **2 → 3:** Adds `sentAt` column to `orders` table for tracking exported orders.

---

## 6. Dependencies

### Build Plugins
| Plugin | Version |
|--------|---------|
| Android Gradle Plugin (AGP) | 9.3.1 |
| Kotlin | 2.2.10 |
| KSP | 2.2.10-2.0.2 |

### Libraries
| Category | Library | Version |
|----------|---------|---------|
| UI | Jetpack Compose BOM | 2026.02.01 |
| UI | Material3 | via BOM |
| UI | Material Icons Extended | via BOM |
| Architecture | Lifecycle Runtime KTX | 2.6.1 |
| Architecture | Lifecycle ViewModel KTX + Compose | 2.6.1 |
| Database | Room (runtime, ktx, compiler) | 2.8.3 |
| DI | Manual (AppContainer) | — |

**Note:** The main app does NOT use Firebase directly. Firebase Cloud Messaging is only in the companion `pushtest` module.

---

## 7. Build & Deployment

### Local Build
```bash
# Build and install on device
./gradlew installDebug
adb shell am start -n com.vpcoffee/.MainActivity
```

### Fastlane Deployment
```bash
bundle exec fastlane deploy_vpcoffee
```

**What Fastlane does:**
1. Auto-increments `versionCode`
2. Builds debug APK
3. Deploys to Firebase App Distribution (internal group)
4. Copies APK to repo root for GitHub Pages
5. Commits version bump with `[skip apk deploy]` tag
6. Pushes to GitHub

### GitHub Actions
- `deploy-apk.yml` builds APK on push to main
- Skips if commit message contains `[skip apk deploy]`
- Deploys to GitHub Pages with download links

### GitHub Pages
Serves `index.html` with download links for both VPCoffee and PushTest APKs.

---

## 8. Permissions

| Permission | Purpose |
|------------|---------|
| `FOREGROUND_SERVICE` | Persistent TTS notification speech service |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Required for Android 14+ foreground service type |
| `POST_NOTIFICATIONS` | Display notifications on Android 13+ |
| `INTERNET` | Network access (PushTest module) |

---

## 9. Key Technical Details

### Currency Formatting
- Vietnamese Dong (VND) using `NumberFormat.getNumberInstance(Locale.getDefault())`
- Display format: `{number} đ` (e.g., "50,000 đ")

### Image Handling
- `SquareImageProcessor` crops images to square aspect ratio (center crop)
- Images stored in app's internal `files/images/` directory
- Uses `FileProvider` for secure URI sharing between camera/gallery and the app
- `AndroidView` with `ImageView` used to display images in Compose (interop)

### Theme
- Custom Material3 purple color scheme (dynamic color disabled)
- Supports light and dark themes
- Purple primary (`#6750A4`), with pink tertiary

### Notification Listener
- `VPCoffeeNotificationListenerService` intercepts system notifications
- Filters for MoMo (`com.mservice.momotransfer`) and PushTest app
- Passes title + text to `NotificationSpeechService` for TTS
- Requires user-granted notification access in system settings
