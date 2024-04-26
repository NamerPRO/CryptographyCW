package ru.namerpro.nchat.ui.chatlist

import ru.namerpro.nchat.domain.model.Chat

sealed interface ChatListState {

    data class UpdateChatList(
        val chats: List<Chat>
    ) : ChatListState

}