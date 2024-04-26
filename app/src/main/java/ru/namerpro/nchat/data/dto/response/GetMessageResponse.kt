package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.Response

data class GetMessageResponse(
    val messages: List<String>
) : Response()