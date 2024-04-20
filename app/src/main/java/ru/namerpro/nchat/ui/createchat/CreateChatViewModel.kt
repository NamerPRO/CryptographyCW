package ru.namerpro.nchat.ui.createchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.namerpro.nchat.commons.Constants.Companion.DIFFIE_HELLMAN_CONSTANT_G
import ru.namerpro.nchat.commons.Constants.Companion.DIFFIE_HELLMAN_CONSTANT_P
import ru.namerpro.nchat.commons.Constants.Companion.PING_DELAY_MS
import ru.namerpro.nchat.commons.SingleLiveEvent
import ru.namerpro.nchat.domain.api.interactor.ChatManagerInteractor
import ru.namerpro.nchat.domain.api.interactor.InitializedClientsInteractor
import ru.namerpro.nchat.domain.model.ClientModel
import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.domain.model.State
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CHAT_DATA
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_ID
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS

class CreateChatViewModel(
    private val initializedClientsInteractor: InitializedClientsInteractor,
    private val chatManagerInteractor: ChatManagerInteractor
) : ViewModel() {

    private val chatCreationStateLiveData = SingleLiveEvent<ChatCreationState>()
    fun observeChatCreationState(): LiveData<ChatCreationState> = chatCreationStateLiveData

    var initializedClients: List<ClientModel>? = null

    var chatPreparationState: State<Triple<String, String, String>>? = null

    fun getInitializedClients() {
        viewModelScope.launch {
            val clientsResource = initializedClientsInteractor.getInitializedClients()
            if (clientsResource is Resource.Success) {
                if (clientsResource.data.isNullOrEmpty()) {
                    initializedClients = null
                    chatCreationStateLiveData.postValue(ChatCreationState.InitializedClientsRequestSuccess(emptyList()))
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

    private fun getClientIdByName(
        clientName: String
    ) = initializedClients?.find {
        it.name.takeWhile { i ->
            i != ' '
        } == clientName
    }?.id ?: -1

    fun createChat(
        chatName: String,
        partnerName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val partnerId = getClientIdByName(partnerName)
            val chatIdResource = chatManagerInteractor.createChat(CLIENT_ID, partnerId)
            if (chatIdResource is Resource.Success) {
                val chatId = chatIdResource.data!!
                val secret = (15..25).random()
                CHAT_DATA[chatId] = Triple(chatName, partnerName, null)
                CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS[chatId] = secret
                val toSend = DIFFIE_HELLMAN_CONSTANT_G.pow(secret).mod(DIFFIE_HELLMAN_CONSTANT_P)
                while (true) {
                    val addNewChatResource = chatManagerInteractor.addNewChat(CLIENT_ID, partnerId, chatId, chatName, toSend)
                    if (addNewChatResource !is Resource.Success) delay(PING_DELAY_MS) else break
                }
            } else {
                chatCreationStateLiveData.postValue(ChatCreationState.FailedToCreateChat)
            }
        }
    }

}