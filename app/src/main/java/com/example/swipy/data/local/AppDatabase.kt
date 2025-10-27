package com.example.swipy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.swipy.data.local.dao.UserDao
import com.example.swipy.data.local.dao.SwipeDao
import com.example.swipy.data.local.entity.UserEntity
import com.example.swipy.data.local.entity.SwipeEntity
import com.example.swipy.data.local.util.Converters

@Database(entities = [UserEntity::class, SwipeEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun swipeDao(): SwipeDao
}
