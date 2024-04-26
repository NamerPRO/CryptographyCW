package ru.namerpro.nchat.data

import com.google.gson.Gson
import ru.namerpro.nchat.commons.Constants
import ru.namerpro.nchat.commons.Constants.Companion.ENCRYPTED_FILE_PREFIX
import ru.namerpro.nchat.commons.Constants.Companion.SUCCESS_RESPONSE_CODE
import ru.namerpro.nchat.data.dto.response.DownloadFileResponse
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.Message.Companion.MESSAGE_CONVERSATION_END_CODE
import ru.namerpro.nchat.domain.model.Message.Companion.MESSAGE_TEXT_CODE
import java.util.Base64

class Convertors(
    private val networkClient: NetworkClient
) {

    suspend fun messageFromString(
        messageAsString: String,
        encrypter: SymmetricEncrypterContext
    ): Message {
        if (messageAsString[0] == Constants.EXIT_MESSAGE_CODE) {
            return Message.ChatEnd
        }
        val data = Gson().fromJson(String(encrypter.decrypt(Base64.getDecoder().decode(messageAsString.substring(2, messageAsString.length - 1))).get()), Message.Data::class.java)
        return when (data.contentType) {
            MESSAGE_CONVERSATION_END_CODE -> Message.ChatEnd
            MESSAGE_TEXT_CODE -> data
            else -> {
                val fileData = networkClient.downloadFile("${ENCRYPTED_FILE_PREFIX}${data.text}")
                if (fileData.responseCode == SUCCESS_RESPONSE_CODE) {
                    fileData as DownloadFileResponse
                    val type = data.text.takeLastWhile { it != '.' }
                    Message.File(true, data.text, fileData.input, type, fileData.size, data.date, data.contentType)
                } else {
                    Message.FailedToLoadFile(data.contentType, data.date)
                }
            }
        }
    }

}