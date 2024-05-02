package ru.namerpro.nchat.domain.api.interactor

import ru.namerpro.nchat.domain.model.Message

interface MessagesDatabaseInteractor {

    suspend fun addMessage(
        chatId: Long,
        message: Message
    )

    suspend fun getMessages(
        chatId: Long
    ): List<Message>

}