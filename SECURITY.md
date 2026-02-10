# Security Policy

## Reporting Vulnerabilities
If you find a security issue in QBIT, please do **NOT** open a public issue.
Contact us privately via Session or Signal (IDs to be announced).

## Security Architecture

### 1. Encryption
*   **Transport**: TLS 1.3 for all connections.
*   **End-to-End**: Double Ratchet Algorithm (Signal Protocol variant) for messages.
*   **At Rest**: SQLCipher (AES-256-GCM) for local database.

### 2. Stealth
*   The application mimics a standard productivity tool (Calendar) to avoid raising suspicion.
*   No "Chat" or "Messenger" strings are visible in the Android Launcher.

### 3. Dependencies
We strive to minimize external dependencies.
*   **No Google Play Services**: QBIT runs fully isolated without GMS.
*   **No Firebase**: Push notifications are handled via periodic polling or background services.

## Verification
All releases are signed with our offline GPG key.
Verify the SHA-256 checksum of the APK before installation.
