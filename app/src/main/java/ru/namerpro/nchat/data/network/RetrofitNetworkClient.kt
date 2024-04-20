package ru.namerpro.nchat.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.namerpro.nchat.data.NetworkClient
import ru.namerpro.nchat.data.dto.Response
import ru.namerpro.nchat.data.dto.response.AddNewChatResponse
import ru.namerpro.nchat.data.dto.response.CreateChatResponse
import ru.namerpro.nchat.data.dto.response.GetPartsOfKeysResponse
import ru.namerpro.nchat.data.dto.response.InitializeResponse
import ru.namerpro.nchat.data.dto.response.InitializedClientsResponse
import ru.namerpro.nchat.data.dto.response.IsClientInitializedResponse
import ru.namerpro.nchat.data.dto.response.NewChatsResponse
import ru.namerpro.nchat.data.dto.response.SendPartOfKeyResponse
import ru.namerpro.nchat.domain.model.NetworkResponse
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RetrofitNetworkClient(
    private val nChatServiceApi: NChatServiceApi
) : NetworkClient {

    private fun handleNetworkException(
        e: Exception,
        networkResponse: NetworkResponse
    ): Response {
        return Response().apply { responseCode = networkResponse.code }
    }

    override suspend fun getInitializedClients(): Response {
        return try {
            withContext(Dispatchers.IO) {
                val clients = nChatServiceApi.getInitializedClients()
                InitializedClientsResponse(clients).apply {
                    responseCode = NetworkResponse.SUCCESS.code
                }
            }
        } catch (exception: HttpException) {
            Response().apply { responseCode = exception.code() }
        } catch (exception: ConnectException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: SocketTimeoutException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: UnknownHostException) {
            handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
        }
    }

    override suspend fun addNewChat(
        creatorId: Long,
        partnerId: Long,
        chatId: Long,
        chatName: String,
        secret: String
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                nChatServiceApi.addNewChat(creatorId, partnerId, chatId, chatName, secret)
                AddNewChatResponse().apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: HttpException) {
            Response().apply { responseCode = exception.code() }
        } catch (exception: ConnectException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: SocketTimeoutException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: UnknownHostException) {
            handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
        }
    }

    override suspend fun isInitialized(
        clientId: Long
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                val isInitialized = nChatServiceApi.isInitialized(clientId)
                IsClientInitializedResponse(isInitialized).apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: HttpException) {
            Response().apply { responseCode = exception.code() }
        } catch (exception: ConnectException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: SocketTimeoutException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: UnknownHostException) {
            handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
        }
    }

    override suspend fun initialize(
        clientName: String
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                val clientId = nChatServiceApi.initialize(clientName)
                InitializeResponse(clientId).apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: HttpException) {
            Response().apply { responseCode = exception.code() }
        } catch (exception: ConnectException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: SocketTimeoutException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: UnknownHostException) {
            handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
        }
    }

    override suspend fun newChats(
        clientId: Long
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                val clientsReadyToStartChat = nChatServiceApi.newChats(clientId)
                NewChatsResponse(clientsReadyToStartChat).apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: HttpException) {
            Response().apply { responseCode = exception.code() }
        } catch (exception: ConnectException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: SocketTimeoutException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: UnknownHostException) {
            handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
        }
    }

    override suspend fun createChat(
        creatorId: Long,
        partnerId: Long
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                val chatId = nChatServiceApi.createChat(creatorId, partnerId)
                CreateChatResponse(chatId).apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: HttpException) {
            Response().apply { responseCode = exception.code() }
        } catch (exception: ConnectException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: SocketTimeoutException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: UnknownHostException) {
            handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
        }
    }

    override suspend fun sendPartOfKey(
        receiverId: Long,
        chatId: Long,
        partOfKey: String
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                nChatServiceApi.sendPartOfKey(receiverId, chatId, partOfKey)
                SendPartOfKeyResponse().apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: HttpException) {
            Response().apply { responseCode = exception.code() }
        } catch (exception: ConnectException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: SocketTimeoutException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: UnknownHostException) {
            handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
        }
    }

    override suspend fun getPartsOfKeys(
        clientId: Long
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                val partsOfKeys = nChatServiceApi.getPartsOfKeys(clientId)
                GetPartsOfKeysResponse(partsOfKeys).apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: HttpException) {
            Response().apply { responseCode = exception.code() }
        } catch (exception: ConnectException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: SocketTimeoutException) {
            handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
        } catch (exception: UnknownHostException) {
            handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
        }
    }

}