package ru.namerpro.nchat.ui.chatlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.namerpro.nchat.commons.Constants.Companion.FIELD_NOT_INITIALIZED
import ru.namerpro.nchat.commons.Constants.Companion.PING_DELAY_MS
import ru.namerpro.nchat.domain.model.Chat
import ru.namerpro.nchat.ui.root.RootViewModel
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_ID

class ChatListViewModel : ViewModel() {

    init {
        pingChats()
    }

    private val chatsStateLiveData = MutableLiveData<ChatListState>()
    fun observeAvailableChats(): LiveData<ChatListState> = chatsStateLiveData

    private fun pingChats() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (CLIENT_ID != FIELD_NOT_INITIALIZED) {
                    val chatsList = RootViewModel.CHAT_DATA
                        .filter { it.value.chatName != null && it.value.partnerName != null && it.value.cipher != null && it.value.secretKey != null && it.value.iv != null }
                        .map { Chat(it.key, it.value.chatName!!, it.value.partnerName!!, it.value.cipher!!, it.value.secretKey!!, it.value.iv!!) }
                        .sortedByDescending { it.id }
                    chatsStateLiveData.postValue(ChatListState.UpdateChatList(chatsList))
                    delay(PING_DELAY_MS)
                }
            }
        }
    }

}