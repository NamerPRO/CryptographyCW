package ru.namerpro.nchat.domain.api.repository

import ru.namerpro.nchat.data.dto.dto.ChatInfoDto
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.entities.ciphers.context.encrypter.Encrypter
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import ru.namerpro.nchat.domain.model.NewChatData
import ru.namerpro.nchat.domain.model.Resource

interface ChatManagerRepository {

    suspend fun newChats(
        clientId: Long,
    ): Resource<List<NewChatData>>

    suspend fun createChat(
        creatorId: Long,
        partnerId: Long,
        chatData: ChatInfoDto
    ): Resource<Long>

}