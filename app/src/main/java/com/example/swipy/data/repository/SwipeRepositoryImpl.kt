package com.example.swipy.data.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.swipy.data.local.dao.SwipeDao
import com.example.swipy.data.local.dao.UserDao
import com.example.swipy.data.local.datasource.AppDatabase
import com.example.swipy.data.local.entity.SwipeEntity
import com.example.swipy.data.local.entity.UserEntity
import com.example.swipy.data.mapper.toEntity
import com.example.swipy.data.mapper.toSwipe
import com.example.swipy.data.mapper.toUser
import com.example.swipy.data.remote.SwipeRemoteDataSource
import com.example.swipy.data.remote.models.SwipeDto
import com.example.swipy.domain.models.Swipe
import com.example.swipy.domain.models.User
import com.example.swipy.domain.repository.SwipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SwipeRepositoryImpl(context: Context) : SwipeRepository {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "swipy-db" 
    ).build()

    private val swipeDao: SwipeDao = db.swipeDao()
    private val userDao: UserDao = db.userDao()
    
    private val swipeRemoteDataSource = SwipeRemoteDataSource()

    
    override suspend fun likeUser(userId: Int, likedUserId: Int): Boolean = withContext(Dispatchers.IO) {
        val swipe = SwipeEntity(
            userId = userId,
            targetUserId = likedUserId,
            action = "like",
            timestamp = System.currentTimeMillis(),
            isSynced = false
        )
        swipeDao.insert(swipe)

        try {
            val result = swipeRemoteDataSource.createSwipe(userId, likedUserId, "like")
            result.onSuccess {
                swipeDao.markAsSynced(userId, likedUserId)
            }.onFailure { error ->
                Log.e("SwipeRepository", " Erreur sync like", error)
            }
        } catch (_: Exception) {
        }

        val reverseSwipe = swipeDao.getLikeSwipe(likedUserId, userId)
        val isMatch = reverseSwipe != null
        
        
        return@withContext isMatch
    }

    
    override suspend fun dislikeUser(userId: Int, dislikedUserId: Int): Unit = withContext(Dispatchers.IO) {
        val swipe = SwipeEntity(
            userId = userId,
            targetUserId = dislikedUserId,
            action = "dislike",
            timestamp = System.currentTimeMillis(),
            isSynced = false
        )
        swipeDao.insert(swipe)

        try {
            val result = swipeRemoteDataSource.createSwipe(userId, dislikedUserId, "dislike")
            result.onSuccess {
                swipeDao.markAsSynced(userId, dislikedUserId)
            }.onFailure { error ->
                Log.e("SwipeRepository", " Erreur sync dislike", error)
            }
        } catch (e: Exception) {
            Log.e("SwipeRepository", " Exception sync dislike", e)
        }
    }

    
    override suspend fun getUserSwipes(userId: Int): List<Swipe> = withContext(Dispatchers.IO) {
        val swipeEntities = swipeDao.getSwipesByUser(userId)
        val swipes = swipeEntities.map { swipeEntity -> swipeEntity.toSwipe() }
        swipes
    }

   
    override suspend fun getMatches(userId: Int): List<User> = withContext(Dispatchers.IO) {
        val userLikes = swipeDao.getSwipesByAction(userId, "like")
        
        val matchedUserIds = userLikes.mapNotNull { userLike ->
            val reverseSwipe = swipeDao.getLikeSwipe(userLike.targetUserId, userId)
            if (reverseSwipe != null) {
                userLike.targetUserId 
            } else {
                null  
            }
        }
        
        val matchedUsers = matchedUserIds.mapNotNull { matchedUserId ->
            userDao.getUserById(matchedUserId)?.toUser()
        }
        
        matchedUsers
    }

    override suspend fun syncPendingSwipes(): Unit = withContext(Dispatchers.IO) {
        try {
            val unsyncedSwipes = swipeDao.getUnsyncedSwipes()
            
            if (unsyncedSwipes.isEmpty()) {
                return@withContext
            }


            var successCount = 0
            var failCount = 0

            unsyncedSwipes.forEach { swipe ->
                try {
                    val result = swipeRemoteDataSource.createSwipe(
                        userId = swipe.userId,
                        targetUserId = swipe.targetUserId,
                        action = swipe.action
                    )

                    result.onSuccess {
                        swipeDao.markAsSynced(swipe.userId, swipe.targetUserId)
                        successCount++
                    }.onFailure {
                        failCount++
                    }
                } catch (e: Exception) {
                    failCount++
                }
            }

        } catch (e: Exception) {
            Log.e("SwipeRepository", " Erreur sync pending swipes", e)
        }
    }

    override suspend fun syncSwipesFromApi(userId: Int): Unit = withContext(Dispatchers.IO) {
        try {
            val result: Result<List<SwipeDto>> = swipeRemoteDataSource.getSwipesByUser(userId)
            
            result.onSuccess { swipeDtos: List<SwipeDto> ->
                var newCount = 0
                var duplicateCount = 0

                swipeDtos.forEach { swipeDto: SwipeDto ->
                    val existing = swipeDao.getSwipeByUserAndTarget(
                        swipeDto.userId,
                        swipeDto.targetUserId
                    )
                    
                    if (existing == null) {
                        val swipeEntity = swipeDto.toEntity()
                        swipeDao.insert(swipeEntity)
                        newCount++
                    } else {
                        duplicateCount++
                    }
                }
                
            }.onFailure { error ->
            }
        } catch (e: Exception) {
        }
    }

   
    override suspend fun hasSwipedUser(userId: Int, targetUserId: Int): Boolean = withContext(Dispatchers.IO) {
        val swipe = swipeDao.getSwipeByUserAndTarget(userId, targetUserId)
        
        if (swipe != null) {
            true
        } else {
            false
        }
    }
    
   
    override suspend fun getPotentialMatches(userId: Int): List<User> = withContext(Dispatchers.IO) {
        try {
            val allUsers: List<UserEntity> = userDao.getAllUsers()
            
            val userSwipes: List<SwipeEntity> = swipeDao.getSwipesByUser(userId)
            val swipedUserIds: Set<Int> = userSwipes.map { swipeEntity: SwipeEntity -> swipeEntity.targetUserId }.toSet()
            
            val potentialMatches: List<User> = allUsers
                .filter { userEntity: UserEntity -> userEntity.id != userId }  // Pas soi-même
                .filter { userEntity: UserEntity -> swipedUserIds.contains(userEntity.id).not() }  // Pas déjà swipé
                .map { userEntity: UserEntity -> userEntity.toUser() }
            
            potentialMatches
        } catch (e: Exception) {
            emptyList()
        }
    }
}