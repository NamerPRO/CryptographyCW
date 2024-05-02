package ru.namerpro.nchat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 1, entities = [ChatEntity::class])
abstract class ChatsDatabase : RoomDatabase() {

    abstract fun getChatsDao(): ChatsDao

}