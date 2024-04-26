package ru.namerpro.nchat.ui.createchat

import ru.namerpro.nchat.domain.model.Client

sealed interface ChatCreationState {

    data class InitializedClientsRequestSuccess(
        val clients: MutableList<Client>
    ) : ChatCreationState

    data object InitializedClientsRequestFailed: ChatCreationState

    data object FailedToAddNewChat: ChatCreationState

    data object FailedToCreateChat: ChatCreationState

    data object FailedToGetNewChats: ChatCreationState

}