package ru.namerpro.nchat.domain.entities.ciphers.mode.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricEncryptMode
import ru.namerpro.nchat.domain.entities.ciphers.cryptoutils.Utility
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class CTR(
    private val service: ExecutorService,
    iv: ByteArray,
    blockSize: Int
) : SymmetricEncryptMode {
    private val iv: ByteArray
    private val blockSize: Int

    init {
        require(blockSize > iv.size) { "Length of IV cannot be more than block size! Block size: " + blockSize + ", IV length: " + iv.size }

        this.iv = iv
        this.blockSize = blockSize
    }

    override fun apply(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        return innerApplyReverseLogic(src, blockSize, encrypter)
    }

    override fun reverse(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        return innerApplyReverseLogic(src, blockSize, encrypter)
    }

    private fun innerApplyReverseLogic(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        val futures: MutableList<Pair<Int, Future<*>>> = ArrayList()
        val split: Array<ByteArray> = Utility.splitToBlocks(src, blockSize)
        for (i in split.indices) {
            futures.add(i to service.submit(Callable {
                Utility.xor(
                    split[i],
                    encrypter.encrypt(getCounter(iv, i))
                )
            }))
        }
        return Utility.queryResult(futures, blockSize, src.size)
    }

    private fun getCounter(
        left: ByteArray,
        right: Int
    ): ByteArray {
        var right = right
        val counter = ByteArray(blockSize)
        var j = 0
        for (i in 0 until blockSize) {
            if (i < left.size) {
                counter[i] = (counter[i].toInt() or left[i].toInt()).toByte()
            } else {
                counter[blockSize - ++j] = (counter[blockSize - ++j].toInt() or right.toByte().toInt()).toByte()
                right = right ushr 8
            }
        }
        return counter
    }
}