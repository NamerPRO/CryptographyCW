package ru.namerpro.nchat.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addChat(
        chatEntity: ChatEntity
    )

    @Query("SELECT * FROM chats_table")
    fun getChats(): List<ChatEntity>

    @Query("UPDATE chats_table SET isAlive = :isAlive WHERE chatId = :chatId")
    fun updateAliveState(
        chatId: Long,
        isAlive: Boolean
    )

}