package ru.namerpro.nchat.data.impl

import kotlinx.coroutines.ensureActive
import ru.namerpro.nchat.commons.Constants.Companion.SUCCESS_RESPONSE_CODE
import ru.namerpro.nchat.data.Convertors
import ru.namerpro.nchat.data.NetworkClient
import ru.namerpro.nchat.data.dto.response.DownloadFileResponse
import ru.namerpro.nchat.data.dto.response.GetMessageResponse
import ru.namerpro.nchat.domain.api.repository.MessagesRepository
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.domain.model.Task
import ru.namerpro.nchat.domain.model.Resource
import java.io.File
import java.io.InputStream

class MessagesRepositoryImpl(
    private val networkClient: NetworkClient
) : MessagesRepository {

    override suspend fun getMessages(
        clientId: Long,
        chatId: Long,
        encrypter: SymmetricEncrypterContext
    ): Resource<List<Message>> {
        val response = networkClient.getMessages(clientId, chatId)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success(
                (response as GetMessageResponse).messages.map { Convertors().messageFromString(it, encrypter) }
            )
        } else {
            Resource.Error(response.responseCode)
        }
    }

    override suspend fun sendMessage(
        clientId: Long,
        chatId: Long,
        message: String
    ): Resource<Unit> {
        val response = networkClient.sendMessage(clientId, chatId, message)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success()
        } else {
            Resource.Error(response.responseCode)
        }
    }

    override suspend fun uploadFile(
        task: Task,
        clientId: Long,
        chatId: Long,
        file: File,
        message: String
    ): Resource<Unit> {
        task.coroutineScope.ensureActive()
        val response = networkClient.uploadFile(task, clientId, chatId, file, message)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            Resource.Success()
        } else {
            Resource.Error()
        }
    }

    override suspend fun downloadFile(
        task: Task,
        pathToFolder: String,
        fileName: String
    ): Resource<Pair<Long, InputStream>> {
        task.coroutineScope.ensureActive()
        val response = networkClient.downloadFile(task, pathToFolder, fileName)
        return if (response.responseCode == SUCCESS_RESPONSE_CODE) {
            response as DownloadFileResponse
            Resource.Success(Pair(response.size, response.input))
        } else {
            Resource.Error()
        }
    }

}