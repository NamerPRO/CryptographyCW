package ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes

abstract class SymmetricPaddingMode {

    abstract fun add(
        src: ByteArray,
        blockSize: Int
    ): ByteArray

    open fun remove(
        src: ByteArray,
        blockSize: Int
    ): ByteArray {
        val toRemoveCount = src[src.size - 1].toInt()
        val out = ByteArray(src.size - toRemoveCount)
        System.arraycopy(src, 0, out, 0, src.size - toRemoveCount)
        return out
    }

}