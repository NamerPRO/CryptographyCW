package ru.namerpro.nchat.data.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import ru.namerpro.nchat.data.dto.ClientDto

interface NChatServiceApi {

    @POST("/get_initialized_clients")
    suspend fun getInitializedClients(): ArrayList<ClientDto>

    @POST("/add_new_chat/{creator_id}/{partner_id}/{chat_id}/{chat_name}")
    suspend fun addNewChat(
        @Path("creator_id") creatorId: Long,
        @Path("partner_id") partnerId: Long,
        @Path("chat_id") chatId: Long,
        @Path("chat_name") chatName: String,
        @Body secret: String
    )

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
    ): List<Triple<Pair<Long, String>, Pair<Long, String>, String>>

    @POST("/create_chat/{creator_id}/{partner_id}")
    suspend fun createChat(
        @Path("creator_id") creatorId: Long,
        @Path("partner_id") partnerId: Long
    ): Long

    @POST("/send_part_of_key/{receiver_id}/{chat_id}")
    suspend fun sendPartOfKey(
        @Path("receiver_id") receiverId: Long,
        @Path("chat_id") chatId: Long,
        @Body partOfKey: String
    )

    @POST("/get_parts_of_keys/{client_id}")
    suspend fun getPartsOfKeys(
        @Path("client_id") clientId: Long
    ): List<Pair<Long, String>>

}