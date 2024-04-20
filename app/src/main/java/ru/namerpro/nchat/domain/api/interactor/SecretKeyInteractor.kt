package ru.namerpro.nchat.domain.api.interactor

import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger

interface SecretKeyInteractor {

    suspend fun sendPartOfKey(
        receiverId: Long,
        chatId: Long,
        partOfKey: BigInteger
    ): Resource<Unit>

    suspend fun getPatsOfKeys(
        clientId: Long
    ): Resource<List<Pair<Long, BigInteger>>>

}