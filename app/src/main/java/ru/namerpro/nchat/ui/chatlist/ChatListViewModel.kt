package ru.namerpro.nchat.ui.chatlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.namerpro.nchat.commons.Constants.Companion.PING_DELAY_MS
import ru.namerpro.nchat.domain.model.ChatModel
import ru.namerpro.nchat.ui.root.RootViewModel

class ChatListViewModel() : ViewModel() {

    init {
        pingChats()
    }

    private val chatsStateLiveData = MutableLiveData<ChatListState>()
    fun observeAvailableChats(): LiveData<ChatListState> = chatsStateLiveData

    private fun pingChats() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val chatsList = RootViewModel.CHAT_DATA
                    .filter { it.value.first != null && it.value.second != null && it.value.third != null }
                    .map { ChatModel(it.key, it.value.first!!, it.value.second!!, it.value.third!!) }
                    .sortedByDescending { it.id }
                chatsStateLiveData.postValue(ChatListState.UpdateChatList(chatsList))
                delay(PING_DELAY_MS)
            }
        }
    }

}