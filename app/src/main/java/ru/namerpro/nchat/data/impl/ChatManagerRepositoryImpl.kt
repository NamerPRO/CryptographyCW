package ru.namerpro.nchat.data.impl

import ru.namerpro.nchat.commons.Constants.Companion.SUCCESS_RESPONSE_CODE
import ru.namerpro.nchat.data.NetworkClient
import ru.namerpro.nchat.data.dto.dto.ChatInfoDto
import ru.namerpro.nchat.data.dto.response.CreateChatResponse
import ru.namerpro.nchat.data.dto.response.NewChatsResponse
import ru.namerpro.nchat.domain.api.repository.ChatManagerRepository
import ru.namerpro.nchat.domain.model.NewChatData
import ru.namerpro.nchat.domain.model.Cipher
import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger
import java.util.Base64

class ChatManagerRepositoryImpl(
    private val networkClient: NetworkClient
) : ChatManagerRepository {

    override suspend fun newChats(
        clientId: Long
    ): Resource<List<NewChatData>> {
        val response = networkClient.newChats(clientId)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success(
                (response as NewChatsResponse).chats.map {
                    NewChatData(
                        it.chatName,
                        it.chatId,
                        it.partnerName,
                        it.partnerId,
                        BigInteger(Base64.getDecoder()
                            .decode(it.secret.substring(1, it.secret.length - 1))),
                        Cipher.fromString(it.cipherType),
                        Base64.getDecoder().decode(it.iv)
                    )
                }
            )
        } else {
            Resource.Error(response.responseCode)
        }
    }

    override suspend fun createChat(
        creatorId: Long,
        partnerId: Long,
        chatData: ChatInfoDto
    ): Resource<Long> {
        val response = networkClient.createChat(creatorId, partnerId, chatData)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success(
                (response as CreateChatResponse).chatId
            )
        } else {
            Resource.Error(response.responseCode)
        }
    }

}