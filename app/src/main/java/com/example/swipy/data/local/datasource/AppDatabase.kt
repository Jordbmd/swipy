package com.example.swipy.data.local.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.swipy.data.local.dao.SwipeDao
import com.example.swipy.data.local.dao.UserDao
import com.example.swipy.data.local.dao.ConversationDao
import com.example.swipy.data.local.dao.MessageDao
import com.example.swipy.data.local.entity.SwipeEntity
import com.example.swipy.data.local.entity.UserEntity
import com.example.swipy.data.local.entity.ConversationEntity
import com.example.swipy.data.local.entity.MessageEntity
import com.example.swipy.data.local.util.Converters

@Database(entities = [UserEntity::class, SwipeEntity::class, ConversationEntity::class, MessageEntity::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun swipeDao(): SwipeDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}