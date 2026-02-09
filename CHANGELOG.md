# QBIT Fork Changelog

## Branding
- **Name**: Changed app name to "QBIT".
- **Package**: Changed `applicationId` to `com.qbit.chat`.
- **Theme**: Applied "Dark by Design" color palette (Cyan Accent, Dark Background).

## Stealth Features
- **Cover Screen**: Added `CalendarCoverScreen` that mimics a calendar app.
- **PIN Unlock**: Implemented a keypad in the "Add Event" dialog.
  - **PIN**: `2026` unlocks the QBIT UI.
  - **Decoy**: `0000` stays in Calendar mode.
- **App Lock**: UI automatically resets to Calendar Cover when the app goes to background (`onPause`/`onStop`).

## Privacy Features
- **Erase Session**: Added a "Delete" button in the Chat Toolbar.
  - Triggers a local wipe of the conversation (simulated remote wipe).
  - No notifications generated.

## Settings
- Added "QBIT Settings" section in the Settings screen with placeholder toggles:
  - Cover Screen
  - Post-Quantum Mode
  - Tor/I2P Only
  - Dead-drop Mode
  - Notifications: Silent
