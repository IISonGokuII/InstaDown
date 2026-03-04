package com.instadown.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.instadown.MainActivity
import com.instadown.R
import com.instadown.data.model.DownloadItem
import com.instadown.data.model.DownloadStatus
import com.instadown.data.repository.DownloadRepository
import com.instadown.download.DownloadEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {

    @Inject
    lateinit var downloadRepository: DownloadRepository

    @Inject
    lateinit var downloadEngine: DownloadEngine

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeDownloadJobs = mutableMapOf<String, Job>()

    companion object {
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "action_start"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_RESUME = "action_resume"
        const val ACTION_CANCEL = "action_cancel"
        const val EXTRA_DOWNLOAD_ID = "extra_download_id"

        fun startService(context: Context) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Starting download service..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startDownloads()
            ACTION_PAUSE -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { pauseDownload(it) }
            }
            ACTION_RESUME -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { resumeDownload(it) }
            }
            ACTION_CANCEL -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { cancelDownload(it) }
            }
        }
        return START_STICKY
    }

    private fun startDownloads() {
        serviceScope.launch {
            downloadRepository.getActiveDownloads().collectLatest { downloads ->
                downloads.forEach { download ->
                    if (!activeDownloadJobs.containsKey(download.id)) {
                        startDownload(download)
                    }
                }
                updateNotification(downloads)
            }
        }
    }

    private fun startDownload(download: DownloadItem) {
        val job = serviceScope.launch {
            downloadEngine.downloadFile(
                id = download.id,
                url = download.url,
                fileName = download.fileName ?: "${System.currentTimeMillis()}.mp4",
                username = download.username
            ).collectLatest { progress ->
                // Progress wird vom DownloadEngine automatisch aktualisiert
            }
        }
        activeDownloadJobs[download.id] = job
    }

    private fun pauseDownload(downloadId: String) {
        downloadEngine.pauseDownload(downloadId)
    }

    private fun resumeDownload(downloadId: String) {
        downloadEngine.resumeDownload(downloadId)
    }

    private fun cancelDownload(downloadId: String) {
        activeDownloadJobs[downloadId]?.cancel()
        activeDownloadJobs.remove(downloadId)
        downloadEngine.cancelDownload(downloadId)
        serviceScope.launch {
            downloadRepository.updateStatus(downloadId, DownloadStatus.CANCELLED)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download notifications"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): android.app.Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("InstaDown")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(downloads: List<DownloadItem>) {
        val activeCount = downloads.count { it.status == DownloadStatus.DOWNLOADING }
        val pendingCount = downloads.count { it.status == DownloadStatus.PENDING }
        val contentText = when {
            activeCount > 0 -> "$activeCount download${if (activeCount > 1) "s" else ""} in progress"
            pendingCount > 0 -> "$pendingCount pending"
            else -> "All downloads completed"
        }

        val notification = buildNotification(contentText)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        activeDownloadJobs.forEach { (_, job) -> job.cancel() }
        activeDownloadJobs.clear()
    }
}
