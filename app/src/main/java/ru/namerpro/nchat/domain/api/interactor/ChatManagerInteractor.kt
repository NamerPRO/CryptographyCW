package ru.namerpro.nchat.domain.api.interactor

import ru.namerpro.nchat.domain.model.ChatInfo
import ru.namerpro.nchat.domain.model.NewChatData
import ru.namerpro.nchat.domain.model.Resource

interface ChatManagerInteractor {

    suspend fun getNewChats(
        clientId: Long
    ): Resource<List<NewChatData>>

    suspend fun createChat(
        creatorId: Long,
        partnerId: Long,
        chatData: ChatInfo
    ): Resource<Long>

    suspend fun leaveChat(
        clientId: Long,
        chatId: Long
    ): Resource<Unit>

}