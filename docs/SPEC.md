# Product Specification: Aura Music Player

## Problem Statement
Android users on budget hardware (2–3 GB RAM, Android Go, quad/octa-core budget chipsets running Android 8 through 15) frequently suffer from bloated, stuttering, and resource-hungry music player apps that crash when memory pressure spikes, leak battery in the background, stutter on audio output transitions (Bluetooth/headphones), or fail to adhere to modern Android permission models (Android 13/14/15 media and notification permissions).

## Solution
Aura Music is a high-performance native Android audio player built with Kotlin, Jetpack Compose, Material 3, and Jetpack Media3 (ExoPlayer). It features a clean modular architecture, rigorous background session and audio focus management, low-memory footprint (<100MB heap), instant indexing for 10,000+ tracks, dynamic album-art theming, Glance home screen widgets, Quick Settings tile, Android Auto support, equalizer with AudioEffect, and smart storage-aware offline caching.

## User Stories
1. **As a listener**, I want the app to scan my device storage and SD cards rapidly, indexing 10,000+ tracks without freezing the UI.
2. **As a listener**, I want to browse my library by Tracks, Albums, Artists, Playlists, and physical Folder hierarchy.
3. **As a listener**, I want uninterrupted, gapless playback with smooth crossfading between tracks.
4. **As a commuter**, I want playback to pause immediately when my headphones are unplugged or Bluetooth disconnects, and resume intelligently after a phone call.
5. **As a listener**, I want rich notification controls with custom action buttons and lock screen media metadata across Android 8 through 15.
6. **As an audiophile**, I want a 5-band equalizer with Bass Boost, Virtualizer, and custom presets that applies cleanly without audio clipping.
7. **As a driver**, I want seamless Android Auto browsing and playback controls directly from my vehicle head unit.
8. **As a user with limited storage**, I want low-storage warnings, SD card selection, and the ability to download remote tracks only when on Wi-Fi or with Data Saver enabled.
9. **As a multilingual user**, I want per-app language settings and smooth runtime permission flows for audio and notifications.
10. **As a home screen user**, I want an interactive Glance widget and Quick Settings tile to control playback instantly without opening the app.

## Implementation Decisions
- **Modularity**:
  - `:core:model` (Domain entities: Track, Album, Artist, Playlist, Queue, EqualizerBand, StorageInfo)
  - `:core:database` (Room database with FTS4 search and indexed queries)
  - `:core:data` (MediaStoreScanner, repositories, WorkManager download sync, DataStore preferences)
  - `:core:playback` (Jetpack Media3 ExoPlayer, MediaSessionService, AudioFocusController, EqualizerEngine)
  - `:core:designsystem` (Material 3 Expressive theme, Palette API dynamic color, typography, insets)
  - `:core:ui` (Shared composables, track list item, mini player, permission dialog, waveform)
  - `:feature:library` (Library screens: Tracks, Albums, Artists, Playlists, Folders)
  - `:feature:player` (Expanded Player Sheet, Queue sheet, scrubber, playback controls)
  - `:feature:equalizer` (Equalizer interface with sliders and presets)
  - `:feature:settings` (Audio preferences, data saver, SD card selection, language)
  - `:feature:widget` (Glance AppWidget and Quick Settings TileService)
  - `:app` (Root Application, Hilt dependency graph, MainActivity, Navigation, Baseline Profile)
- **Permissions**: Fully compatible with Android 8 (API 26) through Android 15 (API 35), requesting `READ_MEDIA_AUDIO` on API 33+, `READ_EXTERNAL_STORAGE` on API <33, `POST_NOTIFICATIONS` on API 33+, and declaring `FOREGROUND_SERVICE_MEDIA_PLAYBACK` on API 34+.
- **Memory & Battery**: Strictly avoid large in-memory bitmap caches; offload image decoding to Coil with RGB_565 downsampling for low-RAM devices; cancel coroutines proactively; release audio effects when idle.

## Testing Decisions
- Unit tests at public seams for repositories, database DAOs, audio focus arbitration, and equalizer calculations using MockK, Truth, and Turbine.
- Playback service integration tests simulating headphone disconnect (`ACTION_AUDIO_BECOMING_NOISY`), incoming phone call audio focus loss (`AUDIOFOCUS_LOSS_TRANSIENT`), and call completion.
- UI tests for Compose screens verifying interaction states and edge-to-edge padding.

## Out of Scope
- Direct peer-to-peer Wi-Fi Direct file sharing (deferred to future roadmap).
- Cloud user accounts and third-party streaming DRM (Spotify/Apple Music private APIs).
