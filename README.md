# QBIT
**Privacy-Focused Stealth Messenger**
*(Fork of SimpleX Chat)*

## Overview
QBIT is a high-security messaging application designed for freedom and human rights advocacy. It operates on a serverless architecture (metadata-free routing) and features a stealth "Calendar Cover" mode to protect user privacy in hostile environments.

## Key Features
*   **Stealth Mode**: App launches as a functional Calendar.
*   **PIN Unlock**: Access the chat interface only via a secret PIN (`2026` in prototype).
*   **Decoy Mode**: Entering a decoy PIN (`0000`) simulates a normal calendar event.
*   **Snapshot Protection**: App content is hidden from "Recent Apps" and screenshots.
*   **Local Wipe**: "Erase Session" instantly deletes conversation data from the device.

## Security & Privacy
*   **No User IDs**: Uses pairwise identifiers; no global username or phone number.
*   **End-to-End Encryption**: Double-ratchet algorithm for perfect forward secrecy.
*   **Metadata Protection**: Servers do not store user profiles or contact lists.

## Build Instructions (Android)
1.  Clone this repository.
2.  Open in **Android Studio**.
3.  Build and Run on an Android device (API 26+).

## Credits
*   **Original Core**: [SimpleX Chat](https://github.com/simplex-chat/simplex-chat)
*   **QBIT Concept & Modifications**: Danijel Zalac 2026

## License
Licensed under AGPLv3. See [LICENSE](./LICENSE) for details.
