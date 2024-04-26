package ru.namerpro.nchat.domain.api.repository

import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.domain.model.SecretKey

interface SecretKeyRepository {

    suspend fun sendPartOfKey(
        receiverId: Long,
        chatId: Long,
        partOfKey: String
    ): Resource<Unit>

    suspend fun getPatsOfKeys(
        clientId: Long
    ): Resource<List<SecretKey>>

}