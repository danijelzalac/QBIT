# Next Steps to Build QBIT APK

Since I cannot compile the APK in this cloud environment, **YES**, you need to install **Android Studio** on your computer.

I have already done all the coding work. The project is ready to build.

## 1. Install Android Studio
Download it here: [https://developer.android.com/studio](https://developer.android.com/studio)

## 2. Install Required Components
During installation (or via **Tools > SDK Manager** inside Android Studio), ensure you check these boxes:
*   **Android SDK Platform 35** (API 35)
*   **NDK (Side by side)** (The project needs version `23.1.7779620`, Android Studio will often offer to install it automatically if missing).
*   **CMake** (Required for the C++ crypto libraries).

## 3. Open the Project
1.  Launch **Android Studio**.
2.  Click **Open**.
3.  Navigate to this folder: 
    `c:\Users\danij\Documents\trae_projects\Qbit\qbit`
4.  **Important**: If asked to "Trust Project", select **Trust**.

## 4. Build & Run
1.  Wait for the "Gradle Sync" to finish (bottom right progress bar).
2.  Connect your Android phone via USB (enable Developer Options > USB Debugging).
3.  Click the green **Run (Play)** button in the top toolbar.

The app **QBIT** will be installed on your phone.
*   **Launch Icon**: It will look like a **Calendar**.
*   **First Launch**: You will be prompted to set your own unlock code.
*   **Unlock**: Tap the "+" FAB and enter your code in the "Duration" field.
*   **Decoy**: If configured, entering the decoy code shows "Event saved" and stays in calendar.
