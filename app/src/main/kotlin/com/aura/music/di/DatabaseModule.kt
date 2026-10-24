package com.aura.music.di

import android.content.Context
import androidx.room.Room
import com.aura.music.core.database.AuraDatabase
import com.aura.music.core.database.PlaylistDao
import com.aura.music.core.database.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuraDatabase {
        return Room.databaseBuilder(
            context,
            AuraDatabase::class.java,
            "aura_music.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTrackDao(database: AuraDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    fun providePlaylistDao(database: AuraDatabase): PlaylistDao {
        return database.playlistDao()
    }
}
