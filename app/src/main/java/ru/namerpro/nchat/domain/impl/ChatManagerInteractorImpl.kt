package ru.namerpro.nchat.domain.impl

import ru.namerpro.nchat.domain.api.interactor.ChatManagerInteractor
import ru.namerpro.nchat.domain.api.repository.ChatManagerRepository
import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger
import java.util.Base64

class ChatManagerInteractorImpl(
    private val chatManagerRepository: ChatManagerRepository
) : ChatManagerInteractor {

    override suspend fun addNewChat(
        creatorId: Long,
        partnerId: Long,
        chatId: Long,
        chatName: String,
        secret: BigInteger
    ): Resource<Unit> {
        return chatManagerRepository.addNewChat(creatorId, partnerId, chatId, chatName, Base64.getEncoder().encodeToString(secret.toByteArray()))
    }

    override suspend fun getNewChats(
        clientId: Long
    ): Resource<List<Triple<Pair<Long, String>, Pair<Long, String>, BigInteger>>> {
        return chatManagerRepository.newChats(clientId)
    }

    override suspend fun createChat(
        creatorId: Long,
        partnerId: Long
    ): Resource<Long> {
        return chatManagerRepository.createChat(creatorId, partnerId)
    }

}