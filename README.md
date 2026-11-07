# Vikku Daily Schedules

A native Android starter app for personal habits and daily planning, styled in dark purple neon.

## Included in this MVP
- Today dashboard with completion percentage
- Habit list with check-off tracking
- Daily task list with time and Low/Medium/High priority
- Optional local notification scheduled for a task time
- SQLite database stored in the app's private local storage
- No account, cloud service, analytics SDK, or `INTERNET` permission

## Build an APK
1. Install Android Studio (current stable) on a computer.
2. Open this folder in Android Studio.
3. Allow Gradle to sync. The first build needs internet to download Android build dependencies; the installed app itself does not need internet.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Find the APK under `app/build/outputs/apk/debug/app-debug.apk`.
6. Transfer the APK to your Android phone and install it. Android may ask you to allow installation from that file manager.

## Technical details
- Kotlin + Jetpack Compose
- SQLiteOpenHelper (database file: `vikku_daily.db`)
- AlarmManager + BroadcastReceiver for local reminders
- Minimum Android version: Android 8.0 (API 26)
- Target SDK: 35
- The app manifest intentionally does not declare `android.permission.INTERNET`.

## Important MVP notes
- Data is local to this app installation. Clearing app data or uninstalling the app deletes its database. Export/import backup is not included in this first MVP.
- The first launch asks for notification permission on Android 13+.
- Reminders use Android's inexact idle-compatible alarm API; battery restrictions and device settings can affect delivery time.
- The included boot receiver is a placeholder; reminders are not yet automatically restored after a device reboot.
- The current MVP shows today's habits and tasks. A full calendar/history, charts, recurring habits, and backup/restore are suitable next additions.
