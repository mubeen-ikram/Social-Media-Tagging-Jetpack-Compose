package com.example.taggingviewtest.downloadManager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri

class DownloadManager {
    private val downloadManagerList = mutableMapOf<String, Long>()
    private val downloadedIdsList = mutableStateListOf<Long>()

    fun downloadFile(
        context: Context,
        destination: Uri,
        source: String,
        fileName: String,
        onStartDownload: (Long) -> Unit,
        onDownloadComplete: () -> Unit,
    ) {
        addDefaultFileId(source)
        val downloadManger = context.getSystemService(DownloadManager::class.java)
        if (!destination.toFile().exists()) {
            val request = DownloadManager.Request(source.toUri())
                .setMimeType("application/pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setTitle(fileName)
                .setDestinationUri(destination)
            if (getSourceId(source) == -1L) {
                val id = downloadManger.enqueue(request)
                setDownloadIds(source, id)
            }
            onStartDownload(getSourceId(source))
        } else {
            if (!isFileDownloading(source)) {
                onDownloadComplete()
                return
            }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(cnt: Context?, intent: Intent?) {
                if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id != -1L && id == getSourceId(source)) {
                        downloadedIdsList.add(id)
                        onDownloadComplete()
                    }
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun getSourceId(source: String): Long {
        return downloadManagerList.getOrDefault(source, -1L)
    }

    private fun isFileDownloading(source: String): Boolean {
        val sourceId = downloadManagerList.getOrDefault(source, -1L)
        return if (sourceId == -1L) {
            //File must be downloaded before this download Manager and should return that the file is already downloaded
            false
        } else {
            if (downloadedIdsList.contains(sourceId)) {
                //return that the file is downloaded Completely
                false
            } else {
                //return the file is currently downloading and listen to broadcast
                true
            }
        }
    }


    private fun setDownloadIds(source: String, id: Long) {
        downloadManagerList[source] = id
    }

    private fun addDefaultFileId(source: String) {
        downloadManagerList.putIfAbsent(source, -1L)
    }
}