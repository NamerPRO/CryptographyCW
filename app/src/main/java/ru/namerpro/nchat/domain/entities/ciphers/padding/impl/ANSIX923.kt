package ru.namerpro.nchat.domain.entities.ciphers.padding.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricPaddingMode

class ANSIX923 : SymmetricPaddingMode() {

    override fun add(
        src: ByteArray,
        blockSize: Int
    ): ByteArray {
        val toAddCount = blockSize - src.size % blockSize
        val out = ByteArray(src.size + toAddCount)
        System.arraycopy(src, 0, out, 0, src.size)
        out[out.size - 1] = toAddCount.toByte()
        return out
    }

}