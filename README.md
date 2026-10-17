# PantryPal

> An offline-first pantry companion built with Kotlin and Jetpack Compose.

Keep track of what's on the shelf, spot food that's best used soon, and remove items as you cook. Pantry entries are stored locally in a Room database; there is no account or cloud service.

## Features

- Search the pantry by item name and filter by category.
- See total items, items expiring within three days, and expired items.
- Add an item with its category, quantity, and days until expiry.
- Mark an item used to remove it from the list.
- Room persistence keeps the pantry on the device across app restarts.
- Kotlin-only sources under `app/src/main/kotlin`.

## Run

Open this project in Android Studio (JDK 17, Android SDK 35) and run the `app` configuration. From a terminal, run `./gradlew :app:installDebug`. Unit tests: `./gradlew testDebugUnitTest`.

## Structure

```text
app/src/main/kotlin/com/premsingh/pantrypal/
  data/       Room database, DAO and ViewModel
  domain/     Expiry summaries and display rules
  MainActivity.kt  Compose pantry screens and add-item form
```

GitHub Actions runs unit tests and assembles the debug app on pushes and pull requests.

## Scope

PantryPal stores data locally and does not request an account, location, camera, or network access. Expiry dates are planning reminders; use the food label and your own judgment for food safety.

## License

MIT. See [LICENSE](LICENSE).
