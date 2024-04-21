package ru.namerpro.nchat.data

import ru.namerpro.nchat.data.dto.Response

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
        chatData: Triple<String, String, String>
    ): Response

    suspend fun sendPartOfKey(
        receiverId: Long,
        chatId: Long,
        partOfKey: String
    ): Response

    suspend fun getPartsOfKeys(
        clientId: Long
    ): Response

}