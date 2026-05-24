# DompetMax

Offline-first personal finance app for tracking income, expenses, recurring bills, and investment portfolios from an Android device.

## Product Focus

DompetMax is designed as a private, local-first finance companion for people who want one place to monitor daily cash flow, upcoming subscriptions, and portfolio positions such as deposits, crypto, mutual funds, and gold.

## Core Features

- Dashboard overview for personal finance signals
- Income and expense tracking
- Recurring bill and subscription management
- Investment portfolio tracking
- Local Room database persistence
- Light/dark/system theme setting
- Multi-language UI utility layer
- Offline-first Android experience

## Tech Stack

- Kotlin
- Android Jetpack Compose
- Material 3
- Room Database
- DataStore Preferences
- ViewModel + Kotlin Coroutines
- KSP
- Robolectric / Roborazzi test dependencies

## Project Structure

```text
app/src/main/java/com/example/
  data/
    local/        Room database and DAOs
    model/        Transaction, subscription, and investment entities
    repository/   Finance repository layer
  ui/
    screens/      Dashboard, transactions, subscriptions, investments, settings
    theme/        Compose theme tokens
    util/         Translation helpers
    viewmodel/    FinanceViewModel state holder
```

## Run Locally

Open the project in Android Studio, let Gradle sync, then run the app on an emulator or Android device.

No API keys are required for the current offline-first build. Do not commit real keystores, service-account files, API keys, or signing passwords.

## Build Status

Current repository validation:

- Source structure inspected
- Android app module detected
- Compose, Room, DataStore, and testing dependencies detected
- Local build not executed in this environment because the repository does not currently include `gradlew`, `gradlew.bat`, or `gradle-wrapper.jar`, and system Gradle is not installed

Recommended next hardening:

- Add the full Gradle wrapper files
- Run `./gradlew test`
- Run `./gradlew assembleDebug`
- Add screenshots from emulator or Roborazzi outputs
- Rename base package from `com.example` to a production namespace such as `com.robby.dompetmax`
- Replace placeholder privacy contact details before Play Store release

## Privacy

See [PRIVACY_POLICY.md](PRIVACY_POLICY.md).
