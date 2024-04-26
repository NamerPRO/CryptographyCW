package ru.namerpro.nchat.data.network

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.apache.commons.io.IOUtils
import retrofit2.HttpException
import ru.namerpro.nchat.commons.Constants
import ru.namerpro.nchat.commons.Constants.Companion.ENCRYPTED_FILE_PREFIX
import ru.namerpro.nchat.commons.saveFileInCache
import ru.namerpro.nchat.data.NetworkClient
import ru.namerpro.nchat.data.dto.Response
import ru.namerpro.nchat.data.dto.dto.ChatInfoDto
import ru.namerpro.nchat.data.dto.response.CreateChatResponse
import ru.namerpro.nchat.data.dto.response.DownloadFileResponse
import ru.namerpro.nchat.data.dto.response.GetMessageResponse
import ru.namerpro.nchat.data.dto.response.GetPartsOfKeysResponse
import ru.namerpro.nchat.data.dto.response.InitializeResponse
import ru.namerpro.nchat.data.dto.response.InitializedClientsResponse
import ru.namerpro.nchat.data.dto.response.IsClientInitializedResponse
import ru.namerpro.nchat.data.dto.response.NewChatsResponse
import ru.namerpro.nchat.data.dto.response.SendMessageResponse
import ru.namerpro.nchat.data.dto.response.SendPartOfKeyResponse
import ru.namerpro.nchat.data.dto.response.UploadFileResponse
import ru.namerpro.nchat.domain.model.NetworkResponse
import java.io.File
import java.io.InputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RetrofitNetworkClient(
    private val nChatServiceApi: NChatServiceApi,
    private val application: Application
) : NetworkClient {

    private fun handleNetworkException(
        e: Throwable,
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
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
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
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
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
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
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
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
        }
    }

    override suspend fun createChat(
        creatorId: Long,
        partnerId: Long,
        chatData: ChatInfoDto
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                val chatId = nChatServiceApi.createChat(creatorId, partnerId, chatData)
                CreateChatResponse(chatId).apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
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
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
        }
    }

    override suspend fun getPartsOfKeys(
        clientId: Long
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                val secret = nChatServiceApi.getPartsOfKeys(clientId)
                GetPartsOfKeysResponse(secret).apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
        }
    }

    override suspend fun getMessages(
        clientId: Long,
        chatId: Long
    ): Response {
        return try {
            withContext(Dispatchers.IO) {
                val messages = nChatServiceApi.getMessages(clientId, chatId)
                GetMessageResponse(messages).apply { responseCode = NetworkResponse.SUCCESS.code }
            }
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
        }
    }

    override suspend fun sendMessage(
        clientId: Long,
        chatId: Long,
        message: String
    ): Response {
        return try {
            nChatServiceApi.sendMessage(clientId, chatId, message)
            SendMessageResponse().apply { responseCode = NetworkResponse.SUCCESS.code }
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
        }
    }

    override suspend fun uploadFile(
        clientId: Long,
        chatId: Long,
        file: File,
        message: String
    ): Response {
        return try {
            val requestFile = RequestBody.create(MultipartBody.FORM, file)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            nChatServiceApi.uploadFile(clientId, chatId, body, message)
            UploadFileResponse().apply { responseCode = NetworkResponse.SUCCESS.code }
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
        }
    }

    override suspend fun downloadFile(
        fileName: String
    ): Response {
        return try {
            val data = nChatServiceApi.downloadFile(fileName)
            val file = saveFileInCache(fileName, data.byteStream(), application)
            DownloadFileResponse(file.inputStream(), file.length()).apply { responseCode = NetworkResponse.SUCCESS.code }
        } catch (exception: Throwable) {
            when (exception) {
                is HttpException -> Response().apply { responseCode = exception.code() }
                is ConnectException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is SocketTimeoutException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                is UnknownHostException -> handleNetworkException(exception, NetworkResponse.SERVICE_UNAVAILABLE)
                else -> handleNetworkException(exception, NetworkResponse.UNKNOWN_ERROR)
            }
        }
    }

}