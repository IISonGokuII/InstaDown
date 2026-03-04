package com.instadown.download

import android.app.DownloadManager
nimport android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.instadown.data.model.DownloadStatus
import com.instadown.data.repository.DownloadRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.io.readByteArray
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadEngine @Inject constructor(
    private val context: Context,
    private val httpClient: HttpClient,
    private val downloadRepository: DownloadRepository
) {
    private val activeDownloads = mutableMapOf<String, DownloadJob>()

    data class DownloadJob(
        val id: String,
        val url: String,
        val destinationFile: File,
        var isPaused: Boolean = false,
        var isCancelled: Boolean = false
    )

    data class DownloadProgress(
        val id: String,
        val progress: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speed: Long
    )

    fun downloadFile(
        id: String,
        url: String,
        fileName: String,
        username: String? = null
    ): Flow<DownloadProgress> = flow {
        downloadRepository.updateStatus(id, DownloadStatus.DOWNLOADING)
        
        val baseDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "InstaDown"
        ).apply { mkdirs() }
        
        val userDir = username?.let { File(baseDir, it).apply { mkdirs() } } ?: baseDir
        val destinationFile = File(userDir, fileName)
        
        val job = DownloadJob(id, url, destinationFile)
        activeDownloads[id] = job
        
        try {
            // Check for partial content support
            val headResponse = httpClient.head(url)
            val supportsResume = headResponse.headers[HttpHeaders.AcceptRanges] == "bytes"
            val totalBytes = headResponse.contentLength() ?: -1L
            
            var downloadedBytes = if (supportsResume && destinationFile.exists()) {
                destinationFile.length()
            } else 0L
            
            if (totalBytes > 0 && downloadedBytes >= totalBytes) {
                emit(DownloadProgress(id, 100, totalBytes, totalBytes, 0))
                downloadRepository.markCompleted(id, destinationFile.absolutePath)
                return@flow
            }
            
            val response: HttpResponse = httpClient.get(url) {
                if (supportsResume && downloadedBytes > 0) {
                    headers[HttpHeaders.Range] = "bytes=$downloadedBytes-"
                }
            }
            
            val channel: ByteReadChannel = response.body()
            val append = supportsResume && downloadedBytes > 0
            
            val outputStream = if (append) {
                RandomAccessFile(destinationFile, "rw").apply {
                    seek(downloadedBytes)
                }
            } else {
                FileOutputStream(destinationFile)
            }
            
            val startTime = System.currentTimeMillis()
            var lastEmitTime = startTime
            var lastDownloadedBytes = downloadedBytes
            
            use(outputStream) { stream ->
                while (!channel.isClosedForRead && !job.isCancelled) {
                    if (job.isPaused) {
                        delay(100)
                        continue
                    }
                    
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    val bytes = packet.readByteArray()
                    
                    if (bytes.isEmpty()) break
                    
                    when (stream) {
                        is RandomAccessFile -> stream.write(bytes)
                        is FileOutputStream -> stream.write(bytes)
                    }
                    
                    downloadedBytes += bytes.size
                    
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastEmitTime >= 500) {
                        val progress = if (totalBytes > 0) {
                            ((downloadedBytes * 100) / totalBytes).toInt()
                        } else 0
                        
                        val speed = if (currentTime > lastEmitTime) {
                            ((downloadedBytes - lastDownloadedBytes) * 1000) / (currentTime - lastEmitTime)
                        } else 0
                        
                        emit(DownloadProgress(id, progress, downloadedBytes, totalBytes, speed))
                        downloadRepository.updateProgress(id, progress, downloadedBytes)
                        
                        lastEmitTime = currentTime
                        lastDownloadedBytes = downloadedBytes
                    }
                }
            }
            
            if (job.isCancelled) {
                downloadRepository.updateStatus(id, DownloadStatus.CANCELLED)
            } else {
                val finalProgress = if (totalBytes > 0) 100 else 0
                emit(DownloadProgress(id, finalProgress, downloadedBytes, totalBytes, 0))
                downloadRepository.markCompleted(id, destinationFile.absolutePath)
            }
            
        } catch (e: Exception) {
            downloadRepository.markFailed(id, e.message ?: "Unknown error")
            throw e
        } finally {
            activeDownloads.remove(id)
        }
    }.flowOn(Dispatchers.IO)
    
    fun pauseDownload(id: String) {
        activeDownloads[id]?.isPaused = true
    }
    
    fun resumeDownload(id: String) {
        activeDownloads[id]?.isPaused = false
    }
    
    fun cancelDownload(id: String) {
        activeDownloads[id]?.isCancelled = true
    }
    
    private inline fun <T : AutoCloseable, R> use(closeable: T, block: (T) -> R): R {
        return try {
            block(closeable)
        } finally {
            when (closeable) {
                is RandomAccessFile -> closeable.close()
                is FileOutputStream -> closeable.close()
            }
        }
    }
}
