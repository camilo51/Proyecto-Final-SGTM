# Repository Guidelines

## Project Objective & Technology

ENGINES JDS is an Android application for consulting and administering motorcycle workshop information. It uses Kotlin, Jetpack Compose, Material 3, ViewModel, StateFlow, Retrofit, and a MySQL-backed REST API.

## Project Structure & Module Organization

This repository contains one Android module, `app`. Kotlin code is under `app/src/main/java/com/example/myapplication/`:

- `data/` contains models, Retrofit services, and API repositories.
- `ui/screens/` contains Compose screens; `ui/viewmodel/` contains screen state and logic; `ui/theme/` contains theme definitions.
- `app/src/main/res/` contains resources and launcher assets.
- `app/src/test/` contains JVM unit tests; `app/src/androidTest/` contains device/instrumented tests.

Keep new code in the matching package and resources under the appropriate `res` directory. Dependency versions are in `gradle/libs.versions.toml`.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper from the repository root:

```bash
./gradlew assembleDebug             # Build the debug APK
./gradlew test                      # Run JVM unit tests
./gradlew lint                      # Run Android lint checks
./gradlew connectedDebugAndroidTest # Run instrumented tests on a device/emulator
```

After changes, run at least `./gradlew assembleDebug`; run tests and lint when applicable. Use Android Studio for emulator/device runs. The project targets SDK 36, supports API 24+, and requires Java/Kotlin toolchain 21.

## Coding Style & Naming Conventions

Use four-space Kotlin indentation and Android Studio’s formatter. Use `PascalCase` for classes/composables, `camelCase` for functions/properties, and suffixes such as `Screen`, `ViewModel`, `Repository`, and `Service`. Keep code simple and easy to explain.

Keep business logic out of composables: use ViewModel and StateFlow for screen state, and Repository classes for API calls. Review existing structure first, and check current dependencies before modifying Gradle files.

## Testing Guidelines

JVM tests use JUnit 4; Android tests use AndroidX JUnit, Espresso, and Compose UI test dependencies. Name tests after the subject, such as `LoginViewModelTest`, with behavior-focused methods such as `loginWithInvalidCredentialsShowsError`. Test affected repository, ViewModel, and screen behavior.

## Commit & Pull Request Guidelines

History uses short imperative messages in English or Spanish, sometimes with Conventional Commit prefixes such as `chore:`. Keep commits focused, e.g. `feat: validate login response`.

PRs should explain the change, list testing performed, link the issue/task, and include screenshots or a recording for UI changes. Call out API/configuration changes. Never commit `local.properties`, credentials, generated output, or ignored local files.

## Security & Configuration

Do not modify `.env` files or commit passwords, tokens, or other secrets. Keep machine-specific configuration local.
