# یاددار — Android Starter (0.1.0)

This is the initial Android-native project scaffold for Yaddar, using Kotlin, Jetpack Compose and Room.

## Requirements
- Android Studio (recent version supporting Android Gradle Plugin 8.9.2)
- JDK 17 or newer
- Android SDK 35

## Build
1. Open this folder in Android Studio.
2. Allow Gradle sync (internet is needed for initial dependency downloads).
3. Run on an Android device or emulator (Android 8.0/API 26 or later).

## Included
- RTL Persian dashboard shell
- Bottom navigation placeholders
- Compose-based UI
- Initial Room entity definitions (products, people, financial events)
- Android project configuration

## Not yet implemented
- Database DAO and repository wiring
- Functional inventory, sales, purchasing, financial ledger, backups and POS integration
- Navigation destinations beyond placeholder screens
- Production tests and generated APK

The current dashboard is a visual scaffold; displayed figures are intentionally placeholders.

## Build APK through GitHub Actions (no Android Studio needed)

1. Create a GitHub repository named `YaddarAndroid` (public or private).
2. Upload the contents of this folder to the repository's `main` branch.
3. Open the repository's **Actions** tab. Select **Build Yaddar Android APK**.
4. If the workflow has not run automatically, press **Run workflow**.
5. Open the completed run and download the `Yaddar-debug-APK` artifact.
6. Extract the artifact ZIP and install `app-debug.apk` on your Android phone. Android may ask you to allow installation from that source.

The APK is a debug/test build, not a Play Store release. This workflow has not been executed in this environment; verify the Actions run succeeds before installing.
