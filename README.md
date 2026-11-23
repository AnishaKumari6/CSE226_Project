# Shiv Traders – Inventory Management App

A modern, offline-first Android application designed to simplify product and inventory management for **Shiv Traders**. Built with Kotlin and Jetpack Compose, the app provides a clean interface for managing products, monitoring stock levels, recording inventory movements, and tracking inventory value.

## Overview

Shiv Traders is an internal inventory management solution designed for business staff. It replaces manual inventory tracking with a centralized digital system that keeps product information and stock movement records organized.

The application follows an offline-first approach, storing data locally using Room Database so that inventory operations can be performed without an internet connection.

## Features

### Dashboard
- Overview of total products and available stock.
- Inventory valuation based on purchase prices.
- Low-stock and out-of-stock indicators.
- Recent inventory activity.
- Quick access to important operations.

### Product Management
- Add new products with essential details.
- Edit existing product information.
- Assign products to categories.
- Store product codes and HSN codes.
- Add purchase prices and selling prices.
- Set minimum stock thresholds.
- Attach optional product images.
- Archive products without deleting their movement history.

### Inventory Management
- **Stock In:** Record incoming stock.
- **Stock Out:** Record outgoing stock.
- **Stock Adjustment:** Update stock according to the actual physical count.
- Prevent stock from becoming negative.
- Maintain a record of every inventory movement.

### Activity History
- View previous stock transactions.
- Track stock additions, removals, and adjustments.
- View previous and updated stock quantities.
- Record reasons for inventory changes.

### Categories
- Organize products into categories.
- Manage category information.
- Make product discovery easier.

### Data Export
- Export product information to CSV.
- Export inventory movement records.
- Save exported files to a selected location.

### Offline-First Storage
- Store application data locally using Room Database.
- Access inventory information without internet connectivity.
- Preserve inventory movement history.
- Update product stock and movement records together using database transactions.

## UI & Design

The application follows a clean, premium design approach with a Burgundy-inspired visual identity.

- Burgundy primary color palette.
- Warm ivory backgrounds and white surfaces.
- Minimal, readable interface.
- Material 3 components.
- Smooth navigation and bottom-sheet interactions.
- Clear visual indicators for stock status.

## Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Jetpack Compose | Declarative UI development |
| Material 3 | UI components and theming |
| Room Database | Local database and persistence |
| SQLite | Local data storage |
| Kotlin Coroutines | Asynchronous operations |
| Kotlin Flow | Reactive data observation |
| MVVM | Application architecture |
| KSP | Room annotation processing |
| Coil | Product image loading |
| Android Activity Result APIs | Image selection and file export |

## Architecture

The application follows the MVVM architecture pattern to separate UI components, business logic, and data persistence.

```text
com.shivtraders.inventory
│
├── MainActivity.kt
│
├── data/
│   ├── Models.kt
│   ├── InventoryDao.kt
│   └── InventoryDatabase.kt
│
├── ui/
│   ├── Theme.kt
│   └── InventoryApp.kt
│
└── utils/
    └── CsvExporter.kt
```

### Architecture Responsibilities

- **UI Layer:** Displays screens and handles user interactions using Jetpack Compose.
- **Data Layer:** Manages database entities, queries, and persistence through Room.
- **Database Layer:** Stores products, categories, and stock movement records.
- **Utility Layer:** Handles supporting operations such as CSV export.

## Database Design

The application uses three primary database entities.

### 1. Product

Stores product information, including:

- Product ID
- Product code
- Product name
- Category ID
- HSN code
- Purchase price
- Selling price
- Current stock
- Minimum stock
- Product image URI
- Description
- Active/inactive status
- Creation timestamp

### 2. Category

Stores product category information:

- Category ID
- Category name
- Description

### 3. StockMovement

Maintains an inventory transaction history:

- Movement ID
- Product ID
- Product name
- Movement type
- Quantity
- Previous stock
- New stock
- Reason
- Timestamp

### Stock Calculation Logic

| Operation | Stock Calculation |
|---|---|
| Stock In | Previous Stock + Quantity |
| Stock Out | Previous Stock - Quantity |
| Adjustment | Set stock to actual physical quantity |

All inventory quantities are measured in **boxes**.

Inventory valuation is calculated using:

```text
Inventory Valuation =
Sum of (Current Stock × Purchase Price)
```

Stock updates and their corresponding movement records are handled together using database transactions to maintain data consistency.

## Getting Started

### Prerequisites

Before running the application, make sure you have:

- Android Studio installed.
- Android SDK configured.
- Kotlin and Jetpack Compose support.
- An Android emulator or physical Android device.

### Installation

**1. Clone the repository**

```bash
git clone https://github.com/YOUR_USERNAME/shiv-traders.git
```

**2. Open the project**

Open the cloned project directory in Android Studio.

**3. Sync Gradle**

Allow Android Studio to download the required dependencies and complete Gradle synchronization.

**4. Run the application**

Connect an Android device or start an emulator, then click the Run button in Android Studio.

## Configuration

The project uses Gradle Kotlin DSL for dependency and build configuration.

Before building, ensure that the Android SDK version and dependency versions configured in the project are supported by your installed Android Studio environment.

## Future Enhancements

Potential improvements include:

- Cloud backup and synchronization.
- Multi-user authentication and staff roles.
- Supplier management.
- Purchase and sales records.
- Invoice generation.
- Barcode and QR code scanning.
- Advanced inventory analytics.
- Automated low-stock notifications.
- PDF report generation.

## Project Goals

The primary goals of Shiv Traders are to:

1. Simplify day-to-day inventory operations.
2. Reduce manual stock calculation errors.
3. Maintain accurate inventory movement records.
4. Make product information easier to access.
5. Provide a reliable offline inventory management solution.

## Contributing

This project is primarily developed for the internal inventory management requirements of Shiv Traders.

Suggestions, bug reports, and improvements are welcome.

## License

This project is currently intended for internal business use. No open-source license has been specified.

---

**Built with Kotlin, Jetpack Compose, and Room Database.**

*Shiv Traders – Smarter Inventory Management.*
