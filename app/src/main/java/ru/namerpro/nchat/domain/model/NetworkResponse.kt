package ru.namerpro.nchat.domain.model

import java.net.HttpURLConnection

enum class NetworkResponse(
    val code: Int
) {
    SUCCESS(HttpURLConnection.HTTP_OK),
    NOT_FOUND(HttpURLConnection.HTTP_NOT_FOUND),
    UNKNOWN_ERROR(HttpURLConnection.HTTP_UNAVAILABLE),
    BAD_REQUEST(HttpURLConnection.HTTP_BAD_REQUEST),
    INTERNAL_SERVER_ERROR(HttpURLConnection.HTTP_INTERNAL_ERROR),
    SERVICE_UNAVAILABLE(HttpURLConnection.HTTP_BAD_GATEWAY),
    FORBIDDEN(HttpURLConnection.HTTP_FORBIDDEN),
    CONFLICT(HttpURLConnection.HTTP_CONFLICT);

    companion object {
        fun getErrorMessage(
            exception: String
        ): NetworkResponse {
            var errorMessage = UNKNOWN_ERROR
            when (exception) {
                "HTTP 400 " -> errorMessage = BAD_REQUEST
                "HTTP 403 " -> errorMessage = FORBIDDEN
                "HTTP 404 " -> errorMessage = NOT_FOUND
                "HTTP 409 " -> errorMessage = CONFLICT
                "HTTP 500 " -> errorMessage = INTERNAL_SERVER_ERROR
                "HTTP 503 " -> errorMessage = SERVICE_UNAVAILABLE
            }
            return errorMessage
        }

        fun getErrorByCode(
            code: Int
        ): NetworkResponse {
            return NetworkResponse.entries.find { it.code == code } ?: NetworkResponse.UNKNOWN_ERROR

        }
    }
}