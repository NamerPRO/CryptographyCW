package ru.namerpro.nchat.domain.api.interactor

import ru.namerpro.nchat.domain.model.ClientModel
import ru.namerpro.nchat.domain.model.Resource

interface InitializedClientsInteractor {

    suspend fun getInitializedClients(): Resource<List<ClientModel>>

    suspend fun isInitialized(
        clientId: Long
    ): Resource<Boolean>

    suspend fun initialize(
        clientName: String
    ): Resource<Long>

}