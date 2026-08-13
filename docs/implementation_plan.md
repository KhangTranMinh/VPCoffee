# Implementation Plan

This plan implements the requirements in [Basic Requirements](basic_requirements.md).

## 1. Establish the Foundation

- Configure the Android project with Kotlin, Jetpack Compose, and Material Design 3.
- Define the Clean Architecture layers:
  - **Presentation:** Compose screens, view models, and UI state.
  - **Domain:** business models, use cases, and repository interfaces.
  - **Data:** database entities, data sources, repository implementations, and Android system integrations.
- Ensure that presentation code depends on domain code, while domain code does not depend on Android or UI frameworks.
- Add a local database for drinks, orders, and order line items.

## 2. Build the Catalog & Inventory Screen

- Create the drink model with a name, square image reference, and price.
- Implement creating, editing, viewing, and removing drinks.
- Validate required fields and prevent invalid prices.
- Design the data model so inventory items, such as milk and coffee beans, can be added later without restructuring the app.

## 3. Build the Point of Sale Screen

- Display available drinks in large, easy-to-read cards with an image, name, and price.
- Add tapped drinks to a cart and increase quantity when the same drink is tapped again.
- Provide a cart-review screen where quantities can be changed or items removed.
- Calculate the order total and save the completed order when the user selects **Done**.
- Clear the cart only after the order is saved successfully.

## 4. Build Orders & Reports

- Display completed orders grouped by day, week, and month.
- Calculate total income from completed orders.
- Show drink-sales charts using completed order data.
- Provide clear empty, loading, and error states.

## 5. Add Notification Speech Support

- Implement an Android notification-listener service in the data layer.
- Filter notifications using a configurable package-name list, initially including MoMo (`com.mservice.momotransfer`).
- Extract only the relevant notification text and pass it to a domain use case that determines whether it should be spoken.
- Use Android text-to-speech to read approved notification text aloud.
- Provide a setup flow that explains and opens the system settings required for notification-listener access.
- Test speech with the screen locked. Document that device settings, battery optimization, audio focus, and OS restrictions can prevent background speech on some devices.

## 6. Accessibility and Visual Design

- Use Material Design 3 components and interaction patterns.
- Use larger default text, generous touch targets, strong contrast, and plain labels for older users.
- Respect system font scaling and confirm that layouts remain usable at larger font sizes.
- Avoid relying on color alone to communicate status or actions.

## 7. Test and Validate

- Unit-test domain use cases, including cart calculations, order completion, report totals, and notification filtering.
- Test repository implementations with a local test database.
- Add UI tests for the core drink-to-order flow and accessibility-critical layouts.
- Manually test notification permissions, text-to-speech, screen-locked behavior, and recovery after app restarts.

## Delivery Order

1. Foundation and local data storage.
2. Catalog & Inventory.
3. Point of Sale and order persistence.
4. Orders & Reports.
5. Notification speech support.
6. Accessibility review, testing, and release preparation.

## Decisions Needed Before Implementation

- Whether drinks need editing and deletion in the first release.
- Whether completed orders can be changed, cancelled, or deleted.
- Which notification text should be spoken and whether sensitive information must be redacted.
- Whether speech should have a user-controlled on/off switch, volume setting, or quiet hours.
