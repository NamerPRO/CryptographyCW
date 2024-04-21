package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.ChatDto
import ru.namerpro.nchat.data.dto.Response

data class NewChatsResponse(
    val chats: List<ChatDto>
) : Response()