package ru.namerpro.nchat.ui.root

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.namerpro.nchat.commons.Constants
import ru.namerpro.nchat.commons.Constants.Companion.DIFFIE_HELLMAN_CONSTANT_G
import ru.namerpro.nchat.commons.Constants.Companion.DIFFIE_HELLMAN_CONSTANT_P
import ru.namerpro.nchat.commons.Constants.Companion.FIELD_NOT_INITIALIZED
import ru.namerpro.nchat.commons.Constants.Companion.STANDARD_AMOUNT_OF_ATTEMPTS
import ru.namerpro.nchat.commons.Constants.Companion.STANDARD_KEY_SIZE_IN_BYTES
import ru.namerpro.nchat.commons.SingleLiveEvent
import ru.namerpro.nchat.domain.api.interactor.ChatManagerInteractor
import ru.namerpro.nchat.domain.api.interactor.InitializedClientsInteractor
import ru.namerpro.nchat.domain.api.interactor.SecretKeyInteractor
import ru.namerpro.nchat.domain.model.NetworkResponse
import ru.namerpro.nchat.domain.model.Resource

class RootViewModel(
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
        CLIENT_NAME = clientName
        viewModelScope.launch {
            val clientIdResource = initializedClientsInteractor.initialize(clientName)
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
                            // it = { chat id + chat name } + { partner id + partner name } + part of key
                            val secret = (15..25).random()
                            val key = it.third
                                .pow(secret)
                                .mod(DIFFIE_HELLMAN_CONSTANT_P)
                                .toByteArray()
                                .takeLast(STANDARD_KEY_SIZE_IN_BYTES)
                                .toByteArray()
                            CHAT_DATA[it.first.first] = Triple(it.first.second, it.second.second, key)
                            val toSend = DIFFIE_HELLMAN_CONSTANT_G.pow(secret).mod(DIFFIE_HELLMAN_CONSTANT_P)
                            secretKeyInteractor.sendPartOfKey(it.second.first, it.first.first, toSend)
                        }
                        pingNewChatAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS
                    } else {
                        --pingNewChatAttemptsLeftBeforeWarningShot
                        if (pingNewChatAttemptsLeftBeforeWarningShot == 0) {
                            pingNewChatAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS
                            applicationStateLiveData.postValue(ApplicationState.FailedToPingNewChats)
                        }
                    }
                    delay(Constants.PING_DELAY_MS)
                }
            }
        }
    }

    private fun pingSecretKeys() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (CLIENT_ID != FIELD_NOT_INITIALIZED) {
                    val secretKeysResource = secretKeyInteractor.getPatsOfKeys(CLIENT_ID)
                    Log.e("123", secretKeysResource.data.toString())
                    Log.e("123", (secretKeysResource is Resource.Success).toString())
                    if (secretKeysResource is Resource.Success) {
                        val secretKeys = secretKeysResource.data
                        secretKeys?.forEach {
                            // it = chat id + part of key
                            val clientSecretConstant = CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS[it.first]!!
                            val secret = it.second
                                .pow(clientSecretConstant)
                                .toByteArray()
                                .take(STANDARD_KEY_SIZE_IN_BYTES)
                                .toByteArray()
                            CHAT_DATA[it.first] = Triple(CHAT_DATA[it.first]?.first, CHAT_DATA[it.first]?.second, secret)
                            CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS.remove(it.first)
                        }
                        pingPartsOfKeysAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS
                    } else {
                        --pingPartsOfKeysAttemptsLeftBeforeWarningShot
                        if (pingPartsOfKeysAttemptsLeftBeforeWarningShot == 0) {
                            pingPartsOfKeysAttemptsLeftBeforeWarningShot = STANDARD_AMOUNT_OF_ATTEMPTS
                            applicationStateLiveData.postValue(ApplicationState.FailedToGetSecretKeys)
                        }
                    }
                    delay(Constants.PING_DELAY_MS)
                }
            }
        }
    }

    companion object {
        var CLIENT_ID: Long = FIELD_NOT_INITIALIZED
        var CLIENT_NAME: String = ""

        // chat id -> chat name + partner name + secret key
        val CHAT_DATA = hashMapOf<Long, Triple<String?, String?, ByteArray?>>()
        val CLIENT_SECRET_DIFFIE_HELLMAN_CONSTANTS = hashMapOf<Long, Int>()
    }

}