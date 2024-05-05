package ru.namerpro.nchat.data

import com.google.gson.Gson
import ru.namerpro.nchat.commons.Constants
import ru.namerpro.nchat.data.db.ChatEntity
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.model.Chat
import ru.namerpro.nchat.domain.model.Cipher
import ru.namerpro.nchat.domain.model.Message
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Locale

class Convertors {

    fun messageFromString(
        messageAsString: String,
        encrypter: SymmetricEncrypterContext
    ): Message {
        if (messageAsString[0] == Constants.EXIT_MESSAGE_CODE) {
            return Message.ChatEnd(System.currentTimeMillis())
        }
        return Gson().fromJson(
            String(
                encrypter.decrypt(
                    Base64.getDecoder()
                        .decode(messageAsString.substring(2, messageAsString.length - 1))
                ).get()
            ), Message.Data::class.java
        )
    }

    fun messageSerializer(
        message: Message
    ): String = Gson().toJson(message)

    fun messageDeserializer(
        contentType: Int,
        serializedMessage: String
    ): Message = if (contentType == Message.MESSAGE_CONVERSATION_END_CODE) {
        Gson().fromJson(serializedMessage, Message.ChatEnd::class.java)
    } else {
        Gson().fromJson(serializedMessage, Message.Data::class.java)
    }

    fun chatToChatEntity(
        chat: Chat
    ): ChatEntity {
        val encoder = Base64.getEncoder()
        return ChatEntity(
            chat.id,
            chat.isAlive,
            chat.name,
            chat.partnerName,
            chat.cipher.name,
            encoder.encodeToString(chat.key),
            encoder.encodeToString(chat.iv)
        )
    }

    fun chatEntityToChat(
        chatEntity: ChatEntity
    ): Chat {
        val decoder = Base64.getDecoder()
        return Chat(
            chatEntity.chatId,
            chatEntity.isAlive,
            chatEntity.name,
            chatEntity.partnerName,
            Cipher.fromString(chatEntity.cipher),
            decoder.decode(chatEntity.key),
            decoder.decode(chatEntity.iv)
        )
    }

}