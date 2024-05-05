package ru.namerpro.nchat.ui.chat

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.namerpro.nchat.commons.Constants.Companion.ENCRYPTED_FILE_PREFIX
import ru.namerpro.nchat.commons.Constants.Companion.PING_DELAY_MS
import ru.namerpro.nchat.commons.QueuedLiveData
import ru.namerpro.nchat.commons.getFileName
import ru.namerpro.nchat.commons.getFileType
import ru.namerpro.nchat.commons.parentPath
import ru.namerpro.nchat.commons.saveFileInCache
import ru.namerpro.nchat.domain.api.interactor.ChatManagerInteractor
import ru.namerpro.nchat.domain.api.interactor.ChatsDatabaseInteractor
import ru.namerpro.nchat.domain.api.interactor.MessagesDatabaseInteractor
import ru.namerpro.nchat.domain.api.interactor.MessagesInteractor
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.domain.model.Task
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_ID
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

class ChatViewModel(
    private val chatsDatabaseInteractor: ChatsDatabaseInteractor,
    private val messagesInteractor: MessagesInteractor,
    private val messagesDatabaseInteractor: MessagesDatabaseInteractor,
    private val chatManagerInteractor: ChatManagerInteractor,
    private val application: Application
) : ViewModel() {

    private val chatLiveData = QueuedLiveData<ChatState>()
    fun observeChat(): LiveData<ChatState> = chatLiveData

    var sendingFilesCounter = 0

    var isChatAlive = true

    private var attemptsBeforeFailedToGetMessageLeft = STANDARD_ATTEMPTS_NUMBER

    fun sendMessage(
        chatId: Long,
        message: Message.Data,
        encrypter: SymmetricEncrypterContext?
    ) {
        viewModelScope.safeLaunch(Dispatchers.Default) {
            val resource = messagesInteractor.sendTextMessage(CLIENT_ID, chatId, message, encrypter!!)
            if (resource is Resource.Success) {
                val messageToSender = message.copy(isReceived = false)
                chatLiveData.postValue(ChatState.SuccessfullySendMessage(messageToSender))
            } else {
                chatLiveData.postValue(ChatState.FailedToSendMessage)
            }
        }
    }

    fun sendFile(
        chatId: Long,
        message: Message.File,
        encrypter: SymmetricEncrypterContext?,
        onProgressChange: (Double) -> Unit
    ) {
        viewModelScope.safeLaunch(Dispatchers.IO) {
            try {
                message.task = Task(onProgressChange, false)
                val fileServerName = "${UUID.randomUUID()}.${getFileType(message.realName)}"
                val cachedFile = saveFileInCache(fileServerName, message.file?.second, application)
                message.file = Pair(message.file!!.first, cachedFile.inputStream())
                message.devicePath = cachedFile.path
                val resource = messagesInteractor.uploadEncryptedFile(CLIENT_ID, chatId, message, encrypter!!, message.task!!)
                if (resource is Resource.Success) {
                    Files.delete(Paths.get("${parentPath(message.devicePath)}${ENCRYPTED_FILE_PREFIX}${getFileName(message.devicePath)}"))
                    chatLiveData.postValue(ChatState.SuccessfullySendFile(message))
                } else {
                    chatLiveData.postValue(ChatState.FileNotSent(message))
                }
            } catch (_: Throwable) {
                chatLiveData.postValue(ChatState.FileNotSent(message))
            }
        }
    }

    fun getMessages(
        chatId: Long,
        encrypter: SymmetricEncrypterContext?
    ) {
        viewModelScope.safeLaunch(Dispatchers.Default) {
            while (isChatAlive) {
                val messagesResource = messagesInteractor.getMessages(CLIENT_ID, chatId, encrypter!!)
                if (messagesResource is Resource.Success) {
                    attemptsBeforeFailedToGetMessageLeft = STANDARD_ATTEMPTS_NUMBER
                    val messages = messagesResource.data!!
                    chatLiveData.postValue(ChatState.SuccessfullyGetMessages(messages))
                } else {
                    if (attemptsBeforeFailedToGetMessageLeft == 0) {
                        attemptsBeforeFailedToGetMessageLeft = STANDARD_ATTEMPTS_NUMBER
                        chatLiveData.postValue(ChatState.FailedToGetMessages)
                    } else {
                        --attemptsBeforeFailedToGetMessageLeft
                    }
                }
                delay(PING_DELAY_MS)
            }
        }
    }

    fun removeTempFiles(
        fileNames: List<Message>
    ) {
        fileNames.forEach {
            if (it is Message.File) {
                Files.delete(Paths.get("${application.applicationContext.externalCacheDir}${File.separator}${ENCRYPTED_FILE_PREFIX}${getFileName(it.devicePath)}"))
            }
        }
    }

    fun downloadFile(
        message: Message.File,
        pathToFolder: String,
        encrypter: SymmetricEncrypterContext?,
        onProgressChange: (Double) -> Unit
    ) {
        viewModelScope.safeLaunch(Dispatchers.IO) {
            try {
                message.task = Task(onProgressChange, false)
                val resource = messagesInteractor.downloadFile(message, pathToFolder, encrypter!!, message.task!!)
                if (resource is Resource.Success) {
                    chatLiveData.postValue(ChatState.SuccessfullyDownloadedFile(message))
                } else {
                    chatLiveData.postValue(ChatState.FileNotReceived(message))
                }
            } catch (_: Throwable) {
                chatLiveData.postValue(ChatState.FileNotReceived(message))
            }
        }
    }

    fun downloadOnClick(
        message: Message.File,
        pathToFolder: String,
        encrypter: SymmetricEncrypterContext?,
        onProgressChange: (Double) -> Unit
    ) {
        viewModelScope.safeLaunch {
            message.task = Task(onProgressChange, false)
            if (Files.exists(Paths.get("${pathToFolder}${getFileName(message.devicePath)}"))) {
                chatLiveData.postValue(ChatState.FileAlreadyDownloadedOnClick)
            } else {
                val resource = messagesInteractor.downloadFile(message, pathToFolder, encrypter!!, message.task!!)
                if (message.task?.isCancelled == true || resource is Resource.Error) {
                    chatLiveData.postValue(ChatState.FileDownloadFailedOnClick)
                }
            }
        }
    }

    fun addMessagesToDataBase(
        chatId: Long,
        message: List<Message>,
        addEndChat: Boolean
    ) {
        viewModelScope.safeLaunch {
            message.forEach {
                if (addEndChat || it !is Message.ChatEnd) {
                    messagesDatabaseInteractor.addMessage(chatId, it)
                }
            }
        }
    }

    fun markChatNotAlive(
        chatId: Long
    ) {
        isChatAlive = false
        viewModelScope.safeLaunch {
            chatsDatabaseInteractor.updateAliveState(chatId, false)
        }
    }

    fun getMessagesFromDatabase(
        chatId: Long
    ) {
        viewModelScope.safeLaunch {
            val messages = messagesDatabaseInteractor.getMessages(chatId)
            chatLiveData.postValue(ChatState.MessagesSuccessfullyLoadedFromDb(messages))
        }
    }

    fun leaveChat(
        chatId: Long,
        clientId: Long
    ) {
        viewModelScope.safeLaunch {
            isChatAlive = false
            chatManagerInteractor.leaveChat(clientId, chatId)
            chatLiveData.postValue(ChatState.SuccessfullyLeavedChat)
        }
    }

    private fun CoroutineScope.safeLaunch(
        coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend CoroutineScope.() -> Unit
    ): Job = launch(coroutineDispatcher) {
        try {
            block()
        } catch (err: Throwable) { throw err } /* EMPTY */
    }

    companion object {
        const val STANDARD_ATTEMPTS_NUMBER = 10
    }

}