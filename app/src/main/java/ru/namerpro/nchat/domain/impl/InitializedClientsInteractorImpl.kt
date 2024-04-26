package ru.namerpro.nchat.domain.impl

import ru.namerpro.nchat.domain.api.interactor.InitializedClientsInteractor
import ru.namerpro.nchat.domain.api.repository.InitializedClientsRepository
import ru.namerpro.nchat.domain.model.Client
import ru.namerpro.nchat.domain.model.Resource

class InitializedClientsInteractorImpl(
    private val initializedClientsRepository: InitializedClientsRepository
) : InitializedClientsInteractor {

    override suspend fun getInitializedClients(): Resource<MutableList<Client>> {
        return initializedClientsRepository.getInitializedClients()
    }

    override suspend fun isInitialized(
        clientId: Long
    ): Resource<Boolean> {
        return initializedClientsRepository.isInitialized(clientId)
    }

    override suspend fun initialize(
        clientName: String
    ): Resource<Long> {
        return initializedClientsRepository.initialize(clientName)
    }

}