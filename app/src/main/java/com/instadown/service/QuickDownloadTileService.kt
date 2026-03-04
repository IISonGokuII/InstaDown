package com.instadown.service

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.instadown.data.repository.InstagramRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class QuickDownloadTileService : TileService() {

    @Inject
    lateinit var instagramRepository: InstagramRepository

    override fun onClick() {
        super.onClick()
        
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString()
            
            if (text != null && instagramRepository.validateInstagramUrl(text)) {
                // Start Download Service with the URL
                DownloadService.startService(this)
                updateTile(Tile.STATE_ACTIVE, "Download started")
            } else {
                updateTile(Tile.STATE_UNAVAILABLE, "No valid URL")
            }
        } else {
            updateTile(Tile.STATE_UNAVAILABLE, "Clipboard empty")
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile(Tile.STATE_INACTIVE, "Tap to download")
    }

    private fun updateTile(state: Int, subtitle: String) {
        qsTile?.apply {
            this.state = state
            this.label = "InstaDown"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.subtitle = subtitle
            }
            updateTile()
        }
    }
}
