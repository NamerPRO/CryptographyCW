package ru.namerpro.nchat.domain.model

enum class Cipher {

    RC5,
    MAGENTA;

    companion object {

        fun fromString(
            cipher: String
        ) = when (cipher) {
            "\"RC5\"", "RC5" -> RC5
            "\"Magenta\"", "Magenta", "MAGENTA", "\"MAGENTA\"" -> MAGENTA
            else -> error("Never thrown. Added to make compiler happy.")
        }

    }

}