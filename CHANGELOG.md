# QBIT Fork Changelog

## Branding
- **Name**: Changed app name to "QBIT".
- **Package**: Changed `applicationId` to `com.qbit.chat`.
- **Theme**: Applied "Dark by Design" color palette (Cyan Accent, Dark Background).

## Stealth Features
- **Cover Screen**: Added `CalendarCoverScreen` that mimics a calendar app with dynamic month navigation.
- **PIN Unlock**: Implemented a keypad in the "Add Event" dialog.
  - User sets their own unlock PIN on first launch (no default PIN).
  - Optional decoy PIN dismisses with "Event saved" toast.
  - PIN hashed with PBKDF2-HMAC-SHA256 (200,000 iterations).
- **App Lock**: UI automatically resets to Calendar Cover when the app goes to background (`onPause`/`onStop`).

## Security Features
- **PIN Setup**: Mandatory first-run PIN configuration flow.
- **Security Settings**: Change unlock/decoy PIN from within unlocked app.
- **Erase Session**: "Delete" button in Chat Toolbar for local conversation wipe.

## Settings
- Added "QBIT Settings" section in the Settings screen with placeholder toggles:
  - Cover Screen
  - Post-Quantum Mode (NOT YET IMPLEMENTED)
  - Tor/I2P Only (NOT YET IMPLEMENTED)
  - Dead-drop Mode (NOT YET IMPLEMENTED)
  - Notifications: Silent
