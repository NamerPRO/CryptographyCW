package ru.namerpro.nchat.ui.chat

import ru.namerpro.nchat.domain.model.Message

sealed interface ChatState {

    data object FailedToSendMessage : ChatState

    data object SuccessfullySendMessage : ChatState

    data class SuccessfullySendFile(
        val message: Message.File
    ) : ChatState

    data object FailedToGetMessages : ChatState

    data class SuccessfullyGetMessages(
        val messages: List<Message>
    ) : ChatState

}