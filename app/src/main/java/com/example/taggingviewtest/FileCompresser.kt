package com.example.taggingviewtest

import android.content.Context
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import java.io.File


suspend fun compressFile(context: Context, path: String): String {
    val compressedImageFile = Compressor.compress(context, File(path)) {
        quality(75)
    }
    return compressedImageFile.path
}