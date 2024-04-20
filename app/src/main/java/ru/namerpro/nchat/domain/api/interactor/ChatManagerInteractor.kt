package ru.namerpro.nchat.domain.api.interactor

import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger

interface ChatManagerInteractor {

    suspend fun addNewChat(
        creatorId: Long,
        partnerId: Long,
        chatId: Long,
        chatName: String,
        secret: BigInteger
    ): Resource<Unit>

    suspend fun getNewChats(
        clientId: Long
    ): Resource<List<Triple<Pair<Long, String>, Pair<Long, String>, BigInteger>>>

    suspend fun createChat(
        creatorId: Long,
        partnerId: Long
    ): Resource<Long>

}