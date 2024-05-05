package ru.namerpro.nchat.data.network

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import org.apache.commons.io.IOUtils.DEFAULT_BUFFER_SIZE
import ru.namerpro.nchat.domain.model.Task
import java.io.File
import java.io.IOException


class CountingRequestBody(
    private val contentType: MediaType,
    private val file: File,
    private val task: Task
): RequestBody() {

    override fun contentType(): MediaType = contentType

    override fun contentLength(): Long = file.length()

    @Throws(IOException::class)
    override fun writeTo(
        sink: BufferedSink
    ) {
        val fileLength: Long = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        file.inputStream().use {
            var read: Int
            while (it.read(buffer).also { sz -> read = sz } != -1) {
                if (task.isCancelled) {
                    return
                }
                task.progress.invoke(50.0 * read / fileLength)
                sink.write(buffer, 0, read)
            }
        }
    }

}