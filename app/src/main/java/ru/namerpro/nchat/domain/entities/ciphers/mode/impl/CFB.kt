package ru.namerpro.nchat.domain.entities.ciphers.mode.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricEncryptMode
import ru.namerpro.nchat.domain.entities.ciphers.cryptoutils.Utility
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class CFB(
    private val service: ExecutorService,
    private val iv: ByteArray
) : SymmetricEncryptMode {

    override fun apply(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        val split: Array<ByteArray> = Utility.splitToBlocks(src, blockSize)
        val c = ByteArray(src.size)
        var prevC = iv
        for (i in split.indices) {
            prevC = Utility.xor(encrypter.encrypt(prevC), split[i])
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
            futures.add(i to service.submit(Callable {
                Utility.xor(
                    encrypter.encrypt(
                        if (i == 0) iv else split[i - 1]
                    ), split[i]
                )
            }))
        }
        return Utility.queryResult(futures, blockSize, src.size)
    }
}