package ru.namerpro.nchat.ui.createchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.namerpro.nchat.commons.Constants.Companion.DIFFIE_HELLMAN_CONSTANT_G
import ru.namerpro.nchat.commons.Constants.Companion.DIFFIE_HELLMAN_CONSTANT_P
import ru.namerpro.nchat.commons.Constants.Companion.STANDARD_KEY_SIZE_IN_BYTES
import ru.namerpro.nchat.commons.SingleLiveEvent
import ru.namerpro.nchat.domain.api.interactor.ChatManagerInteractor
import ru.namerpro.nchat.domain.api.interactor.InitializedClientsInteractor
import ru.namerpro.nchat.domain.model.ChatInfo
import ru.namerpro.nchat.domain.model.Cipher
import ru.namerpro.nchat.domain.model.Client
import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.domain.model.WeakChat
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CHAT_DATA
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_ID
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS
import kotlin.random.Random

class CreateChatViewModel(
    private val initializedClientsInteractor: InitializedClientsInteractor,
    private val chatManagerInteractor: ChatManagerInteractor
) : ViewModel() {

    private val chatCreationStateLiveData = SingleLiveEvent<ChatCreationState>()
    fun observeChatCreationState(): LiveData<ChatCreationState> = chatCreationStateLiveData

    var initializedClients: MutableList<Client>? = null

    fun getInitializedClients() {
        viewModelScope.launch {
            val clientsResource = initializedClientsInteractor.getInitializedClients()
            if (clientsResource is Resource.Success) {
                if (clientsResource.data.isNullOrEmpty()) {
                    initializedClients = null
                    chatCreationStateLiveData.postValue(ChatCreationState.InitializedClientsRequestSuccess(mutableListOf()))
                } else {
                    initializedClients = clientsResource.data
                    chatCreationStateLiveData.postValue(ChatCreationState.InitializedClientsRequestSuccess(clientsResource.data))
                }
            } else {
                initializedClients = null
                chatCreationStateLiveData.postValue(ChatCreationState.InitializedClientsRequestFailed)
            }
        }
    }

    fun createChat(
        chatName: String,
        partnerId: Long,
        partnerName: String,
        cipherType: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val secret = (15..25).random()
            val toSend = DIFFIE_HELLMAN_CONSTANT_G.pow(secret).mod(DIFFIE_HELLMAN_CONSTANT_P)
            val iv = Random.Default.nextBytes(STANDARD_KEY_SIZE_IN_BYTES)
            val chatIdResource = chatManagerInteractor.createChat(CLIENT_ID, partnerId, ChatInfo(chatName, cipherType, toSend, iv))
            if (chatIdResource is Resource.Success) {
                val chatId = chatIdResource.data!!
                CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS[chatId] = secret
                CHAT_DATA[chatId] = WeakChat(chatName, partnerName, CHAT_DATA[chatId]?.secretKey, Cipher.fromString(cipherType), iv)
            } else {
                chatCreationStateLiveData.postValue(ChatCreationState.FailedToCreateChat)
            }
        }
    }

}