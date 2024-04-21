package ru.namerpro.nchat.data.dto

data class ChatDto(
    val chatName: String,
    val chatId: Long,
    val partnerName: String,
    val partnerId: Long,
    val secret: String,
    val cipherType: String
)