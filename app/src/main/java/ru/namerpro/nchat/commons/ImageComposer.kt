package ru.namerpro.nchat.commons

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.core.net.toUri
import ru.namerpro.nchat.commons.Constants.Companion.COMPOSED_FILE_PREFIX
import java.io.File

private const val width = 300
private const val height = 300

fun composedImageUri(
    fullImagePath: String,
    application: Application
): Uri {
    val cacheDir = application.externalCacheDir
    if (cacheDir?.exists() != true) {
        cacheDir?.mkdirs()
    }
    val fileComposed = File(cacheDir, "${COMPOSED_FILE_PREFIX}${getFileName(fullImagePath)}")
    if (!fileComposed.exists()) {
        fileComposed.outputStream().use {
            val bitmapImage = BitmapFactory.decodeFile(fullImagePath)
            val scaledBitmapImage = ThumbnailUtils.extractThumbnail(bitmapImage, width, height)
            scaledBitmapImage.compress(Bitmap.CompressFormat.JPEG, 30, it)
        }
    }
    return fileComposed.toUri()
}