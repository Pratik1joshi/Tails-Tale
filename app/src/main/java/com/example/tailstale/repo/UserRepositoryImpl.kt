package com.example.tailstale.repo

import com.example.tailstale.model.Achievement
import com.example.tailstale.model.LearningProgress
import com.example.tailstale.model.UserModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl : UserRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

    override suspend fun createUser(user: UserModel): Result<UserModel> {
        return try {
            database.child(user.id).setValue(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<UserModel?> {
        return try {
            val snapshot = database.child(userId).get().await()
            val user = snapshot.getValue<UserModel>()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByEmail(email: String): Result<UserModel?> {
        return try {
            val query = database.orderByChild("email").equalTo(email)
            val snapshot = query.get().await()
            val user = snapshot.children.firstOrNull()?.getValue<UserModel>()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: UserModel): Result<UserModel> {
        return try {
            database.child(user.id).setValue(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Boolean> {
        return try {
            database.child(userId).removeValue().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePetCareStats(userId: String, stats: Map<String, Int>): Result<Boolean> {
        return try {
            database.child(userId).child("petCareStats").setValue(stats).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addAchievement(userId: String, achievement: Achievement): Result<Boolean> {
        return try {
            val achievementRef = database.child(userId).child("achievements").push()
            val updatedAchievement = achievement.copy(
                isUnlocked = true,
                dateUnlocked = System.currentTimeMillis()
            )
            achievementRef.setValue(updatedAchievement).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLearningProgress(userId: String, progress: LearningProgress): Result<Boolean> {
        return try {
            database.child(userId).child("learningProgress").setValue(progress).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}