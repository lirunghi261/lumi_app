# Mochi Dashboard

A cozy, customizable Android personal dashboard built with Kotlin and Jetpack Compose.

## Current MVP

- Balanced two-column dashboard grid with one fixed widget size
- Fixed full-width Daily Overview widget for larger content
- Long-press drag reordering and edit mode
- Remove controls without per-widget resizing
- Clock, Weather, Todo, Quick Note, Focus, Habits, and Countdown widgets
- Personal, Study, and Dream spaces
- Cozy Village, Cosmic Voyage, Sakura Days, and Minimal Pixel themes
- Widget library and basic Theme Studio
- Offline JSON persistence through SharedPreferences
- Original Lumi pixel mascot
- Sharp hero artwork that transitions into a blurred, tinted widget backdrop
- Pixelify Sans display font (SIL Open Font License)

## Build

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Next product milestones

- Free-position collision-aware grid editor
- Theme mix-and-match controls and imported wallpapers
- Room-backed widget data and migrations
- Calendar and weather integrations
- More original theme and mascot packs
