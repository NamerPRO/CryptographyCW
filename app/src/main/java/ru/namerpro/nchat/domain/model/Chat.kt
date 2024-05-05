package ru.namerpro.nchat.domain.model

data class Chat(
    val id: Long,
    var isAlive: Boolean,
    val name: String,
    val partnerName: String,
    val cipher: Cipher,
    val key: ByteArray,
    val iv: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chat

        if (isAlive != other.isAlive) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (partnerName != other.partnerName) return false
        if (cipher != other.cipher) return false
        if (!key.contentEquals(other.key)) return false
        return iv.contentEquals(other.iv)
    }

    override fun hashCode(): Int {
        var result = isAlive.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + partnerName.hashCode()
        result = 31 * result + cipher.hashCode()
        result = 31 * result + key.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }

}
