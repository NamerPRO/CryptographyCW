package ru.namerpro.nchat.domain.entities.ciphers.padding.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricPaddingMode
import java.util.*

class ISO10126 : SymmetricPaddingMode() {

    override fun add(
        src: ByteArray,
        blockSize: Int
    ): ByteArray {
        val toAddCount = blockSize - src.size % blockSize
        val out = ByteArray(src.size + toAddCount)
        System.arraycopy(src, 0, out, 0, src.size)
        for (i in 0 until toAddCount - 1) {
            out[src.size + i] = rnd.nextInt(255).toByte()
        }
        out[out.size - 1] = toAddCount.toByte()
        return out
    }

    companion object {
        private val rnd = Random()
    }

}