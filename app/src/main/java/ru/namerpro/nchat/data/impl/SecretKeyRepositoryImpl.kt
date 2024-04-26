package ru.namerpro.nchat.data.impl

import ru.namerpro.nchat.commons.Constants.Companion.SUCCESS_RESPONSE_CODE
import ru.namerpro.nchat.data.NetworkClient
import ru.namerpro.nchat.data.dto.response.GetPartsOfKeysResponse
import ru.namerpro.nchat.domain.api.repository.SecretKeyRepository
import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.domain.model.SecretKey
import java.math.BigInteger
import java.util.Base64

class SecretKeyRepositoryImpl(
    private val networkClient: NetworkClient
) : SecretKeyRepository {

    override suspend fun sendPartOfKey(
        receiverId: Long,
        chatId: Long,
        partOfKey: String
    ): Resource<Unit> {
        val response = networkClient.sendPartOfKey(receiverId, chatId, partOfKey)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success()
        } else {
            Resource.Error(response.responseCode)
        }
    }

    override suspend fun getPatsOfKeys(
        clientId: Long
    ): Resource<List<SecretKey>> {
        val response = networkClient.getPartsOfKeys(clientId)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success(
                (response as GetPartsOfKeysResponse).partsOfKeys.map {
                    SecretKey(
                        it.chatId,
                        BigInteger(
                            Base64.getDecoder().decode(
                                it.partOfKey.substring(1, it.partOfKey.length - 1)
                            )
                        )
                    )
                }
            )
        } else {
            Resource.Error(response.responseCode)
        }
    }

}