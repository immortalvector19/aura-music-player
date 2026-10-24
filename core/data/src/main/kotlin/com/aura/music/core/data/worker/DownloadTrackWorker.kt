package com.aura.music.core.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadTrackWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val trackId = inputData.getLong(KEY_TRACK_ID, -1L)
        val downloadUrl = inputData.getString(KEY_DOWNLOAD_URL) ?: return@withContext Result.failure()
        val destinationPath = inputData.getString(KEY_DESTINATION_PATH) ?: return@withContext Result.failure()

        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(downloadUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.retry()
            }

            val body = response.body ?: return@withContext Result.failure()
            val destFile = File(destinationPath)
            destFile.parentFile?.mkdirs()

            FileOutputStream(destFile).use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }

            Result.success(
                workDataOf(
                    KEY_TRACK_ID to trackId,
                    KEY_OUTPUT_PATH to destFile.absolutePath
                )
            )
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_TRACK_ID = "track_id"
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_DESTINATION_PATH = "destination_path"
        const val KEY_OUTPUT_PATH = "output_path"

        fun scheduleDownload(
            context: Context,
            trackId: Long,
            url: String,
            destinationFile: File,
            wifiOnly: Boolean
        ) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
                )
                .setRequiresStorageNotLow(true)
                .build()

            val request = OneTimeWorkRequestBuilder<DownloadTrackWorker>()
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        KEY_TRACK_ID to trackId,
                        KEY_DOWNLOAD_URL to url,
                        KEY_DESTINATION_PATH to destinationFile.absolutePath
                    )
                )
                .addTag("track_download_$trackId")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
