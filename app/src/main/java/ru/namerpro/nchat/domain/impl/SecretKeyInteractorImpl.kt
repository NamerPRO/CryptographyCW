package ru.namerpro.nchat.domain.impl

import ru.namerpro.nchat.domain.api.interactor.SecretKeyInteractor
import ru.namerpro.nchat.domain.api.repository.SecretKeyRepository
import ru.namerpro.nchat.domain.model.Resource
import java.math.BigInteger
import java.util.Base64

class SecretKeyInteractorImpl(
    private val secretKeyRepository: SecretKeyRepository
) : SecretKeyInteractor {

    override suspend fun sendPartOfKey(
        receiverId: Long,
        chatId: Long,
        partOfKey: BigInteger
    ): Resource<Unit> {
        return secretKeyRepository.sendPartOfKey(receiverId, chatId, Base64.getEncoder().encodeToString(partOfKey.toByteArray()))
    }

    override suspend fun getPatsOfKeys(
        clientId: Long
    ): Resource<List<Pair<Long, BigInteger>>> {
        return secretKeyRepository.getPatsOfKeys(clientId)
    }

}