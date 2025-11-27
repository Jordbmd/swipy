package com.example.swipy.data.local.dao

import androidx.room.*
import com.example.swipy.data.local.entity.UserEntity

@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun getUserByEmailAndPassword(email: String, password: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Update
    suspend fun update(user: UserEntity)
    
    @Query("UPDATE users SET city = :city, country = :country, latitude = :latitude, longitude = :longitude WHERE id = :userId")
    suspend fun updateLocation(userId: Int, city: String, country: String, latitude: Double, longitude: Double)
    
    @Delete
    suspend fun delete(user: UserEntity)
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

