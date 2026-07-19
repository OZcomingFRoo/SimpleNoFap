# Release Preparation

This project has a release build type configured for R8 minification and resource shrinking. Release signing is loaded from a local `release-keystore.properties` file when all required values are present.

## App Icon Inputs

The manifest already uses `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`. To replace the default Android icon, provide source artwork that can be imported with Android Studio Image Asset:

- Adaptive icon foreground: transparent PNG, 432 x 432 px, important artwork inside the center 288 x 288 px safe area.
- Adaptive icon background: either one solid hex color or a 432 x 432 px PNG.
- Optional Play Store icon: PNG, 512 x 512 px, no transparency.

After importing, Android Studio should regenerate these resources:

- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/mipmap-mdpi/ic_launcher.webp`
- `app/src/main/res/mipmap-hdpi/ic_launcher.webp`
- `app/src/main/res/mipmap-xhdpi/ic_launcher.webp`
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.webp`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp`
- Matching `ic_launcher_round.webp` files in the same density folders.

## Release Signing

1. Create a local keystore. Example:

   ```powershell
   New-Item -ItemType Directory -Force -Path V:\secrets\SimpleNoFap
   keytool -genkeypair -v -keystore V:\secrets\SimpleNoFap\simplenofap-release.jks -alias simplenofap-release -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Copy `release-keystore.properties.example` to `release-keystore.properties`.
3. Fill in `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`.
   Use forward slashes for the keystore path, for example `V:/secrets/SimpleNoFap/simplenofap-release.jks`.
4. Build the signed release APK:

   ```powershell
   .\gradlew.bat assembleRelease
   ```

If `release-keystore.properties` is missing or incomplete, Gradle can still assemble an unsigned release artifact for local verification.

## Before Public Release

- Decide whether `applicationId = "com.example.simplenofap"` is final. Changing it later creates a different app identity.
- Confirm `versionCode` and `versionName` in `app/build.gradle.kts`.
- Install and smoke-test the release build on a device before distributing it.
