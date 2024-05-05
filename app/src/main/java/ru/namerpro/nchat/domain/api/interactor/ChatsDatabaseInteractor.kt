package ru.namerpro.nchat.domain.api.interactor

import ru.namerpro.nchat.domain.model.Chat

interface ChatsDatabaseInteractor {

    suspend fun addChat(
        chat: Chat
    )

    suspend fun getChats(): List<Chat>

    suspend fun updateAliveState(
        chatId: Long,
        isAlive: Boolean
    )

}