package ru.namerpro.nchat.domain.impl

import android.net.Uri
import androidx.core.net.toUri
import com.google.gson.Gson
import ru.namerpro.nchat.commons.Constants.Companion.ENCRYPTED_FILE_PREFIX
import ru.namerpro.nchat.domain.api.interactor.MessagesInteractor
import ru.namerpro.nchat.domain.api.repository.MessagesRepository
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.entities.ciphers.encryptionstate.EncryptionState
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.Resource
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64

class MessagesInteractorImpl(
    private val messagesRepository: MessagesRepository
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
        fileName: String,
        clientId: Long,
        chatId: Long,
        message: Message.File,
        encrypter: SymmetricEncrypterContext
    ): Resource<String> {
        val filePath = "${message.dest}/${ENCRYPTED_FILE_PREFIX}${fileName}"
        RandomAccessFile(filePath, "rw").use {
            val encryptionState = encrypter.encrypt(message.src, it, message.size).get()
            if (encryptionState is EncryptionState.Error) {
                return Resource.Error()
            }
            val resource = messagesRepository.uploadFile(clientId, chatId, File(filePath), toString(Message.Data(true, fileName, message.date, message.contentType), encrypter))
            if (resource is Resource.Error) {
                Files.delete(Paths.get(filePath))
                Files.delete(Paths.get("${message.dest}/${fileName}"))
                return Resource.Error()
            }
        }
        return Resource.Success(fileName)
    }

    override fun getDecryptedFileUri(
        message: Message.File,
        encrypter: SymmetricEncrypterContext
    ): Resource<Uri> {
        val path = "${message.dest}/${message.name}"
        if (!Files.exists(Paths.get(path))) {
            RandomAccessFile(path, "rw").use {
                val state = encrypter.decrypt(message.src, it, message.size).get()
                if (state is EncryptionState.Error) {
                    return Resource.Error()
                }
            }
        }
        return Resource.Success(File(path).toUri())
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