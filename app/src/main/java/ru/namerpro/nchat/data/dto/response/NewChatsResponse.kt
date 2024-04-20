package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.Response

data class NewChatsResponse(
    val chats: List<Triple<Pair<Long, String>, Pair<Long, String>, String>>
) : Response()