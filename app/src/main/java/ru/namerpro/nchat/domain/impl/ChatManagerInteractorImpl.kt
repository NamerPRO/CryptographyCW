package ru.namerpro.nchat.domain.impl

import ru.namerpro.nchat.data.dto.dto.ChatInfoDto
import ru.namerpro.nchat.domain.api.interactor.ChatManagerInteractor
import ru.namerpro.nchat.domain.api.repository.ChatManagerRepository
import ru.namerpro.nchat.domain.model.ChatInfo
import ru.namerpro.nchat.domain.model.NewChatData
import ru.namerpro.nchat.domain.model.Resource
import java.util.Base64

class ChatManagerInteractorImpl(
    private val chatManagerRepository: ChatManagerRepository
) : ChatManagerInteractor {

    override suspend fun getNewChats(
        clientId: Long
    ): Resource<List<NewChatData>> {
        return chatManagerRepository.newChats(clientId)
    }

    override suspend fun createChat(
        creatorId: Long,
        partnerId: Long,
        chatData: ChatInfo
    ): Resource<Long> {
        return chatManagerRepository.createChat(
            creatorId,
            partnerId,
            ChatInfoDto(
                chatName = chatData.chatName,
                cipherType = chatData.cipherType,
                secret = Base64.getEncoder().encodeToString(
                    chatData.secret.toByteArray()
                ),
                iv = Base64.getEncoder().encodeToString(
                    chatData.iv
                )
            )
        )
    }

    override suspend fun leaveChat(
        clientId: Long,
        chatId: Long
    ): Resource<Unit> {
        return  chatManagerRepository.leaveChat(clientId, chatId)
    }

}