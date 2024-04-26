package ru.namerpro.nchat.domain.entities.ciphers.padding.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricPaddingMode

class Zeros : SymmetricPaddingMode() {

    override fun add(
        src: ByteArray,
        blockSize: Int
    ): ByteArray {
        if (src.size % blockSize == 0) {
            return src
        }
        val toAddCount = blockSize - src.size % blockSize
        val out = ByteArray(src.size + toAddCount)
        System.arraycopy(src, 0, out, 0, src.size)
        return out
    }

    override fun remove(
        src: ByteArray,
        blockSize: Int
    ): ByteArray {
        var barrier = src.size
        while (barrier > 0 && src[barrier - 1].toInt() == 0) {
            --barrier
        }
        val out = ByteArray(barrier)
        System.arraycopy(src, 0, out, 0, barrier)
        return out
    }

}