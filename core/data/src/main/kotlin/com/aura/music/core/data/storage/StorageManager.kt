package com.aura.music.core.data.storage

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.core.content.ContextCompat
import com.aura.music.core.model.StorageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageManager(private val context: Context) {

    suspend fun getAvailableStorageLocations(): List<StorageInfo> = withContext(Dispatchers.IO) {
        val locations = mutableListOf<StorageInfo>()

        // 1. Primary Internal / Emulated Shared Storage
        val primaryDir = Environment.getExternalStorageDirectory()
        if (primaryDir != null && primaryDir.exists()) {
            locations.add(createStorageInfo(primaryDir, isExternalSd = false))
        }

        // 2. Secondary Removable MicroSD Cards
        val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
        for (dir in externalDirs) {
            if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                val sdCardRoot = findStorageRoot(dir)
                locations.add(createStorageInfo(sdCardRoot, isExternalSd = true))
            }
        }

        locations
    }

    suspend fun getPreferredStorageDirectory(customPath: String? = null): File = withContext(Dispatchers.IO) {
        if (!customPath.isNullOrBlank()) {
            val customDir = File(customPath)
            if (customDir.exists() && customDir.canWrite()) {
                val appMediaDir = File(customDir, "AuraMusic")
                if (!appMediaDir.exists()) appMediaDir.mkdirs()
                return@withContext appMediaDir
            }
        }
        val defaultDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "AuraMusic")
        if (!defaultDir.exists()) defaultDir.mkdirs()
        defaultDir
    }

    private fun createStorageInfo(file: File, isExternalSd: Boolean): StorageInfo {
        return try {
            val stat = StatFs(file.path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            StorageInfo(
                totalBytes = totalBytes,
                availableBytes = availableBytes,
                isExternalSd = isExternalSd,
                storagePath = file.absolutePath
            )
        } catch (e: Exception) {
            StorageInfo(
                totalBytes = 0L,
                availableBytes = 0L,
                isExternalSd = isExternalSd,
                storagePath = file.absolutePath
            )
        }
    }

    private fun findStorageRoot(file: File): File {
        var current: File? = file
        while (current?.parentFile != null && current.parentFile?.canRead() == true) {
            if (current.parentFile?.name == "Android") {
                return current.parentFile?.parentFile ?: current
            }
            current = current.parentFile
        }
        return current ?: file
    }
}
