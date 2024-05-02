package ru.namerpro.nchat.commons

import ru.namerpro.nchat.domain.model.Message

const val UNKNOWN_CONTENT = Message.MESSAGE_FILE_CODE

fun getContentType(
    type: String
): Int {
    return when (type) {
        "png", "bmp", "jpg", "jpeg" -> Message.MESSAGE_IMAGE_CODE
        else -> Message.MESSAGE_FILE_CODE
    }
}