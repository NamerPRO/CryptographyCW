package ru.namerpro.nchat.data

import ru.namerpro.nchat.data.dto.Response
import ru.namerpro.nchat.data.dto.dto.ChatInfoDto
import ru.namerpro.nchat.domain.model.Task
import java.io.File

interface NetworkClient {

    suspend fun getInitializedClients(): Response

    suspend fun isInitialized(
        clientId: Long
    ): Response

    suspend fun initialize(
        clientName: String
    ): Response

    suspend fun newChats(
        clientId: Long
    ): Response

    suspend fun createChat(
        creatorId: Long,
        partnerId: Long,
        chatData: ChatInfoDto
    ): Response

    suspend fun sendPartOfKey(
        receiverId: Long,
        chatId: Long,
        partOfKey: String
    ): Response

    suspend fun getPartsOfKeys(
        clientId: Long
    ): Response

    suspend fun getMessages(
        clientId: Long,
        chatId: Long
    ): Response

    suspend fun sendMessage(
        clientId: Long,
        chatId: Long,
        message: String
    ): Response

    suspend fun uploadFile(
        task: Task,
        clientId: Long,
        chatId: Long,
        file: File,
        message: String
    ): Response

    suspend fun downloadFile(
        task: Task,
        pathToFolder: String,
        fileName: String
    ): Response

    suspend fun leaveChat(
        clientId: Long,
        chatId: Long
    ): Response

    suspend fun deinitialize(
        clientId: Long
    ): Response

}