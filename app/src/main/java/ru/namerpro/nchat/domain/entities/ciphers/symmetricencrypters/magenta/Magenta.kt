package ru.namerpro.nchat.domain.entities.ciphers.symmetricencrypters.magenta

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import ru.namerpro.nchat.domain.entities.ciphers.cryptoutils.Utility
import kotlin.experimental.xor

class Magenta(
    private val key: ByteArray,
    private val alpha: UByte
) : SymmetricEncrypter {

    private val sBox: ByteArray by lazy { generateSBox(alpha) }
    private val roundKeys = Utility.splitToBlocks(key, 8)

    init {
        require(MagentaGF.isPrimitiveElementOfGaloisField8(alpha)) { "Alpha must be a primitive element in GF(2^8)!" }
        require(
            key.size == 16 ||
            key.size == 24 ||
            key.size == 32
        ) { "Expected key of length 128, 192 or 256 buts but " + key.size * 8 + " found!" }
    }

    override fun encrypt(
        block: ByteArray
    ): ByteArray {
        return when (key.size) {
            16 -> f(f(f(f(f(f(block, roundKeys[0]), roundKeys[0]), roundKeys[1]), roundKeys[1]), roundKeys[0]), roundKeys[0])
            24 -> f(f(f(f(f(f(block, roundKeys[0]), roundKeys[1]), roundKeys[2]), roundKeys[2]), roundKeys[1]), roundKeys[0])
            else -> f(f(f(f(f(f(f(f(block, roundKeys[0]),roundKeys[1]), roundKeys[2]), roundKeys[3]), roundKeys[3]),roundKeys[2]), roundKeys[1]), roundKeys[0])
        }
    }

    override fun decrypt(
        block: ByteArray
    ): ByteArray {
        val clonedBlock = block.clone()
        return v(encrypt(v(clonedBlock)))
    }

    private fun v(
        x: ByteArray
    ): ByteArray {
        for (i in 0..7) {
            x[i] = x[i + 8].also { x[i + 8] = x[i] }
        }
        return x
    }

    private fun a(
        x: Byte,
        y: Byte
    ) = sBox[(x.toUByte() xor sBox[y.toUByte().toInt()].toUByte()).toInt()].toUByte()

    private fun pe(
        x: Byte,
        y: Byte
    ) = ((a(x, y).toInt() shl 8) or a(y, x).toInt()).toUShort()

    private fun p(
        x: ByteArray
    ): ByteArray {
        val result = ByteArray(16)
        for (i in 0..7) {
            val peResult = pe(x[i], x[i + 8])
            result[2 * i] = (peResult.toInt() shr 8).toByte()
            result[2 * i + 1] = (peResult and 0xFF.toUShort()).toByte()
        }
        return result
    }

    private fun t(
        x: ByteArray
    ) = p(p(p(p(x))))

    private fun s(
        x: ByteArray
    ): ByteArray {
        val result = ByteArray(16)
        for (i in x.indices step 2) {
            result[i / 2] = x[i]
            result[i / 2 + 8] = x[i + 1]
        }
        return result
    }

    private fun c(
        k: Byte,
        x: ByteArray
    ): ByteArray {
        if (k == 1.toByte()) {
            return t(x)
        }
        return t(Utility.xor(x, s(c(k.dec(), x))))
    }

    private fun generateSBox(
        alpha: UByte
    ): ByteArray {
        val sBox = ByteArray(256)
        sBox[sBox.size - 1] = 0
        for (i in 0u..254u) {
            sBox[i.toInt()] = MagentaGF.pow(alpha, i, MAGENTA_POLYNOMIAL_WITHOUT_8TH_BIT).toByte()
        }
        return sBox
    }

    private fun f(
        x: ByteArray,
        k: ByteArray
    ): ByteArray {
        val glue = ByteArray(16)
        for (i in 0..15) {
            glue[i] = if (i < 8) x[i + 8] else k[i - 8]
        }
        val eValue = s(c(3, glue))
        val result = ByteArray(16)
        for (i in 0..15) {
            result[i] = if (i < 8) x[i + 8] else x[i - 8] xor eValue[i - 8]
        }
        return result
    }

    companion object {
        const val MAGENTA_POLYNOMIAL_WITHOUT_8TH_BIT: UByte = 101u
    }

}