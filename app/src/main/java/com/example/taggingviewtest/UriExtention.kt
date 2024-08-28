package com.example.taggingviewtest


import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.util.Locale

fun Uri?.toFileContent(context: Context): String {
    this?.let { currentUri ->
        val item = context.contentResolver.openInputStream(currentUri)
        val bytes = item?.readBytes()
        val mimeType = this.getMimeType(context)
        val fileType = mimeType?.split("/")?.lastOrNull() ?: "jpeg"
        val filename = currentUri.path?.split('/')?.last() + ".$fileType"
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(bytes)
        }
        item?.close()

        if (filename == null)
            return ""

        return File(context.filesDir, filename).path

    }
    return ""
}

fun Uri.getMimeType(context: Context): String? {
    return when (scheme) {
        ContentResolver.SCHEME_CONTENT -> context.contentResolver.getType(this)
        ContentResolver.SCHEME_FILE -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            MimeTypeMap.getFileExtensionFromUrl(toString()).toLowerCase(Locale.US)
        )

        else -> null
    }
}