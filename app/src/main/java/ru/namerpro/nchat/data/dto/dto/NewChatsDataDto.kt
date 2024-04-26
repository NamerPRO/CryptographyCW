package ru.namerpro.nchat.data.dto.dto

data class NewChatsDataDto(
    val chatName: String,
    val chatId: Long,
    val partnerName: String,
    val partnerId: Long,
    val secret: String,
    val cipherType: String,
    val iv: String
)