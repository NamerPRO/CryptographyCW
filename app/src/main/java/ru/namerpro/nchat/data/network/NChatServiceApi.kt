package ru.namerpro.nchat.data.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming
import ru.namerpro.nchat.data.dto.dto.ChatInfoDto
import ru.namerpro.nchat.data.dto.dto.ClientDto
import ru.namerpro.nchat.data.dto.dto.NewChatsDataDto
import ru.namerpro.nchat.data.dto.dto.SecretKeyDto

interface NChatServiceApi {

    @POST("/get_initialized_clients")
    suspend fun getInitializedClients(): ArrayList<ClientDto>

    @POST("/is_initialized/{client_id}")
    suspend fun isInitialized(
        @Path("client_id") clientId: Long
    ): Boolean

    @POST("/initialize/{client_name}")
    suspend fun initialize(
        @Path("client_name") clientName: String
    ): Long

    @POST("/new_chats/{client_id}")
    suspend fun newChats(
        @Path("client_id") clientId: Long
    ): List<NewChatsDataDto>

    @POST("/create_chat/{creator_id}/{partner_id}")
    suspend fun createChat(
        @Path("creator_id") creatorId: Long,
        @Path("partner_id") partnerId: Long,
        @Body chatData: ChatInfoDto
    ): Long

    @POST("/send_part_of_key/{receiver_id}/{chat_id}")
    suspend fun sendPartOfKey(
        @Path("receiver_id") receiverId: Long,
        @Path("chat_id") chatId: Long,
        @Body secret: String
    )

    @POST("/get_parts_of_keys/{client_id}")
    suspend fun getPartsOfKeys(
        @Path("client_id") clientId: Long
    ): List<SecretKeyDto>

    @POST("/send_message/{client_id}/{chat_id}")
    suspend fun sendMessage(
        @Path("client_id") clientId: Long,
        @Path("chat_id") chatId: Long,
        @Body message: String
    )

    @POST("/get_messages/{client_id}/{chat_id}")
    suspend fun getMessages(
        @Path("client_id") clientId: Long,
        @Path("chat_id") chatId: Long
    ): List<String>

    @Multipart
    @POST("/upload_file/{client_id}/{chat_id}")
    suspend fun uploadFile(
        @Path("client_id") clientId: Long,
        @Path("chat_id") chatId: Long,
        @Part file: MultipartBody.Part,
        @Part("message") message: String
    )

    @Streaming
    @POST("/download_file/{file_name}")
    suspend fun downloadFile(
        @Path("file_name") fileName: String
    ): ResponseBody

}