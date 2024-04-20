package ru.namerpro.nchat.domain.api.repository

import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger

interface ChatManagerRepository {

    suspend fun addNewChat(
        creatorId: Long,
        partnerId: Long,
        chatId: Long,
        chatName: String,
        secret: String
    ): Resource<Unit>

    suspend fun newChats(
        clientId: Long
    ): Resource<List<Triple<Pair<Long, String>, Pair<Long, String>, BigInteger>>>

    suspend fun createChat(
        creatorId: Long,
        partnerId: Long
    ): Resource<Long>

}