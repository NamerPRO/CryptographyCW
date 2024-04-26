package ru.namerpro.nchat.commons

class Constants {

    companion object {
        const val FIELD_NOT_INITIALIZED = -1L
        const val SUCCESS_RESPONSE_CODE = 200
        const val PING_DELAY_MS = 300L
        const val STANDARD_AMOUNT_OF_ATTEMPTS = 15
        const val STANDARD_KEY_SIZE_IN_BYTES = 16
        const val CLICK_DEBOUNCE_DELAY = 100L

        val DIFFIE_HELLMAN_CONSTANT_P = "13232376785240337048752071462798273003935646236777459223".toBigInteger()
        val DIFFIE_HELLMAN_CONSTANT_G = "54216074119355136085795982097390670890367185141189796".toBigInteger()

        val RC5_STANDARD_BLOCK_LENGTH_IN_BITS = 64.toUByte()
        val RC5_STANDARD_ROUNDS_COUNT = 10.toUByte()

        val STANDARD_MAGENTA_PRIMITIVE_ELEMENT = 2.toUByte()

        const val EXIT_MESSAGE_CODE = '1'
        const val EXIT_MESSAGE_CODE_INT = 1
        const val SUCCESS_MESSAGE_CODE_INT = 0

        const val STANDARD_BUFFER_SIZE_IN_BYTES = 4096

        const val ENCRYPTED_FILE_PREFIX = "enc_"
    }
    
}