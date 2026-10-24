# Aura Music Player 🎵

> A native Android music player crafted with **Spotify's dark aesthetic**, **Media3 (ExoPlayer)** background audio engine, and adaptive Material icons.

---

## 🌟 Key Features

- **Spotify Dark Aesthetic**: Immersive `#121212` canvas with elevated surfaces (`#181818`), Spotify Green accents (`#1ED760` / `#1DB954`), and typography hierarchy.
- **Media3 Playback Engine**: Seamless ExoPlayer playback backed by `AuraMediaService` foreground service with media session controls, audio focus handling, and notification controls.
- **Embedded Demo Audio**: Bundled with 3 high-quality synthesized 44.1kHz stereo PCM audio tracks (`Acoustic Sunrise`, `Midnight Groove`, `Ambient Pulse`) auto-seeded for immediate out-of-the-box playback.
- **Full-Screen Now Playing**: Spotify-style bottom sheet featuring ambient gradient, 1:1 rounded cover art with drop shadow, scrubber slider with monospace time readouts, and circular play/pause button.
- **10-Band Graphic Equalizer**: Custom parametric AudioEffect equalizer with bass boost, virtualizer, and responsive presets.
- **Modular Architecture**: Built following Clean Architecture principles with Jetpack Compose, Kotlin Coroutines/Flow, Room SQLite database, and Dagger Hilt DI.
- **Low-End Device Optimized**: Memory footprint strictly managed (<100MB heap) to run smoothly on budget devices with 2–3 GB RAM.

---

## 📐 Architecture & Modular Setup

```text
├── app/                  # Application bootstrap, navigation, Hilt dependency injection
├── core/
│   ├── model/            # Immutable domain models (Track, Album, Artist, Equalizer)
│   ├── database/         # Room SQLite database with FTS4 search indexing
│   ├── data/             # Repositories, MediaStore scanner, storage manager, download workers
│   ├── playback/         # ExoPlayer audio engine, PlaybackManager, MediaSessionService
│   ├── designsystem/     # Spotify color tokens, typography, dark theme
│   └── ui/               # Shared Compose UI components (TrackListItem, MiniPlayerBar)
└── feature/
    ├── library/          # Library browsing, category filter chips, search
    ├── player/           # Full-screen Now Playing sheet & PlayerViewModel
    ├── equalizer/        # Graphic equalizer sliders and presets
    ├── settings/         # Storage manager, data saver mode, audio cache controls
    └── widget/           # Android Glance home screen widget & Quick Settings tile
```

---

## 🚀 Building & Packaging

### Prerequisites
- JDK 17
- Android SDK 34 (minSdk 26, targetSdk 34)

### Commands
```bash
# Run unit tests across all modules
./gradlew test

# Build debug APK for quick install
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Build release Android App Bundle (AAB) with R8 optimization
./gradlew :app:bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

---

## 📥 Download & Install

You can download the compiled APK or AAB directly from the [v1.0.0 Release](https://github.com/immortalvector19/aura-music-player/releases/tag/v1.0.0):

- 📲 **[Download Debug APK](https://github.com/immortalvector19/aura-music-player/releases/download/v1.0.0/app-debug.apk)** (29.4 MB — Direct Android Install)
- 📦 **[Download Release AAB](https://github.com/immortalvector19/aura-music-player/releases/download/v1.0.0/app-release.aab)** (9.4 MB — Google Play Bundle)

---

## 🔒 License & Open Source

This repository is **Public**. Contributions, issues, and forks are welcome!
