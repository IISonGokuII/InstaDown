package com.instadown.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.instadown.data.local.InstaDownDatabase
import com.instadown.data.local.entity.DownloadEntity
import com.instadown.data.model.DownloadItem
import com.instadown.data.model.DownloadStatus
import com.instadown.data.model.MediaType
import com.instadown.data.model.Quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val database: InstaDownDatabase,
    private val context: Context
) {
    private val downloadDao = database.downloadDao()

    fun getAllDownloads(): Flow<List<DownloadItem>> {
        return downloadDao.getAllDownloads().map { entities ->
            entities.map { it.toDownloadItem() }
        }
    }

    fun getDownloadsPaging(): Flow<PagingData<DownloadEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { downloadDao.getDownloadsPaging() }
        ).flow
    }

    fun getActiveDownloads(): Flow<List<DownloadItem>> {
        return downloadDao.getActiveDownloads().map { entities ->
            entities.map { it.toDownloadItem() }
        }
    }

    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>> {
        return downloadDao.getDownloadsByStatus(status).map { entities ->
            entities.map { it.toDownloadItem() }
        }
    }

    suspend fun addDownload(
        url: String,
        mediaType: MediaType,
        username: String? = null,
        caption: String? = null,
        thumbnailUrl: String? = null,
        quality: Quality = Quality.HD
    ): String {
        val id = UUID.randomUUID().toString()
        val entity = DownloadEntity(
            id = id,
            url = url,
            mediaType = mediaType,
            username = username,
            caption = caption,
            thumbnailUrl = thumbnailUrl,
            quality = quality
        )
        downloadDao.insertDownload(entity)
        return id
    }

    suspend fun updateProgress(id: String, progress: Int, downloadedBytes: Long) {
        downloadDao.updateProgress(id, progress, downloadedBytes)
    }

    suspend fun updateStatus(id: String, status: DownloadStatus) {
        downloadDao.updateStatus(id, status)
    }

    suspend fun markCompleted(id: String, filePath: String) {
        downloadDao.markCompleted(
            id = id,
            status = DownloadStatus.COMPLETED,
            filePath = filePath,
            completedAt = System.currentTimeMillis()
        )
    }

    suspend fun markFailed(id: String, errorMessage: String) {
        downloadDao.markFailed(id, DownloadStatus.FAILED, errorMessage)
    }

    suspend fun deleteDownload(id: String) {
        val entity = downloadDao.getDownloadById(id)
        entity?.filePath?.let { path ->
            File(path).delete()
        }
        downloadDao.deleteDownloadById(id)
    }

    suspend fun clearCompleted() {
        downloadDao.clearCompletedDownloads()
    }

    fun getDownloadStats(): Flow<DownloadStats> {
        return downloadDao.getTotalDownloadedBytes().map { bytes ->
            DownloadStats(
                totalBytes = bytes ?: 0L
            )
        }
    }

    suspend fun getDownloadById(id: String): DownloadItem? {
        return downloadDao.getDownloadById(id)?.toDownloadItem()
    }

    suspend fun setHidden(id: String, isHidden: Boolean) {
        downloadDao.setHidden(id, isHidden)
    }

    private fun DownloadEntity.toDownloadItem(): DownloadItem {
        return DownloadItem(
            id = id,
            url = url,
            mediaType = mediaType,
            status = status,
            progress = progress,
            filePath = filePath,
            fileName = fileName,
            fileSize = fileSize,
            downloadedBytes = downloadedBytes,
            thumbnailUrl = thumbnailUrl,
            username = username,
            caption = caption,
            timestamp = timestamp,
            errorMessage = errorMessage,
            quality = quality
        )
    }

    data class DownloadStats(
        val totalBytes: Long
    )
}
