package ru.namerpro.nchat.domain.api.interactor

import android.net.Uri
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.domain.model.Task

interface MessagesInteractor {

    suspend fun getMessages(
        clientId: Long,
        chatId: Long,
        encrypter: SymmetricEncrypterContext
    ): Resource<List<Message>>

    suspend fun sendTextMessage(
        clientId: Long,
        chatId: Long,
        message: Message.Data,
        encrypter: SymmetricEncrypterContext
    ): Resource<Unit>

    suspend fun uploadEncryptedFile(
        clientId: Long,
        chatId: Long,
        message: Message.File,
        encrypter: SymmetricEncrypterContext,
        task: Task
    ): Resource<Unit>

    suspend fun downloadFile(
        message: Message.File,
        pathToFolder: String,
        encrypter: SymmetricEncrypterContext,
        task: Task
    ): Resource<Unit>

}