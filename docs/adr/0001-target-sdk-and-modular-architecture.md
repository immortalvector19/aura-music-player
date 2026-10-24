# Target SDK Selection and Deep Modular Architecture

## Context
The application is a production-grade native Android music player that must run fluidly on low-end hardware (2–3 GB RAM, Android Go / budget chipsets) while taking full advantage of modern platform APIs (Android 14/15, Jetpack Media3, Compose M3, Glance widgets, and dynamic permissions).

## Decision
1. **Target SDK 34 (Android 14) / Compile SDK 34 (or 35)**:
   - Mandatory for current Play Store compliance.
   - Enforces modern permission architectures: `POST_NOTIFICATIONS`, `READ_MEDIA_AUDIO` (API 33+), and explicit `FOREGROUND_SERVICE_MEDIA_PLAYBACK` (API 34+).
2. **Minimum SDK 26 (Android 8.0 Oreo)**:
   - Reaches ~97% of worldwide active Android devices, covering virtually all low-end/budget devices in emerging markets.
   - Natively provides Notification Channels, eliminating legacy notification fallback paths.
   - Provides deterministic background execution rules (`startForegroundService`), ART JIT/AOT profile compilation (critical for 2–3 GB RAM devices), and native Ogg/FLAC decoding.
   - Drops obsolete API 21–25 devices (<3% global share) which require memory-heavy workarounds and lack modern audio session reliability.
3. **Deep Modular Architecture**:
   - Organized into clear seams: `:core:model`, `:core:database`, `:core:data`, `:core:playback`, `:core:designsystem`, `:core:ui`, `:feature:library`, `:feature:player`, `:feature:equalizer`, `:feature:settings`, `:feature:widget`, and `:app`.
   - Isolates Media3/ExoPlayer foreground service lifecycles from UI composition, preventing audio stutter and memory leaks on low-RAM devices during UI recomposition.
