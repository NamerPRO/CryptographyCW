package ru.namerpro.nchat.domain.entities.ciphers.symmetricapi

interface SymmetricEncrypter {

    fun encrypt(
        block: ByteArray
    ): ByteArray

    fun decrypt(
        block: ByteArray
    ): ByteArray

}