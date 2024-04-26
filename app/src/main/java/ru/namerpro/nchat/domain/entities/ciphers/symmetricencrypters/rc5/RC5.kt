package ru.namerpro.nchat.domain.entities.ciphers.symmetricencrypters.rc5

import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import kotlin.math.ceil
import kotlin.math.max

class RC5(
    private val w: UByte,
    private val r: UByte,
    private val b: UByte,
    private val key: ByteArray
) : SymmetricEncrypter {

    init {
        require(w == 16.toUByte() || w == 32.toUByte() || w == 64.toUByte())
        { "Expected w = 16, 32 or 64, but $w found!" }
    }

    private val sBox: LongArray by lazy { expandKey() }

    private val u = w.toByte() / 8

    private val p = when (w.toInt()) {
        16 -> 0xB7E1
        32 -> 0xB7E15163
        64 -> 0xB7E151628AED2A6Bu.toLong()
        else -> error(EXCEPTION_IS_NEVER_THROWN_BECAUSE_OF_CHECKS_BELOW)
    }

    private val q = when (w.toInt()) {
        16 -> 0x9E37
        32 -> 0x9E3779B9
        64 -> 0x9E3779B97F4A7C15u.toLong()
        else -> error(EXCEPTION_IS_NEVER_THROWN_BECAUSE_OF_CHECKS_BELOW)
    }

    private val modulo = when (w.toInt()) {
        16 -> UShort.MAX_VALUE.toULong() + 1u
        32 -> UInt.MAX_VALUE.toULong() + 1u
        64 -> STUB
        else -> error(EXCEPTION_IS_NEVER_THROWN_BECAUSE_OF_CHECKS_BELOW)
    }

    private val bitsCount = when (w.toInt()) {
        16 -> UShort.SIZE_BITS
        32 -> UInt.SIZE_BITS
        64 -> ULong.SIZE_BITS
        else -> error(EXCEPTION_IS_NEVER_THROWN_BECAUSE_OF_CHECKS_BELOW)
    }

    private val bitMask = when (w.toInt()) {
        16 -> UShort.MAX_VALUE.toULong()
        32 -> UInt.MAX_VALUE.toULong()
        64 -> ULong.MAX_VALUE
        else -> error(EXCEPTION_IS_NEVER_THROWN_BECAUSE_OF_CHECKS_BELOW)
    }

    override fun encrypt(
        block: ByteArray
    ): ByteArray {
        var a = toDecimal(block, 0)
        var b = toDecimal(block, u)

        a += sBox[0].toULong()
        b += sBox[1].toULong()

        if (u != 8) {
            a %= modulo
            b %= modulo
        }

        for (i in 1..r.toInt()) {
            a = ((a xor b) cshl b.toUInt()) + sBox[2 * i].toULong()
            if (u != 8) a %= modulo

            b = ((b xor a) cshl a.toUInt()) + sBox[2 * i + 1].toULong()
            if (u != 8) b %= modulo
        }

        val encrypted = ByteArray(block.size)
        toByteArray(a, encrypted, 0)
        toByteArray(b, encrypted, u)

        return encrypted
    }

    override fun decrypt(
        block: ByteArray
    ): ByteArray {
        var a = toDecimal(block, 0)
        var b = toDecimal(block, u)

        for (i in r.toInt() downTo 1) {
            if (u != 8) {
                b = ((((b - sBox[2 * i + 1].toULong() + modulo) % modulo) cshr a.toUInt()) xor a) % modulo
                a = ((((a - sBox[2 * i].toULong() + modulo) % modulo) cshr b.toUInt()) xor b) % modulo
            } else {
                b = ((b - sBox[2 * i + 1].toULong()) cshr a.toUInt()) xor a
                a = ((a - sBox[2 * i].toULong()) cshr b.toUInt()) xor b
            }
        }

        if (u != 8) {
            b = (b - sBox[1].toULong() + modulo) % modulo
            a = (a - sBox[0].toULong() + modulo) % modulo
        } else {
            b -= sBox[1].toULong()
            a -= sBox[0].toULong()
        }

        val decrypted = ByteArray(block.size)
        toByteArray(a, decrypted, 0)
        toByteArray(b, decrypted, u)

        return decrypted
    }

    private fun expandKey(): LongArray {
        val c = ceil(max(b.toInt(), 1).toDouble() / u).toInt()
        val l = getAlignedKey(c)
        val s = buildExpandKeyTable()
        return mixWithSecret(l, s, c)
    }

    private fun getAlignedKey(
        c: Int
    ): LongArray {
        val l = LongArray(c)
        for (i in b.toInt() - 1 downTo 0) {
            l[i / u] = (((l[i / u].toULong() cshl 8u) + key[i].toUByte().toULong()) % modulo).toLong()
        }
        return l
    }

    private fun buildExpandKeyTable(): LongArray {
        val s = LongArray(2 * (r.toInt() + 1))
        s[0] = p
        for (i in 0 until 2 * (r.toInt() + 1) - 1) {
            s[i + 1] = ((s[i] + q).toULong() % modulo).toLong()
        }
        return s
    }

    private fun mixWithSecret(
        l: LongArray,
        s: LongArray,
        c: Int
    ): LongArray {
        var a = 0uL; var b = 0uL
        var i = 0; var j = 0
        for (k in 0..<3 * max(c, 2 * (r.toInt() + 1))) {
            s[i] = (((s[i].toULong() + a + b) cshl 3u) % modulo).toLong()
            l[j] = (((l[j].toULong() + a + b) cshl (a + b).toUInt()) % modulo).toLong()
            a = s[i].toULong(); b = l[j].toULong()
            i = (i + 1) % (2 * (r.toInt() + 1))
            j = (j + 1) % c
        }
        return s
    }

    private infix fun ULong.cshl(
        x: UInt
    ): ULong {
        val move = (x % bitsCount.toUInt()).toInt()
        return ((this shl move) or (this shr (bitsCount - move))) and bitMask
    }

    private infix fun ULong.cshr(
        x: UInt
    ): ULong {
        val move = (x % bitsCount.toUInt()).toInt()
        return ((this shr move) or (this shl (bitsCount - move))) and bitMask
    }

    private fun toDecimal(
        x: ByteArray,
        start: Int
    ): ULong {
        var word = 0uL
        for (i in start + u - 1 downTo start.inc()) {
            word = word or x[i].toUByte().toULong()
            word = word shl 8
        }
        return word or x[start].toUByte().toULong()
    }

    private fun toByteArray(
        x: ULong,
        res: ByteArray,
        start: Int
    ) {
        var modifiableX = x
        for (i in 0 until u - 1) {
            res[start + i] = (modifiableX and 0xFFu).toUByte().toByte()
            modifiableX = modifiableX shr 8
        }
        res[start + u - 1] = (modifiableX and 0xFFu).toUByte().toByte()
    }

    companion object {
        private const val EXCEPTION_IS_NEVER_THROWN_BECAUSE_OF_CHECKS_BELOW = "Never thrown, because of checks below. Added to make compiler happy."
        private const val STUB = 1uL
    }

}