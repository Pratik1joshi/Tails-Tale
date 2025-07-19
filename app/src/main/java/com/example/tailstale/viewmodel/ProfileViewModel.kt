package com.example.tailstale.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailstale.model.UserModel
import com.example.tailstale.model.Achievement
import com.example.tailstale.model.AchievementCategory
import com.example.tailstale.model.AchievementDifficulty
import com.example.tailstale.repo.UserRepository
import com.example.tailstale.service.CloudinaryService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val cloudinaryService = CloudinaryService()
    private val auth = FirebaseAuth.getInstance()

    // Add PetRepository - you'll need to inject this properly
    private val petRepository = com.example.tailstale.repo.PetRepositoryImpl()

    var userProfile by mutableStateOf<UserModel?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isUploading by mutableStateOf(false)
        private set

    var isInitialized by mutableStateOf(false)
        private set

    // Add pets state
    var userPets by mutableStateOf<List<com.example.tailstale.model.PetModel>>(emptyList())
        private set

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    init {
        loadUserProfile()
    }

    /**
     * Load user profile from Firebase using existing repository
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val result = userRepository.getUserById(currentUser.uid)
                    result.fold(
                        onSuccess = { user ->
                            userProfile = user
                            isInitialized = true
                            // Load pets after loading user profile
                            user?.let { loadUserPets(it.pets) }
                            Log.d(TAG, "Profile loaded successfully: ${user?.displayName}")
                        },
                        onFailure = { error ->
                            errorMessage = "Failed to load profile: ${error.message}"
                            Log.e(TAG, "Error loading profile", error)
                            // Try to create a default profile if user doesn't exist
                            createDefaultProfile()
                        }
                    )
                } else {
                    errorMessage = "User not authenticated"
                    isInitialized = false
                }
            } catch (e: Exception) {
                errorMessage = "Unexpected error: ${e.message}"
                Log.e(TAG, "Unexpected error loading profile", e)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Load pets data from pet IDs
     */
    private fun loadUserPets(petIds: List<String>) {
        viewModelScope.launch {
            try {
                val pets = mutableListOf<com.example.tailstale.model.PetModel>()
                petIds.forEach { petId ->
                    petRepository.getPetById(petId).fold(
                        onSuccess = { pet ->
                            pet?.let { pets.add(it) }
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error loading pet $petId: ${error.message}")
                        }
                    )
                }
                userPets = pets
                Log.d(TAG, "Loaded ${pets.size} pets")
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading pets", e)
            }
        }
    }

    /**
     * Update user profile
     */
    fun updateProfile(displayName: String, email: String = "", profileImageUrl: String = "") {
        val currentUser = userProfile ?: return

        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val updatedUser = currentUser.copy(
                    displayName = displayName,
                    email = if (email.isNotBlank()) email else currentUser.email,
                    profileImageUrl = if (profileImageUrl.isNotBlank()) profileImageUrl else currentUser.profileImageUrl
                )

                val result = userRepository.updateUser(updatedUser)
                result.fold(
                    onSuccess = { user ->
                        userProfile = user
                        Log.d(TAG, "Profile updated successfully")
                    },
                    onFailure = { error ->
                        errorMessage = "Failed to update profile: ${error.message}"
                        Log.e(TAG, "Error updating profile", error)
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to update profile: ${e.message}"
                Log.e(TAG, "Exception updating profile", e)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Upload profile image to Cloudinary and update user
     */
    fun uploadProfileImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                isUploading = true
                errorMessage = null

                // Initialize Cloudinary if not already done
                CloudinaryService.initialize(context)

                // Upload image to Cloudinary
                val imageUrl = cloudinaryService.uploadImage(imageUri, "profile_images")
                Log.d(TAG, "Image uploaded to Cloudinary: $imageUrl")

                // Update user profile with new image URL
                val currentUser = userProfile ?: return@launch
                val updatedUser = currentUser.copy(profileImageUrl = imageUrl)

                val result = userRepository.updateUser(updatedUser)
                result.fold(
                    onSuccess = { user ->
                        userProfile = user
                        Log.d(TAG, "Profile image updated successfully")
                    },
                    onFailure = { error ->
                        errorMessage = "Failed to update profile image: ${error.message}"
                        Log.e(TAG, "Error updating profile image", error)
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to upload image: ${e.message}"
                Log.e(TAG, "Exception uploading image", e)
            } finally {
                isUploading = false
            }
        }
    }

    /**
     * Create default profile for new users
     */
    private fun createDefaultProfile() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val defaultUser = UserModel(
                    id = currentUser.uid,
                    username = currentUser.displayName ?: "User",
                    email = currentUser.email ?: "",
                    displayName = currentUser.displayName ?: "New User",
                    profileImageUrl = currentUser.photoUrl?.toString() ?: "",
                    bio = "",
                    location = "",
                    lastLoginDate = System.currentTimeMillis(),
                    creationDate = System.currentTimeMillis(),
                    pets = emptyList(),
                    achievements = emptyList(),
                    petCareStats = emptyMap(),
                    learningProgress = emptyMap()
                )

                val result = userRepository.createUser(defaultUser)
                result.fold(
                    onSuccess = { user ->
                        userProfile = user
                        isInitialized = true
                        Log.d(TAG, "Default profile created successfully")
                    },
                    onFailure = { error ->
                        errorMessage = "Failed to create profile: ${error.message}"
                        Log.e(TAG, "Error creating default profile", error)
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to create profile: ${e.message}"
                Log.e(TAG, "Exception creating default profile", e)
            }
        }
    }

    /**
     * Update pet care statistics
     */
    fun updatePetCareStats(statType: String, increment: Int = 1) {
        val currentUser = userProfile ?: return

        viewModelScope.launch {
            try {
                val currentStats = currentUser.petCareStats.toMutableMap()
                val currentValue = currentStats[statType] ?: 0
                currentStats[statType] = currentValue + increment

                val result = userRepository.updatePetCareStats(currentUser.id, currentStats)

                result.fold(
                    onSuccess = {
                        userProfile = currentUser.copy(petCareStats = currentStats)
                        Log.d(TAG, "Pet care stats updated successfully: $statType = ${currentStats[statType]}")
                    },
                    onFailure = { error ->
                        errorMessage = "Failed to update pet care stats: ${error.message}"
                        Log.e(TAG, "Error updating pet care stats", error)
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to update pet care stats: ${e.message}"
                Log.e(TAG, "Exception updating pet care stats", e)
            }
        }
    }

    /**
     * Add achievement
     */
    fun addAchievement(title: String, description: String) {
        val currentUser = userProfile ?: return

        viewModelScope.launch {
            try {
                val achievement = Achievement(
                    id = System.currentTimeMillis().toString(),
                    name = title,
                    description = description,
                    category = AchievementCategory.CARE, // Default to CARE category
                    difficulty = AchievementDifficulty.BEGINNER, // Default to BEGINNER difficulty
                    isUnlocked = true,
                    dateUnlocked = System.currentTimeMillis(),
                    requirements = emptyMap()
                )

                val result = userRepository.addAchievement(currentUser.id, achievement)

                result.fold(
                    onSuccess = {
                        val updatedAchievements = currentUser.achievements + achievement
                        userProfile = currentUser.copy(achievements = updatedAchievements)
                        Log.d(TAG, "Achievement added successfully: $title")
                    },
                    onFailure = { error ->
                        errorMessage = "Failed to add achievement: ${error.message}"
                        Log.e(TAG, "Error adding achievement", error)
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to add achievement: ${e.message}"
                Log.e(TAG, "Exception adding achievement", e)
            }
        }
    }

    /**
     * Refresh profile from database
     */
    fun refreshProfile() {
        loadUserProfile()
    }

    /**
     * Sign out user
     */
    fun signOut() {
        try {
            auth.signOut()
            userProfile = null
            isInitialized = false
            errorMessage = null
            Log.d(TAG, "User signed out successfully")
        } catch (e: Exception) {
            errorMessage = "Failed to sign out: ${e.message}"
            Log.e(TAG, "Exception signing out", e)
        }
    }

    /**
     * Set error message
     */
    fun setError(message: String) {
        errorMessage = message
    }

    /**
     * Clear error message
     */
    fun clearError() {
        errorMessage = null
    }
}
