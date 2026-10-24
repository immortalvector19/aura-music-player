package com.aura.music.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.aura.music.core.playback.AudioPlayer
import com.aura.music.core.playback.EqualizerEngine
import com.aura.music.core.playback.ExoAudioPlayer
import com.aura.music.core.playback.PlaybackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    @Provides
    @Singleton
    fun provideEqualizerEngine(): EqualizerEngine {
        return EqualizerEngine()
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer,
        equalizerEngine: EqualizerEngine
    ): AudioPlayer {
        return ExoAudioPlayer(exoPlayer, context, equalizerEngine)
    }

    @Provides
    @Singleton
    fun providePlaybackManager(
        @ApplicationContext context: Context,
        audioPlayer: AudioPlayer,
        equalizerEngine: EqualizerEngine
    ): PlaybackManager {
        return PlaybackManager(
            context = context,
            player = audioPlayer,
            equalizerEngine = equalizerEngine
        )
    }
}
