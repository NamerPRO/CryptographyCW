package ru.namerpro.nchat.domain.api.repository

import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger

interface SecretKeyRepository {

    suspend fun sendPartOfKey(
        receiverId: Long,
        chatId: Long,
        partOfKey: String
    ): Resource<Unit>

    suspend fun getPatsOfKeys(
        clientId: Long
    ): Resource<List<Pair<Long, BigInteger>>>

}