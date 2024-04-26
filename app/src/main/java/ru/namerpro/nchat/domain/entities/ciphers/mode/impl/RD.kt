package ru.namerpro.nchat.domain.entities.ciphers.mode.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricEncryptMode
import ru.namerpro.nchat.domain.entities.ciphers.cryptoutils.Utility
import java.math.BigInteger
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class RD(
    private val service: ExecutorService,
    iv: ByteArray
) : SymmetricEncryptMode {

    private val initial: BigInteger
    private val delta: BigInteger

    init {
        val blockSize = iv.size / 2
        val deltaAsByteArray = ByteArray(blockSize + 1)
        val initialAsByteArray = ByteArray(iv.size + 1)
        for (i in 1..iv.size) {
            if (i < deltaAsByteArray.size) {
                deltaAsByteArray[i] = iv[i + blockSize - 1]
            }
            initialAsByteArray[i] = iv[i - 1]
        }

        this.delta = BigInteger(deltaAsByteArray)
        this.initial = BigInteger(initialAsByteArray)
    }

    override fun apply(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        val futures: MutableList<Pair<Int, Future<*>>> = ArrayList()
        val split: Array<ByteArray> = Utility.splitToBlocks(src, blockSize)
        var value = initial
        for (i in split.indices) {
            val finalValue = value
            futures.add(i to service.submit(Callable {
                encrypter.encrypt(
                    Utility.xor(
                        Utility.toByteArray(
                            finalValue
                        ), split[i]
                    )
                )
            }))
            value = value.add(delta)
        }
        return Utility.queryResult(futures, blockSize, src.size)
    }

    override fun reverse(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        val futures: MutableList<Pair<Int, Future<*>>> = ArrayList()
        val split: Array<ByteArray> = Utility.splitToBlocks(src, blockSize)
        var value = initial
        for (i in split.indices) {
            val finalValue = value
            futures.add(i to service.submit(Callable {
                Utility.xor(
                    encrypter.decrypt(split[i]),
                    Utility.toByteArray(finalValue)
                )
            }))
            value = value.add(delta)
        }
        return Utility.queryResult(futures, blockSize, src.size)
    }

}