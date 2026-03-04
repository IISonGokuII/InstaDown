package com.instadown.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.instadown.MainActivity
import com.instadown.R
import com.instadown.service.DownloadService

class DownloadWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_DOWNLOAD = "com.instadown.widget.ACTION_DOWNLOAD"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_download)

        // Open app intent
        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, openAppIntent)

        // Quick download intent
        val downloadIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, DownloadWidgetProvider::class.java).apply {
                action = ACTION_DOWNLOAD
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_download_button, downloadIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_DOWNLOAD) {
            // Start download service to check clipboard
            DownloadService.startService(context)
        }
    }
}
