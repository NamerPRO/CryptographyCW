package ru.namerpro.nchat.domain.model

import ru.namerpro.nchat.commons.Constants.Companion.SUCCESS_RESPONSE_CODE

sealed class Resource<T>(
    val data: T? = null,
    val code: Int
) {

    class Success<T>(
        data: T? = null
    ) : Resource<T>(data, SUCCESS_RESPONSE_CODE)

    class Error<T>(
        code: Int = NetworkResponse.UNKNOWN_ERROR.code
    ) : Resource<T>(null, code)

    class Cancelled<T> : Resource<T>(null, -1)

}
