package com.example.taggingviewtest.downloadManager

import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toFile


@Composable
fun PDFDownload(fileUrl: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val fileName = fileUrl.split("/").last()
    val destination = Uri.withAppendedPath(
        Uri.fromFile(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)),
        fileName
    )
    val fileState = remember {
        mutableStateOf(
            FileDownloadState(
                file = destination,
                uiState = UiState.Loading(Unit),
                id = -1L,
            )
        )
    }
    val downloadManager: DownloadManager = koinInject()
    downloadManager.downloadFile(
        context = context,
        destination = destination,
        source = fileUrl,
        fileName = fileName,
        onStartDownload = { id ->
            fileState.value = fileState.value.copy(
                id = id,
                uiState = UiState.Loading(Unit)
            )
        }
    ) {
        fileState.value = fileState.value.copy(
            uiState = UiState.Default(Unit),
            file = destination
        )
    }
    val downloaded = fileState.value.uiState is UiState.Default && destination.toFile().exists()
    if (!downloaded) {
        LoadingPDFView(onDismiss)
        return

    } else {
        PdfViewer(uri = fileState.value.file!!) {
            onDismiss()
        }
    }
}


data class FileDownloadState(
    val uiState: UiState<Unit>,
    val file: Uri?,
    val id: Long?
) {
    companion object {
        val defaultValue = FileDownloadState(
            uiState = UiState.Default(Unit),
            file = null,
            id = -1L,
        )
    }
}