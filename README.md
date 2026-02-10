# QBIT
**Privacy-Focused Stealth Messenger**
*(Fork of SimpleX Chat)*

## Overview
QBIT is a high-security messaging application designed for freedom and human rights advocacy. It operates on a metadata-free routing protocol (SMP) and features a stealth "Calendar Cover" mode to protect user privacy in hostile environments.

## Key Features
*   **Stealth Mode**: App launches as a functional Calendar with working month navigation.
*   **PIN Unlock**: Access the chat interface via a user-configured secret PIN (set on first launch).
*   **Decoy Mode**: A separate decoy PIN dismisses the dialog with a convincing "Event saved" response.
*   **Snapshot Protection**: App content is hidden from "Recent Apps" and screenshots via `FLAG_SECURE`.
*   **Erase Session**: Delete conversation data from the device.
*   **Security Settings**: Change unlock and decoy PINs from within the app.

## Security & Privacy
*   **No User IDs**: Uses pairwise identifiers; no global username or phone number.
*   **End-to-End Encryption**: Double-ratchet algorithm for perfect forward secrecy.
*   **Metadata Protection**: Servers do not store user profiles or contact lists.
*   **PIN Security**: PINs are hashed with PBKDF2-HMAC-SHA256 (200,000 iterations) and stored in EncryptedSharedPreferences.

## Build Instructions (Android)
1.  Clone this repository.
2.  Open in **Android Studio** (Ladybug or newer).
3.  Install SDK 35, NDK 23.1.7779620, and CMake via SDK Manager.
4.  Build and Run on an Android device (API 26+).

## Credits
*   **Original Core**: [SimpleX Chat](https://github.com/simplex-chat/simplex-chat)
*   **QBIT Concept & Modifications**: Danijel Zalac 2026

## License
Licensed under AGPLv3. See [LICENSE](./LICENSE) for details.
