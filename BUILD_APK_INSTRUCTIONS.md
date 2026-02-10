# BUILD INSTRUCTIONS FOR QBIT (ANDROID)

## IMPORTANT NOTICE
This project requires a native Android Build Environment (Android SDK, NDK, JDK) to compile the APK. 
The current cloud environment does not support compiling Android applications directly.

## HOW TO BUILD THE APK

The source code for **QBIT** (the forked SimpleX Chat) is fully configured on your machine at:
`c:\Users\danij\Documents\trae_projects\Qbit\qbit`

### Prerequisites
1.  **Android Studio** (Ladybug or newer recommended).
2.  **JDK 17** (Usually bundled with Android Studio).
3.  **Android SDK 35** (API 35).

### Steps
1.  Open **Android Studio**.
2.  Select **File > Open** and choose the `qbit` folder.
    *   *Note: Point to `qbit/apps/multiplatform/android` if prompted for the Gradle root.*
3.  Wait for the project to sync (it will download dependencies).
4.  Connect an Android device or start an Emulator.
5.  Click the green **Run** (Play) button in the toolbar.
    *   Or go to `Build > Build Bundle(s) / APK(s) > Build APK`.

### Troubleshooting
*   If you see "NDK not found", go to `Tools > SDK Manager > SDK Tools` and install **NDK (Side by side)**.
*   The app ID is `com.qbit.chat`. Ensure you don't have another app with this ID installed (or uninstall it first).

## WHAT IS INCLUDED
This source code includes all the QBIT features implemented:
*   **Stealth Calendar Cover**: App launches as a Calendar with dynamic month navigation.
*   **PIN Unlock**: User sets their own code on first launch. Enter it in "Add Event" to open Chat.
*   **Decoy Code**: Optional secondary code that dismisses dialog with "Event saved" (configurable in settings).
*   **Erase Session**: Red trash icon in chat to wipe conversation locally.
*   **Dark Theme**: Custom QBIT dark/cyan theme.
*   **Security Settings**: Change unlock/decoy codes from within the app.
