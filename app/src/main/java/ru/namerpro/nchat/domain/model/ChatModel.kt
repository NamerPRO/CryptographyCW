package ru.namerpro.nchat.domain.model

data class ChatModel(
    val id: Long,
    val name: String,
    val partnerName: String,
    val key: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatModel

        if (id != other.id) return false
        if (name != other.name) return false
        return key.contentEquals(other.key)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + key.contentHashCode()
        return result
    }

}
