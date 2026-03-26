# Atlas Monitor

Android app built with Jetpack Compose for tracking subscription renewals, OQD oil pricing, and US National Debt snapshots.

## Current Release

- Version: `v1.2.2`
- Release page: [GitHub Releases](https://github.com/goldshoot0720/jetpackcomposetrae20260119/releases/tag/v1.2.2)
- APK: `app/build/outputs/apk/release/app-release.apk`

## Features

- Dashboard-style home layout with subscription and market monitoring tabs
- Subscription overview with upcoming renewal reminders
- OQD Daily Marker Price monitoring with local history and trend chart
- US Debt dashboard with local history chart sourced from US Debt Clock
- Manual refresh for latest oil pricing
- Background scheduling for subscription checks and oil price updates

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- WorkManager
- Appwrite

## Project Status

- GitHub `main` is aligned with `v1.2.1` and ready for `v1.2.2`
- Signed release APK is published through GitHub Release
- Release validation notes: `docs/release-validation-v1.2.1.md`

## Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```
