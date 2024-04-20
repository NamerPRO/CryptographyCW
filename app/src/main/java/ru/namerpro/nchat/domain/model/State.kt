package ru.namerpro.nchat.domain.model

sealed class State<T>(
    val data: T? = null,
    val message: String? = null
) {

    class WithData<T>(
        data: T
    ) : State<T>(data)

    class NotReady<T>(
        message: String? = null
    ) : State<T>(null, message)

}
