package ru.namerpro.nchat.domain.api.interactor

import ru.namerpro.nchat.domain.model.Client
import ru.namerpro.nchat.domain.model.Resource

interface InitializedClientsInteractor {

    suspend fun getInitializedClients(): Resource<MutableList<Client>>

    suspend fun isInitialized(
        clientId: Long
    ): Resource<Boolean>

    suspend fun initialize(
        clientName: String
    ): Resource<Long>

    suspend fun deinitialize(
        clientId: Long
    ): Resource<Unit>

}