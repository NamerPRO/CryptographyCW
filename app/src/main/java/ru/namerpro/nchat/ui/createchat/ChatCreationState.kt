package ru.namerpro.nchat.ui.createchat

import ru.namerpro.nchat.domain.model.ClientModel

sealed interface ChatCreationState {

    data class InitializedClientsRequestSuccess(
        val clients: List<ClientModel>
    ) : ChatCreationState

    data object InitializedClientsRequestFailed: ChatCreationState

    data object FailedToAddNewChat: ChatCreationState

    data object FailedToCreateChat: ChatCreationState

    data object FailedToGetNewChats: ChatCreationState

}