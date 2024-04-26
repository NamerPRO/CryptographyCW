package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.Response
import java.io.InputStream

data class DownloadFileResponse(
    val input: InputStream,
    val size: Long
) : Response()