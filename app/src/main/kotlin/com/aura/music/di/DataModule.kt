package com.aura.music.di

import android.content.Context
import com.aura.music.core.data.repository.DefaultTrackRepository
import com.aura.music.core.data.repository.PreferencesRepository
import com.aura.music.core.data.repository.TrackRepository
import com.aura.music.core.data.scanner.MediaStoreScanner
import com.aura.music.core.data.storage.StorageManager
import com.aura.music.core.database.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providePreferencesRepository(@ApplicationContext context: Context): PreferencesRepository {
        return PreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideStorageManager(@ApplicationContext context: Context): StorageManager {
        return StorageManager(context)
    }

    @Provides
    @Singleton
    fun provideMediaStoreScanner(
        @ApplicationContext context: Context,
        trackDao: TrackDao
    ): MediaStoreScanner {
        return MediaStoreScanner(context, trackDao)
    }

    @Provides
    @Singleton
    fun provideTrackRepository(
        @ApplicationContext context: Context,
        trackDao: TrackDao,
        mediaStoreScanner: MediaStoreScanner,
        storageManager: StorageManager,
        preferencesRepository: PreferencesRepository
    ): TrackRepository {
        return DefaultTrackRepository(context, trackDao, mediaStoreScanner, storageManager, preferencesRepository)
    }
}
