package ru.namerpro.nchat.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMessage(
        message: MessageEntity
    )

    @Query("SELECT * FROM messages_table WHERE chatId=:chatId ORDER BY time")
    fun getMessages(
        chatId: Long
    ): List<MessageEntity>

}