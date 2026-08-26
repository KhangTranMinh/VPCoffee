# VPCoffee App — Unit Tests

> Unit test documentation for the main VPCoffee POS app.
> Last updated: 2026-08-26

---

## Test Infrastructure

### Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| JUnit | 4.13.2 | Test framework |
| MockK | 1.13.8 | Kotlin mocking framework |
| kotlinx-coroutines-test | 1.8.1 | Coroutine testing support |
| Turbine | 1.0.0 | Flow testing library |
| Room Runtime JVM | 2.8.3 | JVM Room for DAO tests |
| SQLite JVM | 2.6.1 | JVM SQLite driver |
| SQLite Bundled JVM | 2.6.1 | Bundled SQLite for JVM tests |

### Run Tests
```bash
./gradlew :app:testDebugUnitTest
```

---

## Test Files

### Domain Model Tests

#### `OrderTest.kt`
Tests for the Order domain model.

| Test Case | Description |
|-----------|-------------|
| `total_sums_each_item_price_and_quantity` | Basic total calculation |
| `total_is_zero_for_an_empty_order` | Empty order edge case |
| `total_with_single_item` | Single item calculation |
| `total_with_zero_price_item` | Zero price edge case |
| `total_with_zero_quantity_item` | Zero quantity edge case |
| `total_with_large_quantities` | Large quantity handling |
| `total_with_large_price` | Large price handling |
| `total_with_multiple_items_same_drink` | Duplicate drink items |
| `isSent_false_when_sentAt_is_null` | Not sent state |
| `isSent_true_when_sentAt_is_set` | Sent state |
| `isSent_true_when_sentAt_is_zero` | Zero timestamp edge case |
| `order_preserves_id` | ID preservation |
| `orderItem_preserves_all_fields` | Field preservation |

---

### ViewModel Tests

#### `CatalogViewModelTest.kt`
Tests for the drink catalog management ViewModel.

| Test Case | Description |
|-----------|-------------|
| `drinks emits empty list initially` | Verifies initial state is empty |
| `drinks emits repository data` | Verifies repository data flows to UI |
| `saveDrink does nothing when name is blank` | Input validation |
| `saveDrink does nothing when name is only whitespace` | Whitespace validation |
| `saveDrink does nothing when price text has no digits` | Non-numeric price |
| `saveDrink does nothing when price text is empty` | Empty price |
| `saveDrink calls repository with valid input` | Happy path |
| `saveDrink passes name as-is to repository` | Name handling |
| `saveDrink uses provided id` | ID preservation |
| `saveDrink generates id when not provided` | Auto-generated ID |
| `saveDrink preserves imageUri` | Image URI preservation |
| `saveDrink handles price with mixed characters` | Price parsing |
| `deleteDrink calls repository` | Deletion delegation |
| `deleteDrink calls repository with correct id` | Correct ID delegation |

#### `PointOfSaleViewModelTest.kt`
Tests for the point-of-sale order management ViewModel.

| Test Case | Description |
|-----------|-------------|
| `cart is empty initially` | Initial state |
| `drinks emits repository data` | Repository data flow |
| `addDrink adds new item to cart` | New item addition |
| `addDrink increments quantity for existing item` | Quantity increment |
| `addDrink adds separate items for different drinks` | Multiple items |
| `addDrink preserves drink price` | Price preservation |
| `changeQuantity updates item quantity` | Quantity update |
| `changeQuantity removes item when quantity is 0` | Item removal |
| `changeQuantity removes item when quantity is negative` | Negative quantity |
| `changeQuantity does not affect other items` | Isolation |
| `changeQuantity does nothing for non-existent drink` | Missing item handling |
| `completeOrder does nothing when cart is empty` | Empty guard |
| `completeOrder saves order and clears cart` | Happy path |
| `completeOrder saves order with correct items` | Correct items |
| `completeOrder generates unique order id` | Unique ID generation |

#### `ReportsViewModelTest.kt`
Tests for the orders reporting ViewModel.

| Test Case | Description |
|-----------|-------------|
| `orders emits empty list initially` | Initial state |
| `markOrdersAsSent calls repository` | Delegation |

#### `DebugLogViewModelTest.kt`
Tests for the debug log viewer ViewModel.

| Test Case | Description |
|-----------|-------------|
| `LogEntry data class holds values correctly` | Data class integrity |
| `LogLevel enum has correct labels` | Enum labels |
| `LogLevel enum has all expected values` | Enum completeness |
| `regex parses valid logcat line` | Logcat parsing |
| `regex parses error level` | Error level parsing |
| `regex parses verbose level` | Verbose level parsing |
| `regex rejects invalid line` | Invalid input handling |
| `regex rejects line with wrong date format` | Format validation |
| `regex handles message with colon` | Edge case |
| `regex handles empty message` | Edge case |

---

### Repository Tests

#### `DrinkRepositoryImplTest.kt`
Tests for the drink repository implementation.

| Test Case | Description |
|-----------|-------------|
| `observeDrinks maps entities to domain models` | Entity-to-domain mapping |
| `observeDrinks returns empty list when dao returns empty` | Empty state |
| `saveDrink calls dao upsert with trimmed name` | Name trimming |
| `saveDrink returns drink id` | Return value |
| `deleteDrink calls dao delete` | Delegation |

#### `OrderRepositoryImplTest.kt`
Tests for the order repository implementation.

| Test Case | Description |
|-----------|-------------|
| `observeOrders maps OrderWithItems to domain Order` | Complex mapping |
| `observeOrders preserves sentAt field` | Field preservation |
| `observeOrders returns empty list when dao returns empty` | Empty state |
| `markOrdersAsSent calls dao markAsSent` | Delegation |

---

### DAO Tests

#### `DrinkDaoTest.kt`
Tests for the drink DAO using BundledSQLiteDriver (no Robolectric needed).

| Test Case | Description |
|-----------|-------------|
| `observeAll returns empty list when no drinks` | Empty state |
| `upsert inserts drink` | Insert |
| `upsert updates existing drink` | Update |
| `observeAll orders by name` | Ordering |
| `delete removes drink` | Deletion |
| `delete does nothing when id does not exist` | Missing ID handling |
| `upsert preserves imageUri` | Image URI preservation |
| `upsert handles null imageUri` | Null image handling |
| `upsert handles multiple drinks` | Multiple inserts |
| `delete removes correct drink` | Correct deletion |

#### `OrderDaoTest.kt`
Tests for the order DAO using BundledSQLiteDriver (no Robolectric needed).

| Test Case | Description |
|-----------|-------------|
| `observeAll returns empty list when no orders` | Empty state |
| `upsertOrder inserts order` | Insert |
| `upsertOrder updates existing order` | Update |
| `upsertItems inserts items for order` | Item insertion |
| `observeAll returns orders with items via Relation` | Relation query |
| `observeAll orders by createdAt DESC` | Ordering |
| `markAsSent updates sentAt for specified orders` | Update query |
| `sentAt is null by default` | Default value |
| `markAsSent updates multiple orders` | Batch update |
| `upsertItems updates existing items` | Item update |
| `order with multiple items` | Multiple items |

---

## Test Coverage Summary

| Category | File | Tests | Technique |
|----------|------|-------|-----------|
| Domain Models | `OrderTest.kt` | 13 | Pure unit tests |
| ViewModels | `CatalogViewModelTest.kt` | 14 | MockK + coroutines-test |
| ViewModels | `PointOfSaleViewModelTest.kt` | 15 | MockK + coroutines-test + Turbine |
| ViewModels | `ReportsViewModelTest.kt` | 2 | MockK + coroutines-test |
| ViewModels | `DebugLogViewModelTest.kt` | 10 | Pure unit tests |
| Repositories | `DrinkRepositoryImplTest.kt` | 5 | MockK |
| Repositories | `OrderRepositoryImplTest.kt` | 4 | MockK |
| DAOs | `DrinkDaoTest.kt` | 10 | BundledSQLiteDriver |
| DAOs | `OrderDaoTest.kt` | 11 | BundledSQLiteDriver |
| **Total** | **9 files** | **84** | |

**Grand Total (all components): 102 tests**
- App: 84 tests
- PushTest: 7 tests
- Cloudflare Worker: 11 tests

---

## Techniques Used

| Technique | Purpose |
|-----------|---------|
| **MockK** | Mock dependencies (DAOs, Repositories) |
| **kotlinx-coroutines-test** | `runTest`, `advanceUntilIdle`, `StandardTestDispatcher` |
| **Turbine** | StateFlow assertions (`test { awaitItem() }`) |
| **BundledSQLiteDriver** | In-memory Room database on JVM (no Robolectric) |
| **Room JVM variants** | `room-runtime-jvm`, `sqlite-jvm`, `sqlite-bundled-jvm` |

---

## Notes

- DAO tests use BundledSQLiteDriver with JVM Room variants — no Robolectric or androidTest needed
- `ReportsViewModel.exportAndSendCsv()` is not tested because it requires Android Context
- `DebugLogViewModel` logcat capture is not tested (requires Android runtime)
