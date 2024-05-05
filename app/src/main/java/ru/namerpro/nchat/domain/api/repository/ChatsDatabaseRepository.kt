package ru.namerpro.nchat.domain.api.repository

import ru.namerpro.nchat.domain.model.Chat

interface ChatsDatabaseRepository {

    suspend fun addChat(
        chat: Chat
    )

    suspend fun getChats(): List<Chat>

    suspend fun updateAliveState(
        chatId: Long,
        isAlive: Boolean
    )

}