package com.example.tailstale.repo

import com.example.tailstale.model.Achievement
import com.example.tailstale.model.LearningProgress
import com.example.tailstale.model.UserModel


class UserRepositoryImpl : UserRepository {
    // In a real app, this would connect to your database (Room, Firebase, etc.)
    private val users = mutableMapOf<String, UserModel>()

    override suspend fun createUser(user: UserModel): Result<UserModel> {
        return try {
            users[user.id] = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<UserModel?> {
        return try {
            Result.success(users[userId])
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByEmail(email: String): Result<UserModel?> {
        return try {
            val user = users.values.find { it.email == email }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: UserModel): Result<UserModel> {
        return try {
            users[user.id] = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Boolean> {
        return try {
            users.remove(userId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCoins(userId: String, coins: Int): Result<Boolean> {
        return try {
            users[userId]?.let { user ->
                users[userId] = user.copy(coins = coins)
                Result.success(true)
            } ?: Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGems(userId: String, gems: Int): Result<Boolean> {
        return try {
            users[userId]?.let { user ->
                users[userId] = user.copy(gems = gems)
                Result.success(true)
            } ?: Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExperience(userId: String, experience: Int): Result<Boolean> {
        return try {
            users[userId]?.let { user ->
                val newLevel = calculateLevel(experience)
                users[userId] = user.copy(experience = experience, level = newLevel)
                Result.success(true)
            } ?: Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addAchievement(userId: String, achievement: Achievement): Result<Boolean> {
        return try {
            users[userId]?.let { user ->
                user.achievements.add(achievement.copy(isUnlocked = true, dateUnlocked = System.currentTimeMillis()))
                Result.success(true)
            } ?: Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLearningProgress(userId: String, progress: LearningProgress): Result<Boolean> {
        return try {
            users[userId]?.let { user ->
                users[userId] = user.copy(learningProgress = progress)
                Result.success(true)
            } ?: Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateLevel(experience: Int): Int {
        return (experience / 100) + 1 // Simple level calculation
    }
}
