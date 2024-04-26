package ru.namerpro.nchat.ui.chat

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.namerpro.nchat.commons.Constants.Companion.ENCRYPTED_FILE_PREFIX
import ru.namerpro.nchat.commons.Constants.Companion.PING_DELAY_MS
import ru.namerpro.nchat.commons.getContentType
import ru.namerpro.nchat.commons.saveFileInCache
import ru.namerpro.nchat.domain.api.interactor.MessagesInteractor
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_ID
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatViewModel(
    private val messagesInteractor: MessagesInteractor,
    private val application: Application
) : ViewModel() {

    private val chatLiveData = MutableLiveData<ChatState>()
    fun observeChat(): LiveData<ChatState> = chatLiveData

    fun sendMessage(
        chatId: Long,
        message: String,
        encrypter: SymmetricEncrypterContext?
    ) {
        viewModelScope.launch {
            val date = SimpleDateFormat("HH:mm (dd/MM/yyyy)", Locale.getDefault()).format(Date())
            val resource = messagesInteractor.sendTextMessage(CLIENT_ID, chatId, Message.Data(true, message, date, Message.MESSAGE_TEXT_CODE), encrypter!!)
            if (resource is Resource.Success) {
                chatLiveData.postValue(ChatState.SuccessfullySendMessage)
            } else {
                chatLiveData.postValue(ChatState.FailedToSendMessage)
            }
        }
    }

    fun sendFile(
        chatId: Long,
        input: InputStream,
        size: Long,
        date: String,
        type: String,
        encrypter: SymmetricEncrypterContext?
    ) {
        viewModelScope.launch {
            val dest = application.applicationContext.externalCacheDir
            if (dest?.exists() != true) {
                dest?.mkdirs()
            }
            val cachedFile = saveFileInCache("${UUID.randomUUID()}.${type}", input, application)
            val message = Message.File(true,null, cachedFile.inputStream(), type, size, date, getContentType(type), dest.toString())
            val resource = messagesInteractor.uploadEncryptedFile(cachedFile.name, CLIENT_ID, chatId, message, encrypter!!)
            if (resource is Resource.Success) {
                val cachedFileName = resource.data!!
                val responseFile = Message.File(false, cachedFileName, cachedFile.inputStream(), type, size, date, getContentType(type), cachedFile.parent)
                chatLiveData.postValue(ChatState.SuccessfullySendFile(responseFile))
            } else {
                chatLiveData.postValue(ChatState.FailedToSendMessage)
            }
            input.close()
        }
    }

    fun getMessages(
        chatId: Long,
        encrypter: SymmetricEncrypterContext?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
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
                Files.delete(Paths.get("${application.applicationContext.externalCacheDir}${File.separator}${ENCRYPTED_FILE_PREFIX}${it.name}"))
            }
        }
    }

    fun getFileUri(
        message: Message.File,
        encrypter: SymmetricEncrypterContext?
    ): Uri {
        if (message.dest == null) {
            message.dest = application.applicationContext.externalCacheDir.toString()
        }
        val resource = messagesInteractor.getDecryptedFileUri(message, encrypter!!)
        return if (resource is Resource.Success) {
            resource.data!!
        } else {
            Uri.EMPTY
        }
    }

}