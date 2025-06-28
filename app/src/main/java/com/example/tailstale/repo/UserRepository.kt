package com.example.tailstale.repo

import com.example.tailstale.model.Achievement
import com.example.tailstale.model.LearningProgress
import com.example.tailstale.model.UserModel

interface UserRepository {
    suspend fun createUser(user: UserModel): Result<UserModel>
    suspend fun getUserById(userId: String): Result<UserModel?>
    suspend fun getUserByEmail(email: String): Result<UserModel?>
    suspend fun updateUser(user: UserModel): Result<UserModel>
    suspend fun deleteUser(userId: String): Result<Boolean>
    suspend fun updateCoins(userId: String, coins: Int): Result<Boolean>
    suspend fun updateGems(userId: String, gems: Int): Result<Boolean>
    suspend fun updateExperience(userId: String, experience: Int): Result<Boolean>
    suspend fun addAchievement(userId: String, achievement: Achievement): Result<Boolean>
    suspend fun updateLearningProgress(userId: String, progress: LearningProgress): Result<Boolean>
}