package ru.namerpro.nchat.data.impl

import ru.namerpro.nchat.data.Convertors
import ru.namerpro.nchat.data.db.MessageEntity
import ru.namerpro.nchat.data.db.MessagesDatabase
import ru.namerpro.nchat.domain.api.repository.MessagesDatabaseRepository
import ru.namerpro.nchat.domain.model.Message

class MessagesDatabaseRepositoryImpl(
    private val messagesDatabase: MessagesDatabase
) : MessagesDatabaseRepository {

    override suspend fun addMessage(
        chatId: Long,
        message: Message
    ) {
        val entity = MessageEntity(0, message.time, chatId, message.type, Convertors().messageSerializer(message))
        messagesDatabase.getMessageDao().addMessage(entity)
    }

    override suspend fun getMessages(
        chatId: Long
    ): List<Message> {
        return messagesDatabase.getMessageDao().getMessages(chatId).map {
            Convertors().messageDeserializer(it.contentType, it.message)
        }
    }

}