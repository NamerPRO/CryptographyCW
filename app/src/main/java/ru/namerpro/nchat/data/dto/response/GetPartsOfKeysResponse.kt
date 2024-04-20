package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.Response

data class GetPartsOfKeysResponse(
    val partsOfKeys: List<Pair<Long, String>>
) : Response()