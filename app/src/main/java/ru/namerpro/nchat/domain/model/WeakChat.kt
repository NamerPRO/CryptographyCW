package ru.namerpro.nchat.domain.model

data class WeakChat(
    var chatName: String?,
    var partnerName: String?,
    var secretKey: ByteArray?,
    var cipher: Cipher?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WeakChat

        if (chatName != other.chatName) return false
        if (partnerName != other.partnerName) return false
        if (secretKey != null) {
            if (other.secretKey == null) return false
            if (!secretKey.contentEquals(other.secretKey)) return false
        } else if (other.secretKey != null) return false
        return cipher == other.cipher
    }

    override fun hashCode(): Int {
        var result = chatName?.hashCode() ?: 0
        result = 31 * result + (partnerName?.hashCode() ?: 0)
        result = 31 * result + (secretKey?.contentHashCode() ?: 0)
        result = 31 * result + (cipher?.hashCode() ?: 0)
        return result
    }

}