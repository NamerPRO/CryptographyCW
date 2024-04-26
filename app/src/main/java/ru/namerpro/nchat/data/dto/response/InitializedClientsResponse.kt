package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.dto.ClientDto
import ru.namerpro.nchat.data.dto.Response

data class InitializedClientsResponse(
    val clients: ArrayList<ClientDto>
): Response()