# QBIT Threat Model

**Version**: 1.0 (MVP)
**Date**: 2026-02-09

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
    *   **Calendar Cover**: The app appears and functions as a boring calendar.
    *   **Decoy PIN**: Entering `0000` (or user set decoy) unlocks a "normal" calendar state with fake events, hiding the existence of the hidden volume.
    *   **Launcher Obfuscation**: No "QBIT" icon; only "Calendar".

### 3.2 Forensics / Data Extraction
*   **Threat**: Adversary images the device storage.
*   **Defense**:
    *   **Full Disk Encryption (Android)**: Relies on OS-level encryption.
    *   **Database Encryption**: SQLCipher with keys stored in Android Keystore.
    *   **Erase Session**: Per-chat key deletion (crypto-shredding) renders data unrecoverable even if the DB is extracted later.

### 3.3 App Switcher Leaks
*   **Threat**: "Recent Apps" screenshot reveals chat content.
*   **Defense**: 
    *   `FLAG_SECURE` prevents screenshots.
    *   `onPause`/`onStop` hooks instantly swap the UI back to the Calendar Cover.

## 4. Residual Risks (Limitations)
*   **Memory Analysis**: If the device is seized while the app is *running and unlocked* in RAM, keys may be extracted.
*   **Custom Keyboards**: Third-party keyboards (Gboard, SwiftKey) may log keystrokes. *Recommendation: Use OpenBoard.*
*   **Rooted Devices**: Malware with root access can bypass all app-level protections.

## 5. Metadata
QBIT uses the SimpleX routing protocol (SMP) which is designed to minimize metadata. However, traffic analysis (packet timing/size) can still theoretically correlate sender and receiver if both ends are monitored by the same global adversary.
