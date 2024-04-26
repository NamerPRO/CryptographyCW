package ru.namerpro.nchat.domain.model

import java.io.InputStream

sealed class Message(
    val contentType: Int,
    val isMessageReceived: Boolean
) {

    data class Data(
        val messageReceived: Boolean,
        val text: String,
        val date: String,
        val messageContentType: Int
    ) : Message(messageContentType, messageReceived)

    data class File(
        val messageReceived: Boolean,
        val name: String?,
        val src: InputStream,
        val type: String,
        val size: Long,
        val date: String,
        val messageContentType: Int,
        var dest: String? = null
    ) : Message(messageContentType, messageReceived)

    data object ChatEnd : Message(MESSAGE_CONVERSATION_END_CODE,true)

    data class FailedToLoadFile(
        val messageContentType: Int,
        val date: String
    ) : Message(messageContentType, true)

    companion object {
        const val MESSAGE_TEXT_CODE = 0
        const val MESSAGE_IMAGE_CODE = 1
        const val MESSAGE_FILE_CODE = 2
        const val MESSAGE_CONVERSATION_END_CODE = 3
    }

}