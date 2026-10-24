# Tracer-Bullet Engineering Tickets & Milestones

### Milestone 1: Multi-Module Gradle Foundation & Base Domain Models
- **[TICKET-101] Root Gradle & Version Catalog Setup**
  - Configure root and module Gradle scripts with `libs.versions.toml`.
  - Enforce Java 17, minSdk 26, compileSdk/targetSdk 34.
  - Setup Kotlin Coroutines, test runners, and Hilt.
  - *Blocked by: None*
- **[TICKET-102] Core Model Domain Entities**
  - Implement pure Kotlin domain models in `:core:model`: `Track`, `Album`, `Artist`, `Playlist`, `Queue`, `EqualizerConfig`, `StorageInfo`.
  - Add domain unit tests validating invariants and serialization.
  - *Blocked by: [TICKET-101]*

### Milestone 2: Data Layer, MediaStore Scanner & Offline Storage
- **[TICKET-201] Room Database & DAOs**
  - Implement SQLite Room Database in `:core:database` with indexed queries and full-text search (FTS) for 10,000+ tracks.
  - DAOs for Track, Playlist, and PlaybackHistory.
  - Room DAO unit tests with in-memory database.
  - *Blocked by: [TICKET-102]*
- **[TICKET-202] High-Performance MediaStore Scanner & Storage Manager**
  - Implement `MediaStoreScanner` in `:core:data` querying audio files, album art URIs, durations, genres, and SD cards.
  - Implement `StorageManager` detecting internal vs external SD cards and low-space thresholds (<500MB).
  - Implement `DataStore` preferences (data saver, Wi-Fi only downloads, per-app language).
  - Scanner unit tests verifying query projections and URI resolution.
  - *Blocked by: [TICKET-201]*
- **[TICKET-203] Remote Audio Feed, Media3 SimpleCache & WorkManager Sync**
  - Implement Retrofit service + OkHttp client for online audio catalog.
  - Implement WorkManager `DownloadTrackWorker` with network constraint checks (metered vs unmetered Wi-Fi).
  - Setup Media3 `SimpleCache` for caching streamed/downloaded audio.
  - *Blocked by: [TICKET-202]*

### Milestone 3: Playback Engine, MediaSessionService & Audio Effects
- **[TICKET-301] Media3 ExoPlayer & MediaSessionService Foreground Architecture**
  - Implement `AuraMediaService` extending `MediaSessionService` with `FOREGROUND_SERVICE_MEDIA_PLAYBACK`.
  - Rich notification with custom media action buttons and album art bitmap.
  - Lock screen transport controls and playback state synchronization.
  - *Blocked by: [TICKET-102]*
- **[TICKET-302] Audio Focus Arbitration & Noise Handling**
  - Implement `AudioFocusController` handling `AUDIOFOCUS_GAIN`, `AUDIOFOCUS_LOSS`, `AUDIOFOCUS_LOSS_TRANSIENT`, and `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK`.
  - Implement `AudioBecomingNoisyReceiver` pausing playback on headphone unplug or Bluetooth A2DP disconnect.
  - Phone call resumption rules and per-app audio ducking logic.
  - TDD tests verifying state transitions on audio focus events.
  - *Blocked by: [TICKET-301]*
- **[TICKET-303] Equalizer & Audio Effects Engine**
  - Implement `EqualizerEngine` wrapping Android `AudioEffect` / `Equalizer`, `BassBoost`, and `Virtualizer`.
  - Gapless playback configuration and crossfade transition logic.
  - Unit tests for frequency band calculations and preset mapping.
  - *Blocked by: [TICKET-301]*

### Milestone 4: Material 3 UI/UX, Dynamic Theming & Navigation
- **[TICKET-401] Design System & Dynamic Palette Theming**
  - Implement M3 theme in `:core:designsystem` with low-RAM optimized dark/light schemes.
  - Implement `DynamicPaletteExtractor` extracting dominant colors from album art via Palette API.
  - Strict Edge-to-Edge window insets handling (status bar, navigation bar, cutouts).
  - *Blocked by: [TICKET-101]*
- **[TICKET-402] Shared UI Components & Mini Player**
  - Implement reusable UI in `:core:ui`: `TrackListItem`, `MiniPlayerBar`, `WaveformVisualizer`, `PermissionRationaleDialog`.
  - Compose preview and UI tests.
  - *Blocked by: [TICKET-401], [TICKET-102]*
- **[TICKET-403] Library Screens & Folder Hierarchy**
  - Implement `:feature:library`: Tracks tab with fast scroll indexer, Albums grid, Artists list, Playlists manager, and Storage folder browser.
  - Compose UI tests verifying search and list rendering.
  - *Blocked by: [TICKET-402], [TICKET-202]*
- **[TICKET-404] Expanded Player Sheet & Queue Sheet**
  - Implement `:feature:player`: Modern full-screen player with album art carousel, scrub bar, playback controls, repeat/shuffle toggles, and sliding queue sheet.
  - *Blocked by: [TICKET-402], [TICKET-301]*
- **[TICKET-405] Equalizer & Settings Screens**
  - Implement `:feature:equalizer`: 5-band slider controls, bass boost dial, virtualizer dial, and preset chips.
  - Implement `:feature:settings`: Audio output router, Data Saver toggle, Wi-Fi only toggle, SD card storage selector, per-app language selector.
  - *Blocked by: [TICKET-402], [TICKET-303]*

### Milestone 5: System Features (Glance Widgets, Quick Settings, Auto)
- **[TICKET-501] Glance Home Screen Widget & Quick Settings Tile**
  - Implement `AuraGlanceWidget` in `:feature:widget` showing current track, album art, play/pause, and skip buttons.
  - Implement `PlaybackTileService` in `:feature:widget` for the Android Quick Settings drop-down.
  - *Blocked by: [TICKET-301]*
- **[TICKET-502] Android Auto MediaLibraryService**
  - Expose root media hierarchy and browsable tree for Android Auto head units via Media3 `MediaLibraryService`.
  - *Blocked by: [TICKET-301], [TICKET-202]*
- **[TICKET-503] Runtime Permission Flows (Android 8 to 15)**
  - Implement robust permission handlers for `POST_NOTIFICATIONS`, `READ_MEDIA_AUDIO` (Android 13+), `READ_EXTERNAL_STORAGE` (Android <13), and battery optimization exemptions.
  - *Blocked by: [TICKET-402]*

### Milestone 6: Application Assembly, Testing, Benchmarks & Release
- **[TICKET-601] App Module Assembly & Hilt DI Wiring**
  - Connect all core and feature modules in `:app`.
  - Setup Navigation Host with smooth shared element transitions.
  - *Blocked by: [TICKET-403], [TICKET-404], [TICKET-405], [TICKET-501], [TICKET-502], [TICKET-503]*
- **[TICKET-602] Verification Suite & Automated Tests**
  - Run full JUnit, MockK unit test suite.
  - Run integration tests for audio focus loss, noise events, and edge-to-edge Compose tests.
  - *Blocked by: [TICKET-601]*
- **[TICKET-603] Performance, ProGuard/R8 Rules & Release Build**
  - Configure R8 shrinking, ProGuard rules for Media3 and Room.
  - Build universal debug APK and signed release AAB.
  - *Blocked by: [TICKET-602]*
