package ru.namerpro.nchat.data.dto.dto

data class ChatInfoDto(
    val chatName: String,
    val cipherType: String,
    val secret: String,
    val iv: String
)
