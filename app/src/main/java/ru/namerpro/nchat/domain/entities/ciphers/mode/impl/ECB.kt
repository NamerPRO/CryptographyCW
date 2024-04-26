package ru.namerpro.nchat.domain.entities.ciphers.mode.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricEncryptMode
import ru.namerpro.nchat.domain.entities.ciphers.cryptoutils.Utility
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class ECB(
    private val service: ExecutorService
) : SymmetricEncryptMode {

    override fun apply(
        src: ByteArray,
        blockSize: Int,
        encrypter: SymmetricEncrypter
    ): ByteArray {
        val futures: MutableList<Pair<Int, Future<*>>> = ArrayList()
        val split: Array<ByteArray> = Utility.splitToBlocks(src, blockSize)
        for (i in split.indices) {
            futures.add(i to service.submit(Callable { encrypter.encrypt(split[i]) }))
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
        for (i in split.indices) {
            futures.add(i to service.submit(Callable { encrypter.decrypt(split[i]) }))
        }
        return Utility.queryResult(futures, blockSize, src.size)
    }

}