package ru.namerpro.nchat.domain.api.repository

import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.Resource
import ru.namerpro.nchat.domain.model.Task
import java.io.File
import java.io.InputStream

interface MessagesRepository {

    suspend fun getMessages(
        clientId: Long,
        chatId: Long,
        encrypter: SymmetricEncrypterContext
    ): Resource<List<Message>>

    suspend fun sendMessage(
        clientId: Long,
        chatId: Long,
        message: String
    ): Resource<Unit>

    suspend fun uploadFile(
        task: Task,
        clientId: Long,
        chatId: Long,
        file: File,
        message: String
    ): Resource<Unit>

    suspend fun downloadFile(
        task: Task,
        pathToFolder: String,
        fileName: String
    ): Resource<Pair<Long, InputStream>>

}