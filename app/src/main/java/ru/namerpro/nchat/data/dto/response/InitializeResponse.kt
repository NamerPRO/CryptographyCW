package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.Response

data class InitializeResponse(
    val clientId: Long
) : Response()