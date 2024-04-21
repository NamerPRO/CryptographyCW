package ru.namerpro.nchat.domain.impl

import ru.namerpro.nchat.domain.api.interactor.ChatManagerInteractor
import ru.namerpro.nchat.domain.api.repository.ChatManagerRepository
import ru.namerpro.nchat.domain.model.ChatData
import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger
import java.util.Base64

class ChatManagerInteractorImpl(
    private val chatManagerRepository: ChatManagerRepository
) : ChatManagerInteractor {

    override suspend fun getNewChats(
        clientId: Long
    ): Resource<List<ChatData>> {
        return chatManagerRepository.newChats(clientId)
    }

    override suspend fun createChat(
        creatorId: Long,
        partnerId: Long,
        chatData: Triple<String, String, BigInteger>
    ): Resource<Long> {
        return chatManagerRepository.createChat(creatorId, partnerId, Triple(chatData.first, chatData.second, Base64.getEncoder().encodeToString(chatData.third.toByteArray())))
    }

}