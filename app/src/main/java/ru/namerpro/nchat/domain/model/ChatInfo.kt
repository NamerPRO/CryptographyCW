package ru.namerpro.nchat.domain.model

import java.math.BigInteger

data class ChatInfo(
    val chatName: String,
    val cipherType: String,
    val secret: BigInteger,
    val iv: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatInfo

        if (chatName != other.chatName) return false
        if (cipherType != other.cipherType) return false
        if (secret != other.secret) return false
        return iv.contentEquals(other.iv)
    }

    override fun hashCode(): Int {
        var result = chatName.hashCode()
        result = 31 * result + cipherType.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }

}