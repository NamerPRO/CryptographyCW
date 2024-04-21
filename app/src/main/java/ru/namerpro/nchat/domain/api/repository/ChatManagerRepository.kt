package ru.namerpro.nchat.domain.api.repository

import ru.namerpro.nchat.domain.model.ChatData
import ru.namerpro.nchat.domain.model.Resource

interface ChatManagerRepository {

    suspend fun newChats(
        clientId: Long
    ): Resource<List<ChatData>>

    suspend fun createChat(
        creatorId: Long,
        partnerId: Long,
        chatData: Triple<String, String, String>
    ): Resource<Long>

}