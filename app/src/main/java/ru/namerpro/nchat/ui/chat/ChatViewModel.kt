package ru.namerpro.nchat.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    private val chatLiveData = MutableLiveData<ChatState>()
    fun observeChat(): LiveData<ChatState> = chatLiveData

}