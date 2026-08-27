# VPCoffee App — UI Tests

> UI test documentation for the main VPCoffee POS app.
> Last updated: 2026-08-26

---

## Overview

| Test Type | Framework | Runs On | Purpose |
|-----------|-----------|---------|---------|
| UI Tests | Compose Testing | Device/Emulator | Test user interactions |

**Note:** Screenshot tests are not yet available. Paparazzi is incompatible with AGP 9.3.1, and Compose Preview Screenshot Testing plugin is not yet available for AGP 9.

---

## Test Infrastructure

### Dependencies
| Library | Purpose |
|---------|---------|
| Compose UI Test JUnit4 | Compose testing framework |
| Compose UI Test Manifest | Test activity for Compose |
| Espresso | Android UI testing |
| AndroidX JUnit | Test runner |
| MockK Android | Mock ViewModels |

### Run Tests
```bash
# Requires connected device or emulator
./gradlew :app:connectedDebugAndroidTest
```

---

## Test Files

### `PointOfSaleScreenTest.kt`
Tests for the point-of-sale screen.

| Test Case | Description |
|-----------|-------------|
| `drinkGrid_showsDrinks` | Drinks are displayed in grid |
| `drinkGrid_showsMultipleDrinks` | Multiple drinks displayed correctly |

### `CatalogScreenTest.kt`
Tests for the catalog screen.

| Test Case | Description |
|-----------|-------------|
| `drinkList_showsDrinks` | Drinks are displayed in list |
| `drinkList_showsMultipleDrinks` | Multiple drinks displayed correctly |

### Fake ViewModels

| File | Purpose |
|------|---------|
| `FakePointOfSaleViewModel.kt` | Real ViewModel with fake repositories |
| `FakeCatalogViewModel.kt` | Real ViewModel with fake repositories |
| `FakeReportsViewModel.kt` | Real ViewModel with fake repositories |

---

## Test Coverage Summary

| Category | Tests | Technique |
|----------|-------|-----------|
| PointOfSaleScreen | 2 | Compose Testing |
| CatalogScreen | 2 | Compose Testing |
| **Total** | **4** | |

---

## Techniques Used

| Technique | Purpose |
|-----------|---------|
| **Compose Testing** | `createComposeRule()`, semantic matchers |
| **Fake Repositories** | Provide data without real database |
| **Real ViewModels** | Test actual ViewModel behavior |

---

## Notes

- UI tests require a connected device or emulator
- `AndroidView` with `ImageView` is avoided in tests (drinks without images)
- Empty state tests are skipped due to `stateIn` with `WhileSubscribed` timing issues
- Screenshot tests will be added when Paparazzi supports AGP 9
