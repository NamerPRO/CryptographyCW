package ru.namerpro.nchat.data.impl

import ru.namerpro.nchat.commons.Constants.Companion.SUCCESS_RESPONSE_CODE
import ru.namerpro.nchat.data.NetworkClient
import ru.namerpro.nchat.data.dto.response.CreateChatResponse
import ru.namerpro.nchat.data.dto.response.NewChatsResponse
import ru.namerpro.nchat.domain.api.repository.ChatManagerRepository
import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger
import java.util.Base64

class ChatManagerRepositoryImpl(
    private val networkClient: NetworkClient
) : ChatManagerRepository {

    override suspend fun addNewChat(
        creatorId: Long,
        partnerId: Long,
        chatId: Long,
        chatName: String,
        secret: String
    ): Resource<Unit> {
        val response = networkClient.addNewChat(creatorId, partnerId, chatId, chatName, secret)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success()
        } else {
            Resource.Error(response.responseCode)
        }
    }

    override suspend fun newChats(
        clientId: Long
    ): Resource<List<Triple<Pair<Long, String>, Pair<Long, String>, BigInteger>>> {
        val response = networkClient.newChats(clientId)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success(
                (response as NewChatsResponse).chats.map {
                    Triple(
                        it.first,
                        it.second,
                        BigInteger(Base64.getDecoder()
                            .decode(it.third.substring(1, it.third.length - 1)))
                    )
                }
            )
        } else {
            Resource.Error(response.responseCode)
        }
    }

    override suspend fun createChat(
        creatorId: Long,
        partnerId: Long
    ): Resource<Long> {
        val response = networkClient.createChat(creatorId, partnerId)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success(
                (response as CreateChatResponse).chatId
            )
        } else {
            Resource.Error(response.responseCode)
        }
    }

}