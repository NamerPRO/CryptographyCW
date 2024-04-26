package ru.namerpro.nchat.domain.api.repository

import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.Resource
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
        clientId: Long,
        chatId: Long,
        file: File,
        message: String
    ): Resource<Unit>

}