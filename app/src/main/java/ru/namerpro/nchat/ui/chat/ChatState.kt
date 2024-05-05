package ru.namerpro.nchat.ui.chat

import ru.namerpro.nchat.domain.model.Message

sealed interface ChatState {

    data class SuccessfullySendFile(
        val fileMessage: Message.File
    ) : ChatState

    data class FileNotSent(
        val fileMessage: Message.File
    ) : ChatState

    data class SuccessfullySendMessage(
        val message: Message.Data
    ) : ChatState

    data class SuccessfullyGetMessages(
        val messages: List<Message>
    ) : ChatState

    data class SuccessfullyDownloadedFile(
        val fileMessage: Message.File
    ) : ChatState

    data class FileNotReceived(
        val fileMessage: Message.File
    ) : ChatState

    data object FailedToSendMessage : ChatState

    data object FailedToGetMessages : ChatState

    data object FileAlreadyDownloadedOnClick : ChatState

    data object FileDownloadFailedOnClick : ChatState

    data class MessagesSuccessfullyLoadedFromDb(
        val messages: List<Message>
    ) : ChatState

    data object SuccessfullyLeavedChat : ChatState

}