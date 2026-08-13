package com.vpcoffee.feature.catalog.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun cropImageToSquare(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
    val source = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: return@withContext false
    val size = minOf(source.width, source.height)
    val left = (source.width - size) / 2
    val top = (source.height - size) / 2
    val square = Bitmap.createBitmap(source, left, top, size, size)
    try {
        context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
            square.compress(Bitmap.CompressFormat.JPEG, 90, output)
        } ?: false
    } finally {
        if (square != source) square.recycle()
        source.recycle()
    }
}
