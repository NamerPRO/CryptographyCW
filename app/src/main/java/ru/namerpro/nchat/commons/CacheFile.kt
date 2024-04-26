package ru.namerpro.nchat.commons

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import java.io.File
import java.io.InputStream

fun saveFileInCache(
    fileName: String,
    fileInputStream: InputStream?,
    application: Application
): File {
    if (fileInputStream == null) {
        return Uri.EMPTY.toFile()
    }
    val target = application.applicationContext.externalCacheDir
    if (target != null && !target.exists()) {
        target.mkdirs()
    }
    val copiedFile = File(target, fileName)
    val outputStream = copiedFile.outputStream()
    val buffer = ByteArray(Constants.STANDARD_BUFFER_SIZE_IN_BYTES)
    var len: Int
    while (fileInputStream.read(buffer).also { len = it } > 0) {
        outputStream.write(buffer, 0, len)
    }
    outputStream.close()
    return copiedFile
}