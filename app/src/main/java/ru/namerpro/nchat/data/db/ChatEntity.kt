package ru.namerpro.nchat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats_table")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val chatId: Long,
    val name: String,
    val partnerName: String,
    val cipher: String,
    val key: String,
    val iv: String
)