package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.dto.NewChatsDataDto
import ru.namerpro.nchat.data.dto.Response

data class NewChatsResponse(
    val chats: List<NewChatsDataDto>
) : Response()