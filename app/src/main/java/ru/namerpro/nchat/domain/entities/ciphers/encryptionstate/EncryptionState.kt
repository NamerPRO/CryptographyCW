package ru.namerpro.nchat.domain.entities.ciphers.encryptionstate

sealed interface EncryptionState {

    data object Success : EncryptionState

    data class Error(
        val error: Throwable
    ) : EncryptionState

}