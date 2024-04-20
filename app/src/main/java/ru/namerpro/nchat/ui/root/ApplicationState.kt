package ru.namerpro.nchat.ui.root

sealed interface ApplicationState {

    data object FailedToPingNewChats : ApplicationState

    data object FailedToGetSecretKeys : ApplicationState

    data object ClientInitializationSuccess : ApplicationState

    data object ClientInitializationFailed : ApplicationState

    data object IncorrectNameProvided : ApplicationState

}