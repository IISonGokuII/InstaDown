package com.instadown.download.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.instadown.data.model.DownloadStatus
import com.instadown.data.repository.DownloadRepository
import com.instadown.download.DownloadEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collectLatest

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val downloadRepository: DownloadRepository,
    private val downloadEngine: DownloadEngine
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_URL = "url"
        const val KEY_FILE_NAME = "file_name"
        const val KEY_USERNAME = "username"
        const val KEY_PROGRESS = "progress"
        const val KEY_SPEED = "speed"
    }

    override suspend fun doWork(): Result {
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return Result.failure()
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        val username = inputData.getString(KEY_USERNAME)

        return try {
            downloadRepository.updateStatus(downloadId, DownloadStatus.DOWNLOADING)

            downloadEngine.downloadFile(
                id = downloadId,
                url = url,
                fileName = fileName,
                username = username
            ).collectLatest { progress ->
                val progressData = workDataOf(
                    KEY_PROGRESS to progress.progress,
                    KEY_SPEED to progress.speed
                )
                setProgress(progressData)
            }

            Result.success()
        } catch (e: Exception) {
            downloadRepository.markFailed(downloadId, e.message ?: "Unknown error")
            Result.failure(workDataOf("error" to e.message))
        }
    }
}
