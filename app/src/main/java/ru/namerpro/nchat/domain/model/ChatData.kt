package ru.namerpro.nchat.domain.model

import java.math.BigInteger

data class ChatData(
    val chatName: String,
    val chatId: Long,
    val partnerName: String,
    val partnerId: Long,
    val secret: BigInteger,
    val cipherType: Cipher
)