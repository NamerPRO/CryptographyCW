package ru.namerpro.nchat.data

import com.google.gson.Gson
import ru.namerpro.nchat.commons.Constants
import ru.namerpro.nchat.data.db.ChatEntity
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.model.Chat
import ru.namerpro.nchat.domain.model.Cipher
import ru.namerpro.nchat.domain.model.Message
import java.util.Base64

class Convertors {

    fun messageFromString(
        messageAsString: String,
        encrypter: SymmetricEncrypterContext
    ): Message {
        if (messageAsString[0] == Constants.EXIT_MESSAGE_CODE) {
            return Message.ChatEnd
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
    ) = Gson().toJson(message)

    fun messageDeserializer(
        serializedMessage: String
    ) = Gson().fromJson(serializedMessage, Message.Data::class.java)

    fun chatToChatEntity(
        chat: Chat
    ): ChatEntity {
        val encoder = Base64.getEncoder()
        return ChatEntity(
            0,
            chat.id,
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
            chatEntity.name,
            chatEntity.partnerName,
            Cipher.fromString(chatEntity.cipher),
            decoder.decode(chatEntity.key),
            decoder.decode(chatEntity.iv)
        )
    }

}