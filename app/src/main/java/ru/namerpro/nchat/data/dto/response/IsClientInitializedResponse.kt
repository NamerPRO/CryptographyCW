package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.Response

data class IsClientInitializedResponse(
    val isInitialized: Boolean
) : Response()