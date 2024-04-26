package ru.namerpro.nchat.domain.entities.ciphers.symmetricencrypters.magenta

import ru.namerpro.nchat.domain.entities.ciphers.symmetricencrypters.magenta.Magenta.Companion.MAGENTA_POLYNOMIAL_WITHOUT_8TH_BIT

object MagentaGF {
    fun add(
        x: UByte,
        y: UByte
    ): UByte {
        return x xor y
    }

    private fun multiply(
        x: UByte,
        y: UByte,
        mod: UByte
    ): UByte {
        var x = x
        var mul = 0.toUShort()
        while (x != 0.toUByte()) {
            val bitIndex = getPolynomialPower(x.toLong())
            mul = mul xor (y.toInt() shl bitIndex).toUShort()
            x = x xor (1 shl bitIndex).toUByte()
        }
        return reminder8(mul, mod)
    }

    private fun reminder8(
        x: UShort,
        mod: UByte
    ): UByte {
        var x = x
        if (mod == 0.toUByte()) {
            throw ArithmeticException("Reminder by 0 is not allowed!")
        }
        if (x == 0.toUShort()) {
            return 0u
        }
        val maxModPolyPow = 8
        var xPolyPow = getPolynomialPower(x.toLong())
        while (xPolyPow >= maxModPolyPow) {
            x = x xor ((mod.toInt() or (1 shl 8)) shl (xPolyPow - maxModPolyPow)).toUShort()
            xPolyPow = getPolynomialPower(x.toLong())
        }
        return x.toUByte()
    }

    fun pow(
        x: UByte,
        y: UInt,
        mod: UByte
    ): UByte {
        var x = x
        var y = y
        var z: UByte = 1u
        while (y != 0u) {
            if ((y and 1u) == 1u) {
                z = multiply(z, x, mod)
            }
            x = multiply(x, x, mod)
            y = y shr 1
        }
        return z
    }

    fun isPrimitiveElementOfGaloisField8(
        x: UByte
    ): Boolean = pow(x, 15u, MAGENTA_POLYNOMIAL_WITHOUT_8TH_BIT) != 1.toUByte()
                    && pow(x, 51u, MAGENTA_POLYNOMIAL_WITHOUT_8TH_BIT) != 1.toUByte()
                        && pow(x, 85u, MAGENTA_POLYNOMIAL_WITHOUT_8TH_BIT) != 1.toUByte()

    fun getPrimitiveElementsOfGaloisField8(): ByteArray {
        val elements = ByteArray(128)
        var j = 0
        for (i in 1..<(1 shl 8)) {
            if (pow(i.toUByte(), 15u, MAGENTA_POLYNOMIAL_WITHOUT_8TH_BIT) != 1.toUByte() && pow(i.toUByte(), 51u, MAGENTA_POLYNOMIAL_WITHOUT_8TH_BIT) != 1.toUByte()
                    && pow(i.toUByte(), 85u, MAGENTA_POLYNOMIAL_WITHOUT_8TH_BIT) != 1.toUByte()) {
                elements[j++] = i.toByte()
            }
        }
        return elements
    }

    private fun getPolynomialPower(
        x: Long
    ): Int {
        return if (x == 0L) 0 else java.lang.Long.numberOfTrailingZeros(java.lang.Long.highestOneBit(x))
    }
}