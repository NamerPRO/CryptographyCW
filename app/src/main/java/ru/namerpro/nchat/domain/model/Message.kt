package ru.namerpro.nchat.domain.model

import kotlinx.coroutines.CoroutineScope
import java.io.InputStream

sealed class Message(
    val type: Int,
    val isMessageReceived: Boolean,
    val time: Long
) {

    data class Data(
        val text: String,
        val date: String,
        val messageTime: Long,
        var isReceived: Boolean,
        val contentType: Int
    ) : Message(contentType, isReceived, messageTime)

    data class File(
        var devicePath: String? = null,
        val realName: String,
        val date: String,
        val messageTime: Long,
        val isReceived: Boolean,
        val contentType: Int,
        var file: Pair<Long, InputStream?>? = null,
        var progress: Double,
        var task: Task? = null
    ) : Message(contentType, isReceived, messageTime)

    data class ChatEnd(
        val messageTime: Long
    ) : Message(MESSAGE_CONVERSATION_END_CODE, true, messageTime)

    companion object {
        const val MESSAGE_TEXT_CODE = 0
        const val MESSAGE_IMAGE_CODE = 1
        const val MESSAGE_FILE_CODE = 2
        const val MESSAGE_CONVERSATION_END_CODE = 3
    }

}