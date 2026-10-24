package com.aura.music.core.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@OptIn(UnstableApi::class)
class AuraMediaService : MediaSessionService(), Player.Listener {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private lateinit var audioFocusController: AudioFocusController
    private lateinit var audioBecomingNoisyReceiver: AudioBecomingNoisyReceiver
    private val equalizerEngine = EqualizerEngine()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val activeManager = PlaybackManager.instance
        val exoPlayer = (activeManager?.player as? ExoAudioPlayer)?.exoPlayer ?: run {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            ExoPlayer.Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_LOCAL)
                .build()
        }

        exoPlayer.addListener(this)
        player = exoPlayer

        audioFocusController = AudioFocusController(
            context = this,
            onPausePlayback = { activeManager?.pause() ?: exoPlayer.pause() },
            onResumePlayback = { activeManager?.play() ?: exoPlayer.play() },
            onSetVolume = { exoPlayer.volume = it }
        )

        audioBecomingNoisyReceiver = AudioBecomingNoisyReceiver(
            context = this,
            onBecomingNoisy = { activeManager?.pause() ?: exoPlayer.pause() }
        )

        val sessionCallback = object : MediaSession.Callback {
            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: android.os.Bundle
            ): ListenableFuture<SessionResult> {
                when (customCommand.customAction) {
                    COMMAND_TOGGLE_FAVORITE -> {
                        // Custom action handled by service
                    }
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setCallback(sessionCallback)
            .build()

        val eq = activeManager?.equalizerEngine ?: equalizerEngine
        eq.bindToAudioSession(exoPlayer.audioSessionId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val activeManager = PlaybackManager.instance
        when (intent?.action) {
            ACTION_PREVIOUS -> activeManager?.skipPrevious() ?: player?.seekToPreviousMediaItem()
            ACTION_PLAY_PAUSE -> {
                if (player?.isPlaying == true) {
                    activeManager?.pause() ?: player?.pause()
                } else {
                    activeManager?.play() ?: player?.play()
                }
            }
            ACTION_NEXT -> activeManager?.skipNext() ?: player?.seekToNextMediaItem()
            ACTION_STOP -> {
                activeManager?.pause() ?: player?.pause()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
        }
        startForegroundWithNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val isPlaying = player?.isPlaying ?: false
        if (!isPlaying) {
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        startForegroundWithNotification()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        startForegroundWithNotification()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            audioFocusController.onPlaybackStarted()
            audioBecomingNoisyReceiver.register()
            startForegroundWithNotification()
        } else {
            audioFocusController.onPlaybackStopped()
            audioBecomingNoisyReceiver.unregister()
            startForegroundWithNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(false)
            }
        }
    }

    override fun onAudioSessionIdChanged(audioSessionId: Int) {
        val eq = PlaybackManager.instance?.equalizerEngine ?: equalizerEngine
        eq.bindToAudioSession(audioSessionId)
    }

    override fun onDestroy() {
        audioBecomingNoisyReceiver.unregister()
        audioFocusController.abandonFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        if (PlaybackManager.instance == null) {
            equalizerEngine.release()
        }

        mediaSession?.run {
            release()
            mediaSession = null
        }
        player?.removeListener(this)
        if (PlaybackManager.instance == null) {
            player?.release()
        }
        player = null
        super.onDestroy()
    }

    private fun startForegroundWithNotification() {
        createNotificationChannel()
        val track = PlaybackManager.instance?.currentTrack?.value
        val title = track?.title ?: "Aura Music"
        val artist = track?.artist ?: "Playing"
        val isPlaying = player?.isPlaying ?: false

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val prevIntent = Intent(this, AuraMediaService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(
            this, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, AuraMediaService::class.java).apply { action = ACTION_PLAY_PAUSE }
        val playPausePendingIntent = PendingIntent.getService(
            this, 2, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, AuraMediaService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(
            this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseActionTitle = if (isPlaying) "Pause" else "Play"

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(track?.album ?: "Aura")
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseActionTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)

        mediaSession?.let { session ->
            notificationBuilder.setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        }

        if (pendingIntent != null) {
            notificationBuilder.setContentIntent(pendingIntent)
        }

        val notification = notificationBuilder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aura Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media controls for Aura Music Player"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "aura_playback_channel"
        const val NOTIFICATION_ID = 1001
        const val COMMAND_TOGGLE_FAVORITE = "com.aura.music.COMMAND_TOGGLE_FAVORITE"
        const val ACTION_PLAY_PAUSE = "com.aura.music.ACTION_PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.aura.music.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.aura.music.ACTION_NEXT"
        const val ACTION_STOP = "com.aura.music.ACTION_STOP"
    }
}
