package ru.namerpro.nchat.domain.impl

import ru.namerpro.nchat.domain.api.interactor.MessagesDatabaseInteractor
import ru.namerpro.nchat.domain.api.repository.MessagesDatabaseRepository
import ru.namerpro.nchat.domain.model.Message

class MessagesDatabaseInteractorImpl(
    private val messagesDatabaseRepository: MessagesDatabaseRepository
) : MessagesDatabaseInteractor {

    override suspend fun addMessage(
        chatId: Long,
        message: Message
    ) {
        return messagesDatabaseRepository.addMessage(chatId, message)
    }

    override suspend fun getMessages(
        chatId: Long
    ): List<Message> {
        return messagesDatabaseRepository.getMessages(chatId)
    }

}