package ru.namerpro.nchat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages_table")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val chatId: Long,
    val message: String
)