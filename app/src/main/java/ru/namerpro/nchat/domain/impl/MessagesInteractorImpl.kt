package ru.namerpro.nchat.domain.impl

import com.google.gson.Gson
import ru.namerpro.nchat.commons.Constants.Companion.ENCRYPTED_FILE_PREFIX
import ru.namerpro.nchat.commons.getFileName
import ru.namerpro.nchat.commons.parentPath
import ru.namerpro.nchat.domain.api.interactor.MessagesDatabaseInteractor
import ru.namerpro.nchat.domain.api.interactor.MessagesInteractor
import ru.namerpro.nchat.domain.api.repository.MessagesRepository
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.entities.ciphers.encryptionstate.EncryptionState
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.domain.model.Task
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64

class MessagesInteractorImpl(
    private val messagesRepository: MessagesRepository,
    private val databaseInteractor: MessagesDatabaseInteractor
) : MessagesInteractor {

    override suspend fun getMessages(
        clientId: Long,
        chatId: Long,
        encrypter: SymmetricEncrypterContext
    ): Resource<List<Message>> {
        return messagesRepository.getMessages(clientId, chatId, encrypter)
    }

    override suspend fun sendTextMessage(
        clientId: Long,
        chatId: Long,
        message: Message.Data,
        encrypter: SymmetricEncrypterContext
    ): Resource<Unit> {
        return messagesRepository.sendMessage(clientId, chatId, toString(message, encrypter))
    }

    override suspend fun uploadEncryptedFile(
        clientId: Long,
        chatId: Long,
        message: Message.File,
        encrypter: SymmetricEncrypterContext,
        task: Task
    ): Resource<Unit> {
        if (task.isCancelled) {
            return Resource.Cancelled()
        }
        val serverSideName = getFileName(message.devicePath)
        val filePath = "${parentPath(message.devicePath)}${ENCRYPTED_FILE_PREFIX}$serverSideName"
        RandomAccessFile(filePath, "rw").use {
            val encryptionState = encrypter.encrypt(task, message.file?.second!!, it, message.file!!.first).get()
            if (encryptionState is EncryptionState.Cancelled) {
                return Resource.Cancelled()
            }
            if (encryptionState is EncryptionState.Error) {
                return Resource.Error()
            }
            val fileMessage = Message.Data("${serverSideName}|${message.realName}", message.date, message.time, true, message.contentType)
            val serializedMessage = toString(fileMessage, encrypter)
            val resource = messagesRepository.uploadFile(task, clientId, chatId, File(filePath), serializedMessage)
            if (resource is Resource.Error) {
                Files.delete(Paths.get(filePath))
                Files.delete(Paths.get(message.devicePath))
                return Resource.Error()
            }
            fileMessage.isReceived = false
            if (task.isCancelled) {
                return Resource.Cancelled()
            }
            databaseInteractor.addMessage(chatId, fileMessage)
        }
        return if (task.isCancelled) {
            Resource.Cancelled()
        } else {
            Resource.Success()
        }
    }

    override suspend fun downloadFile(
        message: Message.File,
        pathToFolder: String,
        encrypter: SymmetricEncrypterContext,
        task: Task
    ): Resource<Unit> {
        if (task.isCancelled) {
            return Resource.Cancelled()
        }
        val pathToFile = "${pathToFolder}${getFileName(message.devicePath)}"
        if (!Files.exists(Paths.get(pathToFile))) {
            val resource = messagesRepository.downloadFile(task, pathToFolder, "${ENCRYPTED_FILE_PREFIX}${getFileName(message.devicePath)}")
            if (task.isCancelled) {
                return Resource.Cancelled()
            }
            if (resource is Resource.Error) {
                return Resource.Error()
            }
            message.file = resource.data
            RandomAccessFile(pathToFile, "rw").use {
                val state = encrypter.decrypt(task, message.file?.second!!, it, message.file!!.first).get()
                if (task.isCancelled) {
                    return Resource.Cancelled()
                }
                if (state is EncryptionState.Error) {
                    return Resource.Error()
                }
            }
            Files.delete(Paths.get("${pathToFolder}${ENCRYPTED_FILE_PREFIX}${getFileName(message.devicePath)}"))
        }
        return if (task.isCancelled) {
            return Resource.Cancelled()
        } else {
            Resource.Success()
        }
    }

    private fun toString(
        message: Message.Data,
        encrypter: SymmetricEncrypterContext
    ): String {
        return Base64.getEncoder().encodeToString(
            encrypter.encrypt(
                Gson().toJson(message).toByteArray()
            ).get()
        )
    }

}