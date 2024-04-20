package ru.namerpro.nchat.ui.chatlist

import ru.namerpro.nchat.domain.model.ChatModel

sealed interface ChatListState {

    data class UpdateChatList(
        val chats: List<ChatModel>
    ) : ChatListState

}