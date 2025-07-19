package com.example.tailstale.repository

import android.util.Log
import com.example.tailstale.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val TAG = "UserRepository"
    }

    /**
     * Get current user's profile from Firestore
     */
    suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.success(null)
            }

            val document = firestore
                .collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .get()
                .await()

            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)
                Result.success(profile)
            } else {
                // Create default profile if doesn't exist
                val defaultProfile = createDefaultProfile(currentUser.uid)
                saveUserProfile(defaultProfile)
                Result.success(defaultProfile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Save user profile to Firestore
     */
    suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            firestore
                .collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .set(userProfile, SetOptions.merge())
                .await()

            Log.d(TAG, "User profile saved successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Update specific fields in user profile
     */
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val updatesWithTimestamp = updates.toMutableMap()
            updatesWithTimestamp["updatedAt"] = System.currentTimeMillis()

            firestore
                .collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .update(updatesWithTimestamp)
                .await()

            Log.d(TAG, "User profile updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Update profile image URL
     */
    suspend fun updateProfileImage(imageUrl: String): Result<Unit> {
        return updateUserProfile(mapOf("profileImageUrl" to imageUrl))
    }

    /**
     * Add new pet to user profile
     */
    suspend fun addPet(pet: UserProfile.Pet): Result<Unit> {
        return try {
            val currentProfile = getCurrentUserProfile().getOrNull()
            if (currentProfile != null) {
                val updatedPets = currentProfile.pets + pet
                val updates = mapOf("pets" to updatedPets)
                updateUserProfile(updates)
            } else {
                Result.failure(Exception("Could not get current profile"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding pet", e)
            Result.failure(e)
        }
    }

    /**
     * Add achievement to user profile
     */
    suspend fun addAchievement(achievement: UserProfile.Achievement): Result<Unit> {
        return try {
            val currentProfile = getCurrentUserProfile().getOrNull()
            if (currentProfile != null) {
                val existingAchievements = currentProfile.achievements
                if (!existingAchievements.any { it.id == achievement.id }) {
                    val updatedAchievements = existingAchievements + achievement
                    val updates = mapOf("achievements" to updatedAchievements)
                    updateUserProfile(updates)
                } else {
                    Result.success(Unit) // Achievement already exists
                }
            } else {
                Result.failure(Exception("Could not get current profile"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding achievement", e)
            Result.failure(e)
        }
    }

    /**
     * Update user statistics
     */
    suspend fun updateStats(stats: UserProfile.Stats): Result<Unit> {
        return updateUserProfile(mapOf("stats" to stats))
    }

    /**
     * Update user settings
     */
    suspend fun updateSettings(settings: UserProfile.Settings): Result<Unit> {
        return updateUserProfile(mapOf("settings" to settings))
    }

    /**
     * Create default profile for new users
     */
    private fun createDefaultProfile(userId: String): UserProfile {
        val currentUser = auth.currentUser
        return UserProfile(
            id = userId,
            name = currentUser?.displayName ?: "Pet Lover",
            email = currentUser?.email ?: "",
            bio = "Welcome to TailsTale! 🐾",
            location = "",
            profileImageUrl = currentUser?.photoUrl?.toString() ?: "",
            pets = emptyList(),
            stats = UserProfile.Stats(
                daysActive = 1,
                totalCare = 0,
                level = 1
            ),
            achievements = emptyList(),
            settings = UserProfile.Settings(
                notifications = true,
                darkMode = false,
                locationServices = false,
                autoUpdates = true,
                soundEnabled = true
            ),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
