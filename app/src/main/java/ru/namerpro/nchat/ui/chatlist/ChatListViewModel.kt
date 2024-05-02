package ru.namerpro.nchat.ui.chatlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.namerpro.nchat.commons.Constants.Companion.FIELD_NOT_INITIALIZED
import ru.namerpro.nchat.commons.Constants.Companion.PING_DELAY_MS
import ru.namerpro.nchat.commons.SingleLiveEvent
import ru.namerpro.nchat.domain.api.interactor.ChatsDatabaseInteractor
import ru.namerpro.nchat.domain.model.Chat
import ru.namerpro.nchat.ui.root.RootViewModel
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_ID

class ChatListViewModel(
    private val chatsDatabaseInteractor: ChatsDatabaseInteractor
) : ViewModel() {

    init {
        pingChats()
    }

    private val chatsStateLiveData = SingleLiveEvent<ChatListState>()
    fun observeAvailableChats(): LiveData<ChatListState> = chatsStateLiveData

    private fun pingChats() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (CLIENT_ID != FIELD_NOT_INITIALIZED) {
                    val iterator = RootViewModel.CHAT_DATA.iterator()
                    val createdChats = mutableListOf<Chat>()
                    while (iterator.hasNext()) {
                        val data = iterator.next()
                        if (data.value.chatName != null && data.value.partnerName != null
                                && data.value.cipher != null && data.value.secretKey != null
                                    && data.value.iv != null) {
                            createdChats.add(
                                Chat(
                                    data.key,
                                    data.value.chatName!!,
                                    data.value.partnerName!!,
                                    data.value.cipher!!,
                                    data.value.secretKey!!,
                                    data.value.iv!!
                                )
                            )
                            iterator.remove()
                        }
                    }
                    if (createdChats.isNotEmpty()) {
                        chatsStateLiveData.postValue(ChatListState.UpdateChatList(createdChats))
                    }
                    delay(PING_DELAY_MS)
                }
            }
        }
    }

    fun getChatsFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            val chats = chatsDatabaseInteractor.getChats()
                .sortedByDescending { it.id }
            chatsStateLiveData.postValue(ChatListState.ChatsRestoreFromDb(chats))
        }
    }

    fun addChatsToDb(
        chats: List<Chat>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            chats.forEach {
                chatsDatabaseInteractor.addChat(it)
            }
        }
    }

}