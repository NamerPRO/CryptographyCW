package ru.namerpro.nchat.domain.entities.ciphers.mode.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricEncryptMode
import ru.namerpro.nchat.domain.entities.ciphers.cryptoutils.Utility

class OFB(
    private val iv: ByteArray
) : SymmetricEncryptMode {

    override fun apply(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        val split: Array<ByteArray> = Utility.splitToBlocks(src, blockSize)
        val o = preprocess(split.size, encrypter)
        val c = ByteArray(src.size)
        var block: ByteArray
        for (i in split.indices) {
            block = Utility.xor(split[i], o[i])
            for (j in 0 until blockSize) {
                c[blockSize * i + j] = block[j]
            }
        }
        return c
    }

    override fun reverse(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        val split: Array<ByteArray> = Utility.splitToBlocks(src, blockSize)
        val o = preprocess(split.size, encrypter)
        val m = ByteArray(src.size)
        var block: ByteArray
        for (i in split.indices) {
            block = Utility.xor(split[i], o[i])
            for (j in 0 until blockSize) {
                m[blockSize * i + j] = block[j]
            }
        }
        return m
    }

    private fun preprocess(
        blocksCount: Int,
        encrypter: SymmetricEncrypter
    ): Array<ByteArray> {
        val o = Array(blocksCount) { byteArrayOf() }
        for (i in 0 until blocksCount) {
            o[i] = encrypter.encrypt(if (i == 0) iv else o[i - 1])
        }
        return o
    }
}