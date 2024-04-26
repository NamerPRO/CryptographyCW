package ru.namerpro.nchat.domain.entities.ciphers.padding.impl

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricPaddingMode

class PKCS7 : SymmetricPaddingMode() {

    override fun add(
        src: ByteArray,
        blockSize: Int
    ): ByteArray {
        val toAddCount = blockSize - src.size % blockSize
        val out = ByteArray(src.size + toAddCount)
        System.arraycopy(src, 0, out, 0, src.size)
        for (i in 0 until toAddCount) {
            out[src.size + i] = toAddCount.toByte()
        }
        return out
    }

}