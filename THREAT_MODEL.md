# QBIT Threat Model

**Version**: 1.1  
**Date**: 2026-02-10

## 1. Scope
QBIT is a high-security, stealth-focused messaging application for Android. 
This document defines the attacker capabilities we defend against and the limitations of our current protection.

## 2. Attacker Profile
We assume an adversary with:
*   **Physical Access**: Temporary access to the unlocked device (e.g., at a border crossing or checkpoint).
*   **Coercion**: Ability to demand passwords/PINs (Rubber-hose cryptanalysis).
*   **Network Surveillance**: Ability to monitor metadata and traffic flow (ISP/State level).

## 3. Defense Mechanisms

### 3.1 Stealth / Plausible Deniability
*   **Threat**: Adversary sees a chat app and demands the password.
*   **Defense**: 
    *   **Calendar Cover**: The app appears and functions as a working calendar with dynamic month navigation and today highlighting.
    *   **Decoy PIN**: Entering the decoy code shows an "Event saved" toast and stays in calendar mode. The adversary sees normal calendar behavior.
    *   **Launcher Obfuscation**: No "QBIT" icon; only "Calendar".
    *   **PIN Security**: User-configured PINs hashed with PBKDF2-HMAC-SHA256 (200,000 iterations). No default or hardcoded PINs.

### 3.2 Forensics / Data Extraction
*   **Threat**: Adversary images the device storage.
*   **Defense**:
    *   **Full Disk Encryption (Android)**: Relies on OS-level encryption.
    *   **Database Encryption**: SQLCipher with keys stored in Android Keystore.
    *   **Erase Session**: Deletes contact/conversation data via API (NOTE: attachment cleanup and crypto-shredding are planned but not yet implemented — see Limitations).

### 3.3 App Switcher Leaks
*   **Threat**: "Recent Apps" screenshot reveals chat content.
*   **Defense**: 
    *   `FLAG_SECURE` prevents screenshots.
    *   `onPause`/`onStop` hooks instantly swap the UI back to the Calendar Cover.

## 4. Residual Risks (Limitations)

> **IMPORTANT**: Users in high-risk environments must understand these limitations.

*   **Memory Analysis**: If the device is seized while the app is *running and unlocked* in RAM, keys may be extracted.
*   **Custom Keyboards**: Third-party keyboards (Gboard, SwiftKey) may log keystrokes. *Recommendation: Use OpenBoard.*
*   **Rooted Devices**: Malware with root access can bypass all app-level protections.
*   **Erase Session Gaps**: Current erase only removes contact records. Attachments on disk, thumbnail cache, and search index are NOT yet purged. Full crypto-shredding is a roadmap item.
*   **Notification Content**: By default, notifications may show sender names. Users should set notification preview mode to HIDDEN in settings.
*   **Process Name**: `com.qbit.chat` is visible to apps with `QUERY_ALL_PACKAGES` permission. The "chat" suffix may raise suspicion.

## 5. Metadata
QBIT uses the SimpleX routing protocol (SMP) which is designed to minimize metadata. However, traffic analysis (packet timing/size) can still theoretically correlate sender and receiver if both ends are monitored by the same global adversary.
