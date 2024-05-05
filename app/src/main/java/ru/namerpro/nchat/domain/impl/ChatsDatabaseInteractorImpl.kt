package ru.namerpro.nchat.domain.impl

import ru.namerpro.nchat.domain.api.interactor.ChatsDatabaseInteractor
import ru.namerpro.nchat.domain.api.repository.ChatsDatabaseRepository
import ru.namerpro.nchat.domain.model.Chat

class ChatsDatabaseInteractorImpl(
    private val chatsDatabaseRepository: ChatsDatabaseRepository
) : ChatsDatabaseInteractor {

    override suspend fun addChat(
        chat: Chat
    ) {
        chatsDatabaseRepository.addChat(chat)
    }

    override suspend fun getChats(): List<Chat> {
        return chatsDatabaseRepository.getChats()
    }

    override suspend fun updateAliveState(
        chatId: Long,
        isAlive: Boolean
    ) {
        return chatsDatabaseRepository.updateAliveState(chatId, isAlive)
    }

}