# Pokke

Native Android music player built with **Jetpack Compose**.

## Features (UI)

- **Home** – Continue listening, Made for you, Recent
- **Full Player** – Blurred album art background, dynamic feel, progress, volume, device name
- **Lyrics** – Highlighted current line with scale animation
- **Queue (Cola)** – Now playing + autoplay + drag list
- **Audio Output sheet**
- **Overflow menu** (Mejorar calidad, Convertir a video, Radio, etc.)
- **Share Lyrics sheet** (preview card + Convertir en historia / Guardar / Compartir)

## Tech

- Kotlin + Jetpack Compose + Material 3
- Navigation Compose
- Coil (images)
- Palette (ready for dynamic color extraction)
- Media3 (ready for playback)

## Build

Open in Android Studio or use:

```bash
./gradlew assembleDebug
```

APK will be in `app/build/outputs/apk/debug/`.

For GitHub Actions APK generation from phone, you can push this repo and use a standard Android CI workflow.

## Notes

- No emojis in the UI.
- App icon placeholder – replace `ic_launcher` with the vinyl icon you provide.
- Mock data uses Laufey tracks to match the reference screenshots.
- Color extraction from album art is prepared (Palette dependency included). Full implementation can be added next.
