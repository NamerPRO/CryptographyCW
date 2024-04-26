package ru.namerpro.nchat.domain.entities.ciphers.mode.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricEncryptMode
import ru.namerpro.nchat.domain.entities.ciphers.cryptoutils.Utility
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class PCBC(
    private val service: ExecutorService? = null,
    private val iv: ByteArray
) : SymmetricEncryptMode {

    override fun apply(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        val split: Array<ByteArray> = Utility.splitToBlocks(src, blockSize)
        val c = ByteArray(src.size)
        var prevC = byteArrayOf(0)
        for (i in split.indices) {
            prevC = encrypter.encrypt(Utility.xor(split[i], (if (i == 0) iv else Utility.xor(split[i - 1], prevC))))
            for (j in 0 until blockSize) {
                c[blockSize * i + j] = prevC[j]
            }
        }
        return c
    }

    override fun reverse(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        val futures: MutableList<Pair<Int, Future<*>>> = ArrayList()
        val split: Array<ByteArray> = Utility.splitToBlocks(src, blockSize)
        for (i in split.indices) {
            futures.add(i to service!!.submit(Callable { encrypter.decrypt(split[i]) }))
        }
        @Suppress("UNCHECKED_CAST")
        val decryptData = Utility.queryResult(futures, blockSize, -1, true) as Array<ByteArray>
        val m = ByteArray(src.size)
        var prevM = byteArrayOf(0)
        for (i in decryptData.indices) {
            prevM = Utility.xor(decryptData[i], (if (i == 0) iv else Utility.xor(split[i - 1], prevM)))
            for (j in 0 until blockSize) {
                m[blockSize * i + j] = prevM[j]
            }
        }
        return m
    }
}