# Music Player Domain Context

A high-performance, battery-efficient native Android audio player targeting API 26+ to the latest Android releases, engineered for low-memory (2-3 GB RAM) devices as well as modern flagships.

## Language

**Track**:
A single playable audio asset containing metadata (title, artist, album, duration, bitrate, URI, cover art URI).
_Avoid_: Song, audio file, media item

**Queue**:
The in-memory ordered sequence of Tracks staged for sequential, repeat, or shuffled playback in the active session.
_Avoid_: Now playing list, play queue, playlist

**Playlist**:
A persistent, user-defined or system-generated collection of Tracks stored in the local database.
_Avoid_: Mix, album list

**Playback Session**:
The active foreground service (`MediaSessionService`) coordinating ExoPlayer playback, system media notifications, lock screen controls, and external media button events.
_Avoid_: Player controller, audio thread, playback manager

**Audio Focus**:
The Android OS arbitration mechanism that governs which application owns audio output and handles interruptions (calls, navigation alerts, transient sounds).
_Avoid_: Sound priority, audio lock, volume hijack

**Audio Output Route**:
The destination hardware peripheral transmitting audio (Built-in Speaker, Wired 3.5mm/USB-C Headset, Bluetooth A2DP, Android Auto, Auracast).
_Avoid_: Sound device, audio target

**Media Library**:
The indexed repository of Tracks, Albums, Artists, and Genres extracted via MediaStore scanning and remote sync, cached in the local database.
_Avoid_: Audio storage, track collection

**Offline Cache**:
A dedicated, storage-aware cache (internal storage or removable SD card) that stores streamed or pre-downloaded audio chunks using Media3 Cache.
_Avoid_: Temp buffer, download folder
