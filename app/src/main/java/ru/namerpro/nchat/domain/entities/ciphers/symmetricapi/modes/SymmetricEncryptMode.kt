package ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter

interface SymmetricEncryptMode {

    fun apply(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray

    fun reverse(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray

}