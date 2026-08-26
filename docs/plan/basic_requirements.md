# Basic Requirements

## Purpose

This application supports an individual operating a very small coffee shop.

## Screens

### Point of Sale

- Display all available drinks as cards containing the drink name, image, and price.
- Tapping a drink adds it to the cart. Tapping a drink already in the cart increases its quantity.
- Allow the user to review the cart before completing an order.
- When the user selects **Done**, save the order as completed.

### Catalog & Inventory

- Allow the user to add and manage drinks.
- Each drink must have a name, square image, and price.
- The screen should be designed to support inventory items in the future, such as milk and coffee beans.

### Orders & Reports

- Display completed orders.
- Allow orders to be grouped by date, week, and month.
- Show charts for drink sales.
- Show total income.

## Other Features

- Listen for notifications from a configurable list of applications.
- Initially, the supported application list contains MoMo (`com.mservice.momotransfer`).
- When the app receives a notification from a supported application, use text-to-speech to read the relevant notification text aloud to the user.
- The app should continue to read supported notifications while the screen is locked, where the operating system and device settings allow it. This requires notification-listener access and must account for device-specific background-execution, battery-optimization, audio, and lock-screen restrictions.

## Coding Rules

- Follow Clean Architecture principles.
- Follow Google Material Design guidelines for the app's visual design, components, and interaction patterns.
- Design for older users: use larger text, support system font scaling, and maintain clear, easy-to-read typography.
- Keep UI code separate from business logic.
- Keep business logic independent of framework-specific UI code.
- Organize code by feature first. Within each feature, keep data access, business logic, and presentation/UI concerns in clearly separated layers.
