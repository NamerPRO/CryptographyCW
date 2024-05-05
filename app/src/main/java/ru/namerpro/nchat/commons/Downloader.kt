package ru.namerpro.nchat.commons

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import ru.namerpro.nchat.commons.Constants.Companion.API_BASE_URL
import ru.namerpro.nchat.domain.model.Task
import java.io.File
import java.util.concurrent.TimeUnit

fun interface ResponseBodyListener {
    fun update(responseBody: ResponseBody)
}

fun getDownloader(
    listener: ResponseBodyListener
): Retrofit = Retrofit.Builder()
    .baseUrl(API_BASE_URL)
    .client(initHttpDownloadListenerClient(listener))
    .build()

private fun initHttpDownloadListenerClient(
    listener: ResponseBodyListener
) = OkHttpClient.Builder()
    .connectTimeout(0, TimeUnit.SECONDS)
    .readTimeout(0, TimeUnit.SECONDS)
    .writeTimeout(0, TimeUnit.SECONDS)
    .addNetworkInterceptor { chain ->
        chain.proceed(chain.request()).also { originalResponse ->
            println(originalResponse.body()?.contentLength())
            originalResponse.body().let { listener.update(it!!) }
        }
    }
    .build()

fun ResponseBody.downloadToFileWithProgress(
    fileName: String,
    pathToFolder: File,
    task: Task
) {
    var deleteFile = true
    if (!pathToFolder.exists()) {
        pathToFolder.mkdirs()
    }
    val file = File(pathToFolder, fileName)
    try {
        byteStream().use { inputStream ->
            file.outputStream().use { outputStream ->
                val data = ByteArray(DEFAULT_BUFFER_SIZE)
                var progressBytes = 0L

                var length: Int
                while (inputStream.read(data).also { length = it } != -1) {
                    if (task.isCancelled) {
//                        file.delete()
                        return
                    }
                    outputStream.write(data, 0, length)
                    progressBytes += length
                    task.progress.invoke(50.0 * length / contentLength())
                }

                when {
                    progressBytes < contentLength() -> throw Exception("Too few bytes read!")
                    progressBytes > contentLength() -> throw Exception("Too many bytes read!")
                    else -> deleteFile = false
                }
            }
        }
    } finally {
        if (deleteFile) {
            file.delete()
        }
    }
}