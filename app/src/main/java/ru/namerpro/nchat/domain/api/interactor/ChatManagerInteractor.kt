package ru.namerpro.nchat.domain.api.interactor

import ru.namerpro.nchat.domain.model.ChatData
import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger

interface ChatManagerInteractor {

    suspend fun getNewChats(
        clientId: Long
    ): Resource<List<ChatData>>

    suspend fun createChat(
        creatorId: Long,
        partnerId: Long,
        chatData: Triple<String, String, BigInteger>
    ): Resource<Long>

}