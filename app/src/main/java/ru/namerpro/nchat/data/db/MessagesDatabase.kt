package ru.namerpro.nchat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 1, entities = [MessageEntity::class])
abstract class MessagesDatabase : RoomDatabase() {

    abstract fun getMessageDao(): MessageDao

}