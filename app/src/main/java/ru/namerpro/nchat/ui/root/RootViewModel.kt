package ru.namerpro.nchat.ui.root

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.namerpro.nchat.commons.Constants.Companion.DIFFIE_HELLMAN_CONSTANT_G
import ru.namerpro.nchat.commons.Constants.Companion.DIFFIE_HELLMAN_CONSTANT_P
import ru.namerpro.nchat.commons.Constants.Companion.FIELD_NOT_INITIALIZED
import ru.namerpro.nchat.commons.Constants.Companion.PING_DELAY_MS
import ru.namerpro.nchat.commons.Constants.Companion.STANDARD_AMOUNT_OF_ATTEMPTS
import ru.namerpro.nchat.commons.Constants.Companion.STANDARD_KEY_SIZE_IN_BYTES
import ru.namerpro.nchat.commons.SingleLiveEvent
import ru.namerpro.nchat.domain.api.interactor.ChatManagerInteractor
import ru.namerpro.nchat.domain.api.interactor.ChatsDatabaseInteractor
import ru.namerpro.nchat.domain.api.interactor.InitializedClientsInteractor
import ru.namerpro.nchat.domain.api.interactor.MessagesDatabaseInteractor
import ru.namerpro.nchat.domain.api.interactor.SecretKeyInteractor
import ru.namerpro.nchat.domain.model.Chat
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.NetworkResponse
import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.domain.model.WeakChat

class RootViewModel(
    private val chatsDatabaseInteractor: ChatsDatabaseInteractor,
    private val messagesDatabaseInteractor: MessagesDatabaseInteractor,
    private val chatManagerInteractor: ChatManagerInteractor,
    private val secretKeyInteractor: SecretKeyInteractor,
    private val initializedClientsInteractor: InitializedClientsInteractor
) : ViewModel() {

    init {
        pingNewChats()
        pingSecretKeys()
    }

    private val applicationStateLiveData = SingleLiveEvent<ApplicationState>()
    fun observeApplicationState(): LiveData<ApplicationState> = applicationStateLiveData

    private var pingNewChatAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS

    private var pingPartsOfKeysAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS

    fun initializeClient(
        clientName: String
    ) {
        CLIENT_NAME = clientName.trim()
        viewModelScope.launch {
            val clientIdResource = initializedClientsInteractor.initialize(CLIENT_NAME)
            if (clientIdResource is Resource.Success && clientIdResource.data != -1L) {
                CLIENT_ID = clientIdResource.data!!
                applicationStateLiveData.postValue(ApplicationState.ClientInitializationSuccess)
            } else if (clientIdResource.code == NetworkResponse.CONFLICT.code) {
                applicationStateLiveData.postValue(ApplicationState.IncorrectNameProvided)
            } else {
                applicationStateLiveData.postValue(ApplicationState.ClientInitializationFailed)
            }
        }
    }

    private fun pingNewChats() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (CLIENT_ID != FIELD_NOT_INITIALIZED) {
                    val newChatsResource = chatManagerInteractor.getNewChats(CLIENT_ID)
                    if (newChatsResource is Resource.Success) {
                        val newChats = newChatsResource.data
                        newChats?.forEach {
                            val secret = (15..25).random()
                            val key = it.secret
                                .pow(secret)
                                .mod(DIFFIE_HELLMAN_CONSTANT_P)
                                .toByteArray()
                                .takeLast(STANDARD_KEY_SIZE_IN_BYTES)
                                .toByteArray()
                            CHAT_DATA[it.chatId] = WeakChat(it.chatName, it.partnerName, key, it.cipherType, it.iv)
                            val toSend = DIFFIE_HELLMAN_CONSTANT_G.pow(secret).mod(DIFFIE_HELLMAN_CONSTANT_P)
                            secretKeyInteractor.sendPartOfKey(it.partnerId, it.chatId, toSend)
                        }
                        pingNewChatAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS
                    } else {
                        --pingNewChatAttemptsLeftBeforeWarningShot
                        if (pingNewChatAttemptsLeftBeforeWarningShot == 0) {
                            pingNewChatAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS
                            applicationStateLiveData.postValue(ApplicationState.FailedToPingNewChats)
                        }
                    }
                }
                delay(PING_DELAY_MS)
            }
        }
    }

    private fun pingSecretKeys() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (CLIENT_ID != FIELD_NOT_INITIALIZED) {
                    val secretKeysResource = secretKeyInteractor.getPatsOfKeys(CLIENT_ID)
                    if (secretKeysResource is Resource.Success) {
                        val secretKeys = secretKeysResource.data
                        secretKeys?.forEach {
                            val chatId: Long = it.chatId
                            while (CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS[chatId] == null) {
                                delay(PING_DELAY_MS)
                            }
                            val clientSecretConstant = CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS[chatId]!!
                            val secret = it.partOfKey
                                .pow(clientSecretConstant)
                                .mod(DIFFIE_HELLMAN_CONSTANT_P)
                                .toByteArray()
                                .takeLast(STANDARD_KEY_SIZE_IN_BYTES)
                                .toByteArray()
                            CHAT_DATA[chatId] = WeakChat(CHAT_DATA[chatId]?.chatName, CHAT_DATA[chatId]?.partnerName, secret, CHAT_DATA[chatId]?.cipher, CHAT_DATA[chatId]?.iv)
                            CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS.remove(chatId)
                        }
                        pingPartsOfKeysAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS
                    } else {
                        --pingPartsOfKeysAttemptsLeftBeforeWarningShot
                        if (pingPartsOfKeysAttemptsLeftBeforeWarningShot == 0) {
                            pingPartsOfKeysAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS
                            applicationStateLiveData.postValue(ApplicationState.FailedToGetSecretKeys)
                        }
                    }
                }
                delay(PING_DELAY_MS)
            }
        }
    }

    fun leaveAllChats() {
        runBlocking(Dispatchers.IO) {
            READY_CHATS.forEach {
                if (it.isAlive) {
                    it.isAlive = false
                    chatManagerInteractor.leaveChat(CLIENT_ID, it.id)
                    messagesDatabaseInteractor.addMessage(it.id, Message.ChatEnd(System.currentTimeMillis()))
                    chatsDatabaseInteractor.updateAliveState(it.id, it.isAlive)
                }
            }
            initializedClientsInteractor.deinitialize(CLIENT_ID)
        }
    }

    companion object {
        var CLIENT_ID: Long = FIELD_NOT_INITIALIZED
        var CLIENT_NAME: String = ""

        val CHAT_DATA = hashMapOf<Long, WeakChat>()
        val CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS = hashMapOf<Long, Int>()

        var DOWNLOAD_NOTIFICATION_ID = 0

        val READY_CHATS = arrayListOf<Chat>()

        var CAN_EXIT = true
    }

}