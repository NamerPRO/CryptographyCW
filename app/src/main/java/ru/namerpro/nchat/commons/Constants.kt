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
        const val COMPOSED_FILE_PREFIX = "composed_"

        const val MAXIMUM_UPLOAD_AMOUNT_AT_THE_SAME_TIME = 2

        const val START_PROGRESS = 0.0
        const val END_PROGRESS = 100.0
        const val FAILED_TO_LOAD_PROGRESS = -1.0

        const val API_BASE_URL = "http://192.168.0.105:8080/"
        const val DOWNLOADS_FOLDER_NAME = "nchat"

        const val NOTIFICATION_CHANNEL_ID = "download_file_progress_notification"
        const val NOTIFICATION_CHANNEL_NAME = "download_file_progress_notification_name"
        const val NOTIFICATION_CHANNEL_DESCRIPTION = "download_file_progress_notification_description"

        const val NOTIFICATION_UPDATE_TIME = 1000L
        const val EPSILON = 0.000001

        const val MESSAGES_DATABASE = "messages_database.db"
        const val CHATS_DATABASE = "chats_database.db"
    }
    
}