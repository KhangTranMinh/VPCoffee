package com.vpcoffee.feature.catalog.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

suspend fun cropImageToSquare(context: Context, uri: Uri): Uri? = withContext(Dispatchers.IO) {
    val source = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: return@withContext null
    if (source.width == source.height) {
        source.recycle()
        return@withContext uri
    }
    val size = minOf(source.width, source.height)
    val left = (source.width - size) / 2
    val top = (source.height - size) / 2
    val square = Bitmap.createBitmap(source, left, top, size, size)
    try {
        val imageDirectory = File(context.filesDir, "images").apply { mkdirs() }
        val imageFile = File(imageDirectory, "drink-${UUID.randomUUID()}.jpg")
        imageFile.outputStream().use { output ->
            if (square.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
            } else {
                null
            }
        }
    } finally {
        if (square != source) square.recycle()
        source.recycle()
    }
}
