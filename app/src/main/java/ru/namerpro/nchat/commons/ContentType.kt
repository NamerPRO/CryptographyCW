package ru.namerpro.nchat.commons

import android.webkit.MimeTypeMap
import ru.namerpro.nchat.domain.model.Message

fun getContentType(
    type: String
): Int {
    return if (MimeTypeMap.getSingleton().getMimeTypeFromExtension(type)?.startsWith("image") == true) {
        Message.MESSAGE_IMAGE_CODE
    } else {
        Message.MESSAGE_FILE_CODE
    }
}