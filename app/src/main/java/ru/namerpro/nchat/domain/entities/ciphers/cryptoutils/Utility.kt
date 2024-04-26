package ru.namerpro.nchat.domain.entities.ciphers.cryptoutils

import java.math.BigInteger
import java.util.concurrent.Future
import kotlin.experimental.xor
import kotlin.math.max

object Utility {
    fun splitToBlocks(
        src: ByteArray,
        blockSize: Int
    ): Array<ByteArray> {
        val blocksCount = src.size / blockSize
        val splitSrc = Array(blocksCount) {
            ByteArray(
                blockSize
            )
        }
        for (i in 0 until blocksCount) {
            for (j in 0 until blockSize) {
                splitSrc[i][j] = src[i * blockSize + j]
            }
        }
        return splitSrc
    }

    fun queryResult(
        futures: MutableList<Pair<Int, Future<*>>>,
        blockSize: Int,
        textSize: Int
    ): ByteArray {
        return queryResult(futures, blockSize, textSize, false) as ByteArray
    }

    fun queryResult(
        futures: MutableList<Pair<Int, Future<*>>>,
        blockSize: Int,
        textSize: Int,
        isSplit: Boolean
    ): Any {
        val result: Any
        if (isSplit) {
            val arrayOfByteArrays = Array(futures.size) { byteArrayOf() }
            result = arrayOfByteArrays
        } else {
            val byteArray = ByteArray(textSize)
            result = byteArray
        }
        var it = futures.listIterator()
        while (futures.isNotEmpty()) {
            val currentData = it.next()
            val currentIndex = currentData.first
            val currentFuture = currentData.second
            if (currentFuture.isDone) {
                val response = currentFuture.get() as ByteArray
                if (isSplit) {
                    @Suppress("UNCHECKED_CAST")
                    (result as Array<ByteArray>)[currentIndex] = response
                } else {
                    for (i in 0 until blockSize) {
                        (result as ByteArray)[currentIndex * blockSize + i] = response[i]
                    }
                }
                it.remove()
            }
            if (!it.hasNext()) {
                it = futures.listIterator()
            }
        }
        return result
    }

    fun xor(
        left: ByteArray,
        right: ByteArray
    ): ByteArray {
        val result = ByteArray(max(left.size, right.size))
        if (left.size < right.size) {
            System.arraycopy(right, 0, result, 0, right.size - left.size)
            val j = right.size - left.size
            for (i in j until result.size) {
                result[i] = left[i - j] xor right[i]
            }
        } else if (left.size > right.size) {
            System.arraycopy(left, 0, result, 0, left.size - right.size)
            val j = left.size - right.size
            for (i in j until result.size) {
                result[i] = left[i] xor right[i - j]
            }
        } else {
            for (i in result.indices) {
                result[i] = left[i] xor right[i]
            }
        }
        return result
    }

    fun toByteArray(
        text: String
    ): ByteArray {
        return text.toByteArray()
    }

    fun toByteArray(
        number: Int
    ): ByteArray {
        return byteArrayOf(
            (number ushr 24).toByte(),
            (number ushr 16).toByte(),
            (number ushr 8).toByte(),
            number.toByte()
        )
    }

    fun toByteArray(
        number: Long
    ): ByteArray {
        return byteArrayOf(
            (number ushr 56).toByte(),
            (number ushr 48).toByte(),
            (number ushr 40).toByte(),
            (number ushr 32).toByte(),
            (number ushr 24).toByte(),
            (number ushr 16).toByte(),
            (number ushr 8).toByte(),
            number.toByte()
        )
    }

    fun toByteArray(
        number: BigInteger
    ): ByteArray {
        val out = number.toByteArray()
        val extra = if (out[0].toInt() == 0) (if (out.size == 1) 0 else 1) else 0
        if (extra == 0) {
            return out
        }
        val out2 = ByteArray(out.size - 1)
        System.arraycopy(out, 1, out2, 0, out.size - 1)
        return out2
    }
}