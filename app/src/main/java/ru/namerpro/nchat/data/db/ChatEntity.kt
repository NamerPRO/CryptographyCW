package ru.namerpro.nchat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats_table")
data class ChatEntity(
    @PrimaryKey
    val chatId: Long,
    val isAlive: Boolean,
    val name: String,
    val partnerName: String,
    val cipher: String,
    val key: String,
    val iv: String
)