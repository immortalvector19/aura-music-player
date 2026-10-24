package com.aura.music.core.data.cache

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
class Media3CacheManager(
    private val context: Context,
    storageDirectory: File? = null
) {
    private val cacheDirectory = File(
        storageDirectory ?: context.cacheDir,
        "aura_media3_cache"
    )

    // 256MB LRU cache evictor specifically chosen for 2-3 GB RAM and low-storage budget devices
    private val maxCacheBytes = 256L * 1024L * 1024L
    private val databaseProvider = StandaloneDatabaseProvider(context)
    private val evictor = LeastRecentlyUsedCacheEvictor(maxCacheBytes)

    val simpleCache: SimpleCache by lazy {
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
        }
        SimpleCache(cacheDirectory, evictor, databaseProvider)
    }

    fun buildCacheDataSourceFactory(): DataSource.Factory {
        val upstreamFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(8000)
            .setReadTimeoutMs(8000)

        val defaultDataSourceFactory = DefaultDataSource.Factory(context, upstreamFactory)

        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun release() {
        try {
            simpleCache.release()
        } catch (e: Exception) {
            // Cache already released or inaccessible
        }
    }
}
