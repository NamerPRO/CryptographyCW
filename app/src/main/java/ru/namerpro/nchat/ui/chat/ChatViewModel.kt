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
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import ru.namerpro.nchat.commons.Constants.Companion.ENCRYPTED_FILE_PREFIX
import ru.namerpro.nchat.commons.Constants.Companion.PING_DELAY_MS
import ru.namerpro.nchat.commons.QueuedLiveData
import ru.namerpro.nchat.commons.getFileName
import ru.namerpro.nchat.commons.getFileType
import ru.namerpro.nchat.commons.parentPath
import ru.namerpro.nchat.commons.saveFileInCache
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
    private val messagesInteractor: MessagesInteractor,
    private val messagesDatabaseInteractor: MessagesDatabaseInteractor,
    private val application: Application
) : ViewModel() {

    private val chatLiveData = QueuedLiveData<ChatState>()
    fun observeChat(): LiveData<ChatState> = chatLiveData

    var sendingFilesCounter = 0

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
                message.coroutineScope = this
                val fileServerName = "${UUID.randomUUID()}.${getFileType(message.realName)}"
                val cachedFile = saveFileInCache(fileServerName, message.file?.second, application)
                message.file = Pair(message.file!!.first, cachedFile.inputStream())
                message.devicePath = cachedFile.path
                val resource = messagesInteractor.uploadEncryptedFile(CLIENT_ID, chatId, message, encrypter!!, Task(onProgressChange, this))
                ensureActive()
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
            while (true) {
                val messagesResource = messagesInteractor.getMessages(CLIENT_ID, chatId, encrypter!!)
                if (messagesResource is Resource.Success) {
                    val messages = messagesResource.data!!
                    chatLiveData.postValue(ChatState.SuccessfullyGetMessages(messages))
                } else {
                    chatLiveData.postValue(ChatState.FailedToGetMessages)
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
                message.coroutineScope = this
                val resource = messagesInteractor.downloadFile(message, pathToFolder, encrypter!!, Task(onProgressChange, this))
                ensureActive()
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
            message.coroutineScope = this
            if (Files.exists(Paths.get("${pathToFolder}${getFileName(message.devicePath)}"))) {
                chatLiveData.postValue(ChatState.FileAlreadyDownloadedOnClick)
            } else {
                val resource = messagesInteractor.downloadFile(message, pathToFolder, encrypter!!, Task(onProgressChange, this))
                ensureActive()
                if (resource is Resource.Error) {
                    chatLiveData.postValue(ChatState.FileDownloadFailedOnClick)
                }
            }
        }
    }

    fun addMessagesToDataBase(
        chatId: Long,
        message: List<Message>
    ) {
        viewModelScope.safeLaunch {
            message.forEach {
                messagesDatabaseInteractor.addMessage(chatId, it)
            }
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

    private fun CoroutineScope.safeLaunch(
        coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend CoroutineScope.() -> Unit
    ): Job = launch(coroutineDispatcher) {
        try {
            block()
        } catch (err: Throwable) { /* EMPTY */ }
    }

}