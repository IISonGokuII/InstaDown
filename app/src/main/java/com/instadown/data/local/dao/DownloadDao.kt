package com.instadown.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instadown.data.local.entity.DownloadEntity
import com.instadown.data.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>
    
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getDownloadsPaging(): PagingSource<Int, DownloadEntity>
    
    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY timestamp DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>
    
    @Query("SELECT * FROM downloads WHERE status IN ('PENDING', 'QUEUED', 'DOWNLOADING') ORDER BY timestamp ASC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>
    
    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadEntity?
    
    @Query("SELECT * FROM downloads WHERE url = :url LIMIT 1")
    suspend fun getDownloadByUrl(url: String): DownloadEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloads(downloads: List<DownloadEntity>)
    
    @Update
    suspend fun updateDownload(download: DownloadEntity)
    
    @Query("UPDATE downloads SET progress = :progress, downloadedBytes = :downloadedBytes WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, downloadedBytes: Long)
    
    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: DownloadStatus)
    
    @Query("UPDATE downloads SET status = :status, filePath = :filePath, completedAt = :completedAt WHERE id = :id")
    suspend fun markCompleted(id: String, status: DownloadStatus, filePath: String, completedAt: Long)
    
    @Query("UPDATE downloads SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun markFailed(id: String, status: DownloadStatus, errorMessage: String)
    
    @Delete
    suspend fun deleteDownload(download: DownloadEntity)
    
    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: String)
    
    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompletedDownloads()
    
    @Query("SELECT COUNT(*) FROM downloads WHERE status IN ('PENDING', 'QUEUED', 'DOWNLOADING')")
    fun getActiveDownloadCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM downloads WHERE status = 'COMPLETED'")
    fun getCompletedDownloadCount(): Flow<Int>
    
    @Query("SELECT SUM(fileSize) FROM downloads WHERE status = 'COMPLETED'")
    fun getTotalDownloadedBytes(): Flow<Long?>
    
    @Query("SELECT * FROM downloads WHERE username = :username ORDER BY timestamp DESC")
    fun getDownloadsByUsername(username: String): Flow<List<DownloadEntity>>
    
    @Query("UPDATE downloads SET isHidden = :isHidden WHERE id = :id")
    suspend fun setHidden(id: String, isHidden: Boolean)
}
