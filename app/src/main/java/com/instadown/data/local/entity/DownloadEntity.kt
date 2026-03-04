package com.instadown.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instadown.data.model.DownloadStatus
import com.instadown.data.model.MediaType
import com.instadown.data.model.Quality

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val mediaType: MediaType,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Int = 0,
    val filePath: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0,
    val downloadedBytes: Long = 0,
    val thumbnailUrl: String? = null,
    val username: String? = null,
    val caption: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val quality: Quality = Quality.HD,
    val isHidden: Boolean = false
)
