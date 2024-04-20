package ru.namerpro.nchat.data.impl

import ru.namerpro.nchat.commons.Constants.Companion.SUCCESS_RESPONSE_CODE
import ru.namerpro.nchat.data.NetworkClient
import ru.namerpro.nchat.data.dto.response.InitializeResponse
import ru.namerpro.nchat.data.dto.response.InitializedClientsResponse
import ru.namerpro.nchat.data.dto.response.IsClientInitializedResponse
import ru.namerpro.nchat.domain.api.repository.InitializedClientsRepository
import ru.namerpro.nchat.domain.model.ClientModel
import ru.namerpro.nchat.domain.model.Resource

class InitializedClientsRepositoryImpl(
    private val networkClient: NetworkClient
) : InitializedClientsRepository {

    override suspend fun getInitializedClients(): Resource<List<ClientModel>> {
        val response = networkClient.getInitializedClients()
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success(
                (response as InitializedClientsResponse).clients.map { ClientModel(it.first, it.second) }.toList()
            )
        } else {
            Resource.Error(response.responseCode)
        }
    }

    override suspend fun isInitialized(
        clientId: Long
    ): Resource<Boolean> {
        val response = networkClient.isInitialized(clientId)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success(
                (response as IsClientInitializedResponse).isInitialized
            )
        } else {
            Resource.Error(response.responseCode)
        }
    }

    override suspend fun initialize(
        clientName: String
    ): Resource<Long> {
        val response = networkClient.initialize(clientName)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success(
                (response as InitializeResponse).clientId
            )
        } else {
            Resource.Error(response.responseCode)
        }
    }

}