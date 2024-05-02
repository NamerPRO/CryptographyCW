package ru.namerpro.nchat.data.impl

import ru.namerpro.nchat.data.Convertors
import ru.namerpro.nchat.data.db.ChatsDatabase
import ru.namerpro.nchat.domain.api.repository.ChatsDatabaseRepository
import ru.namerpro.nchat.domain.model.Chat

class ChatsDatabaseRepositoryImpl(
    private val chatsDatabase: ChatsDatabase
) : ChatsDatabaseRepository {

    override suspend fun addChat(
        chat: Chat
    ) {
        chatsDatabase.getChatsDao().addChat(Convertors().chatToChatEntity(chat))
    }

    override suspend fun getChats(): List<Chat> {
        return chatsDatabase.getChatsDao().getChats().map {
            Convertors().chatEntityToChat(it)
        }
    }

}