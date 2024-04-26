package ru.namerpro.nchat.data.dto.response

import ru.namerpro.nchat.data.dto.Response
import ru.namerpro.nchat.data.dto.dto.SecretKeyDto

data class GetPartsOfKeysResponse(
    val partsOfKeys: List<SecretKeyDto>
) : Response()