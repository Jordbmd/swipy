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


class SwipeRepositoryImpl(
    context: Context,
    private val currentUserId: Int
) : SwipeRepository {

    init {
        Log.d("SwipeRepository", "SwipeRepositoryImpl initialized with currentUserId: $currentUserId")
    }

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

            unsyncedSwipes.forEach { swipe ->
                try {
                    val result = swipeRemoteDataSource.createSwipe(
                        userId = swipe.userId,
                        targetUserId = swipe.targetUserId,
                        action = swipe.action
                    )

                    result.onSuccess {
                        swipeDao.markAsSynced(swipe.userId, swipe.targetUserId)
                    }
                } catch (_: Exception) {
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
                swipeDtos.forEach { swipeDto: SwipeDto ->
                    val existing = swipeDao.getSwipeByUserAndTarget(
                        swipeDto.userId,
                        swipeDto.targetUserId
                    )
                    
                    if (existing == null) {
                        val swipeEntity = swipeDto.toEntity()
                        swipeDao.insert(swipeEntity)
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

   
    override suspend fun hasSwipedUser(userId: Int, targetUserId: Int): Boolean = withContext(Dispatchers.IO) {
        val swipe = swipeDao.getSwipeByUserAndTarget(userId, targetUserId)
        swipe != null
    }
    
   
    override suspend fun getPotentialMatches(userId: Int): List<User> = withContext(Dispatchers.IO) {
        try {
            val allUsers: List<UserEntity> = userDao.getAllUsers()
            
            val userSwipes: List<SwipeEntity> = swipeDao.getSwipesByUser(userId)
            val swipedUserIds: Set<Int> = userSwipes.map { swipeEntity: SwipeEntity -> swipeEntity.targetUserId }.toSet()
            
            val potentialMatches: List<User> = allUsers
                .filter { userEntity: UserEntity -> userEntity.id != userId }
                .filter { userEntity: UserEntity -> !swipedUserIds.contains(userEntity.id) }
                .map { userEntity: UserEntity -> userEntity.toUser() }
            
            potentialMatches
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getUsersForSwipe(
        minAge: Int,
        maxAge: Int,
        maxDistance: Float,
        userLat: Double,
        userLon: Double
    ): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            Log.d("SwipeRepository", "getUsersForSwipe - currentUserId: $currentUserId, minAge: $minAge, maxAge: $maxAge, maxDistance: $maxDistance")
            Log.d("SwipeRepository", "User location: lat=$userLat, lon=$userLon")
            
            val allUsers = userDao.getAllUsers()
            Log.d("SwipeRepository", "Total users in database: ${allUsers.size}")
            
            val usersWithoutSwipeFilter = userDao.getUsersForSwipeWithoutSwipeFilter(currentUserId, minAge, maxAge)
            Log.d("SwipeRepository", "Users matching age filter (excluding current user): ${usersWithoutSwipeFilter.size}")
            
            val existingSwipes = swipeDao.getSwipesByUser(currentUserId)
            Log.d("SwipeRepository", "Existing swipes for user $currentUserId: ${existingSwipes.size}")
            existingSwipes.forEach { swipe ->
                Log.d("SwipeRepository", "  - Swiped user ${swipe.targetUserId}: ${swipe.action}")
            }
            
            val users = userDao.getUsersForSwipeFiltered(currentUserId, minAge, maxAge)
            Log.d("SwipeRepository", "Found ${users.size} users after age/swipe filter")
            
            val filteredUsers = users
                .map { it.toUser() }
                .filter { user ->
                    val lat = user.latitude ?: 0.0
                    val lon = user.longitude ?: 0.0
                    val distance = calculateDistance(userLat, userLon, lat, lon)
                    Log.d("SwipeRepository", "User ${user.id} (${user.firstname}): lat=$lat, lon=$lon, distance=${distance}km, maxDistance=${maxDistance}km")
                    distance <= maxDistance
                }
            
            Log.d("SwipeRepository", "After distance filter: ${filteredUsers.size} users")
            Result.success(filteredUsers)
        } catch (e: Exception) {
            Log.e("SwipeRepository", "Error in getUsersForSwipe", e)
            Result.failure(e)
        }
    }
    
    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000
    }
}