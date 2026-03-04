package com.instadown.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.instadown.data.model.DownloadStatus
import com.instadown.data.repository.DownloadRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var downloadRepository: DownloadRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Resume pending downloads after reboot
            CoroutineScope(Dispatchers.IO).launch {
                val pendingDownloads = downloadRepository.getDownloadsByStatus(DownloadStatus.PENDING)
                pendingDownloads.collect { downloads ->
                    if (downloads.isNotEmpty()) {
                        DownloadService.startService(context)
                    }
                }
            }
        }
    }
}
