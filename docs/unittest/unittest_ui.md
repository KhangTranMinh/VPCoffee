# VPCoffee — UI Tests

> UI test documentation for the VPCoffee POS app.
> Last updated: 2026-08-26

---

## Overview

| Test Type | Framework | Runs On | Purpose |
|-----------|-----------|---------|---------|
| UI Tests | Compose Testing | Device/Emulator | Test user interactions |

**Note:** Screenshot tests are not yet available. Paparazzi is incompatible with AGP 9.3.1, and Compose Preview Screenshot Testing plugin is not yet available for AGP 9. Will be added when either option supports AGP 9.

---

## UI Tests (androidTest)

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
./gradlew :app:connectedDebugAndroidTest
```

### Test Files

#### `PointOfSaleScreenTest.kt`
Tests for the point-of-sale screen.

| Test Case | Description |
|-----------|-------------|
| `emptyState_showsAddDrinksMessage` | Empty state displays message |
| `drinkGrid_showsDrinks` | Drinks are displayed in grid |
| `addDrink_showsCartButton` | Adding drink shows cart button |
| `cartDialog_showsItems` | Cart dialog displays items |
| `completeOrder_clearsCart` | Completing order clears cart |

#### `CatalogScreenTest.kt`
Tests for the catalog screen.

| Test Case | Description |
|-----------|-------------|
| `emptyState_showsNoDrinksMessage` | Empty state displays message |
| `drinkList_showsDrinks` | Drinks are displayed in list |
| `addButton_opensDialog` | Add button opens editor dialog |
| `deleteDrink_removesFromList` | Deleting drink removes from list |

#### `ReportsScreenTest.kt`
Tests for the reports screen.

| Test Case | Description |
|-----------|-------------|
| `emptyState_showsNoOrdersMessage` | Empty state displays message |
| `tabSwitching_works` | Day/Week/Month tabs work |
| `orderList_showsOrders` | Orders are displayed |
| `orderDetails_showsTotal` | Order details show total |

### Fake ViewModels

| File | Purpose |
|------|---------|
| `FakePointOfSaleViewModel.kt` | Controllable state for POS tests |
| `FakeCatalogViewModel.kt` | Controllable state for catalog tests |
| `FakeReportsViewModel.kt` | Controllable state for reports tests |

---

## Test Coverage Summary

| Category | Tests | Technique |
|----------|-------|-----------|
| UI Tests | 8 | Compose Testing + MockK |
| **Total** | **8** | |

---

## Techniques Used

| Technique | Purpose |
|-----------|---------|
| **Compose Testing** | `createComposeRule()`, semantic matchers |
| **MockK** | Mock ViewModels with controllable state |
| **Fake Repositories** | Provide data without real database |

---

## Notes

- UI tests require a connected device or emulator
- `AndroidView` with `ImageView` is avoided in tests (drinks without images)
- Screenshot tests will be added when Paparazzi supports AGP 9
