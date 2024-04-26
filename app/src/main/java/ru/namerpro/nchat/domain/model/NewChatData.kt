package ru.namerpro.nchat.domain.model

import java.math.BigInteger

data class NewChatData(
    val chatName: String,
    val chatId: Long,
    val partnerName: String,
    val partnerId: Long,
    val secret: BigInteger,
    val cipherType: Cipher,
    val iv: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NewChatData

        if (chatName != other.chatName) return false
        if (chatId != other.chatId) return false
        if (partnerName != other.partnerName) return false
        if (partnerId != other.partnerId) return false
        if (secret != other.secret) return false
        if (cipherType != other.cipherType) return false
        return iv.contentEquals(other.iv)
    }

    override fun hashCode(): Int {
        var result = chatName.hashCode()
        result = 31 * result + chatId.hashCode()
        result = 31 * result + partnerName.hashCode()
        result = 31 * result + partnerId.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + cipherType.hashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }

}