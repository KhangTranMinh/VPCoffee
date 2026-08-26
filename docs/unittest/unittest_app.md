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
| Room Testing | 2.8.3 | In-memory database testing |

### Run Tests
```bash
./gradlew :app:testDebugUnitTest
```

---

## Test Files

### ViewModel Tests

#### `CatalogViewModelTest.kt`
Tests for the drink catalog management ViewModel.

| Test Case | Description |
|-----------|-------------|
| `drinks emits empty list initially` | Verifies initial state is empty |
| `drinks emits repository data` | Verifies repository data flows to UI |
| `saveDrink does nothing when name is blank` | Input validation |
| `saveDrink does nothing when price is negative` | Input validation |
| `saveDrink does nothing when price text is not numeric` | Input validation |
| `saveDrink calls repository with valid input` | Happy path |
| `saveDrink trims name` | Whitespace handling |
| `saveDrink uses provided id` | ID preservation |
| `deleteDrink calls repository` | Delegation to repository |

#### `PointOfSaleViewModelTest.kt`
Tests for the point-of-sale order management ViewModel.

| Test Case | Description |
|-----------|-------------|
| `cart is empty initially` | Initial state |
| `addDrink adds new item to cart` | New item addition |
| `addDrink increments quantity for existing item` | Quantity increment |
| `addDrink adds separate items for different drinks` | Multiple items |
| `changeQuantity updates item quantity` | Quantity update |
| `changeQuantity removes item when quantity is 0` | Item removal |
| `changeQuantity does not affect other items` | Isolation |
| `completeOrder does nothing when cart is empty` | Empty guard |
| `completeOrder saves order and clears cart` | Happy path |

#### `ReportsViewModelTest.kt`
Tests for the orders reporting ViewModel.

| Test Case | Description |
|-----------|-------------|
| `orders emits empty list initially` | Initial state |
| `orders combines order and drink data` | Data combination |
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
| `saveOrder calls dao upsertOrder and upsertItems` | Transactional save |
| `saveOrder returns order id` | Return value |
| `markOrdersAsSent calls dao markAsSent` | Delegation |

---

### DAO Tests

#### `DrinkDaoTest.kt`
Tests for the drink DAO using Room in-memory database.

| Test Case | Description |
|-----------|-------------|
| `observeAll returns empty list when no drinks` | Empty state |
| `upsert inserts drink` | Insert |
| `upsert updates existing drink` | Update |
| `observeAll orders by name` | Ordering |
| `delete removes drink` | Deletion |
| `delete does nothing when id does not exist` | Missing ID handling |

#### `OrderDaoTest.kt`
Tests for the order DAO using Room in-memory database.

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

---

## Test Coverage Summary

| Category | Tests | Status |
|----------|-------|--------|
| ViewModels | 4 files, ~25 tests | ✅ |
| Repositories | 2 files, ~11 tests | ✅ |
| DAOs | 2 files, ~14 tests | ✅ |
| Domain Models | 1 file (existing) | ✅ |
| **Total** | **9 files, ~50+ tests** | ✅ |

---

## Notes

- DAO tests require `androidTest` (instrumented) or Robolectric for Room in-memory database
- `ReportsViewModel.exportAndSendCsv()` is not tested because it requires Android Context
- `DebugLogViewModel` logcat capture is not tested (requires Android runtime)
