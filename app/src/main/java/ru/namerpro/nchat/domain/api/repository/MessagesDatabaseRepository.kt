package ru.namerpro.nchat.domain.api.repository

import ru.namerpro.nchat.domain.model.Message

interface MessagesDatabaseRepository {

    suspend fun addMessage(
        chatId: Long,
        message: Message
    )

    suspend fun getMessages(
        chatId: Long
    ): List<Message>

}