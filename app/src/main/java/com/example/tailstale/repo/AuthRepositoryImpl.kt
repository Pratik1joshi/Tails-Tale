package com.example.tailstale.repo

import com.example.tailstale.model.Inventory
import com.example.tailstale.model.PetModel
import com.example.tailstale.model.PetType
//import com.example.tailstale.model.PetType
import com.example.tailstale.model.UserModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): Result<UserModel> {
        return try {
            println("DEBUG: AuthRepository - signInWithEmail called for: $email")
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Authentication failed")
            println("DEBUG: AuthRepository - Firebase auth successful for: ${firebaseUser.uid}")

            // Try to get existing user data from database
            val userResult = userRepository.getUserByEmail(email)
            val user = userResult.getOrNull()
            println("DEBUG: AuthRepository - User from database: ${user?.displayName}")
            println("DEBUG: AuthRepository - User pets: ${user?.pets}")

            if (user != null) {
                println("DEBUG: AuthRepository - Returning existing user")
                Result.success(user)
            } else {
                println("DEBUG: AuthRepository - Creating new user model")
                // Create a new user model if not found in the database
                val newUser = createUserModelFromFirebaseUser(firebaseUser)
                userRepository.createUser(newUser).getOrThrow()
                println("DEBUG: AuthRepository - New user created: ${newUser.displayName}")
                Result.success(newUser)
            }
        } catch (e: Exception) {
            println("DEBUG: AuthRepository - signInWithEmail failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<UserModel> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // Update display name in Firebase Auth
            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            firebaseUser.updateProfile(profileUpdates).await()

            // Create user in our database with complete data - removed game references
            val newUser = UserModel(
                id = firebaseUser.uid,
                username = displayName,
                email = email,
                displayName = displayName,
                profileImageUrl = "",
                bio = "",
                location = "",
                lastLoginDate = System.currentTimeMillis(),
                creationDate = System.currentTimeMillis(),
                pets = emptyMap(), // Change to Map structure
                achievements = emptyList(),
                petCareStats = emptyMap(),
                learningProgress = emptyMap()
            )

            // Save to Firebase Realtime Database
            userRepository.createUser(newUser).getOrThrow()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // In AuthRepositoryImpl.kt
    override suspend fun signInWithGoogle(credential: AuthCredential): Result<UserModel> {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Create or get existing user - removed game references
                val user = UserModel(
                    id = firebaseUser.uid,
                    username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "",
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                    bio = "",
                    location = "",
                    lastLoginDate = System.currentTimeMillis(),
                    creationDate = System.currentTimeMillis(),
                    pets = emptyMap(), // Change to Map structure
                    achievements = emptyList(),
                    petCareStats = emptyMap(),
                    learningProgress = emptyMap()
                )

                // Save user to repository
                userRepository.createUser(user).getOrThrow()
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication failed - no user returned"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Google sign in failed: ${e.message}"))
        }
    }

    override suspend fun signInWithGithub(credential: AuthCredential): Result<UserModel> {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("GitHub authentication failed")

            // Check if user already exists in our database
            val userResult = userRepository.getUserById(firebaseUser.uid)
            val user = userResult.getOrNull()

            if (user != null) {
                Result.success(user)
            } else {
                // Create a new user in our database
                val newUser = createUserModelFromFirebaseUser(firebaseUser)
                userRepository.createUser(newUser).getOrThrow()
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Boolean> {
        return try {
            auth.signOut()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<UserModel?> {
        return try {
            println("DEBUG: AuthRepository - getCurrentUser called")
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                println("DEBUG: AuthRepository - Firebase user exists: ${firebaseUser.uid}")
                val result = userRepository.getUserById(firebaseUser.uid)
                val user = result.getOrNull()
                println("DEBUG: AuthRepository - User from database: ${user?.displayName}")
                println("DEBUG: AuthRepository - User pets: ${user?.pets}")
                result
            } else {
                println("DEBUG: AuthRepository - No Firebase user found")
                Result.success(null)
            }
        } catch (e: Exception) {
            println("DEBUG: AuthRepository - getCurrentUser failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun signUpWithCompleteData(
        email: String,
        password: String,
        displayName: String,
        petType: String,
        petName: String
    ): Result<UserModel> {
        return try {
            println("DEBUG: Starting signUpWithCompleteData")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // Update display name in Firebase Auth
            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            firebaseUser.updateProfile(profileUpdates).await()

            // Create the actual pet model
            val petModel = PetModel(
                name = petName,
                type = petType
            )
            println("DEBUG: Created pet model: ${petModel.name} with ID: ${petModel.id}")

            // Save pet to Firebase
            val petRepository = PetRepositoryImpl()
            val createdPet = petRepository.createPet(petModel).getOrThrow()
            println("DEBUG: Saved pet to database: ${createdPet.id}")

            // Link pet to user in userPets collection - this is crucial!
            petRepository.linkPetToUser(firebaseUser.uid, createdPet.id).getOrThrow()
            println("DEBUG: Linked pet ${createdPet.id} to user ${firebaseUser.uid}")

            // Create complete user data with the pet ID included
            val newUser = UserModel(
                id = firebaseUser.uid,
                username = displayName,
                email = email,
                displayName = displayName,
                profileImageUrl = "",
                bio = "",
                location = "",
                lastLoginDate = System.currentTimeMillis(),
                creationDate = System.currentTimeMillis(),
                pets = mapOf(createdPet.id to true), // Change to Map structure
                achievements = emptyList(),
                petCareStats = emptyMap(),
                learningProgress = emptyMap()
            )

            // Save user to Firebase Realtime Database
            userRepository.createUser(newUser).getOrThrow()
            println("DEBUG: Saved user to database with pets: ${newUser.pets}")

            Result.success(newUser)
        } catch (e: Exception) {
            println("DEBUG: Error in signUpWithCompleteData: ${e.message}")
            Result.failure(e)
        }
    }

    private fun createUserModelFromFirebaseUser(firebaseUser: FirebaseUser): UserModel {
        return UserModel(
            id = firebaseUser.uid,
            username = firebaseUser.email?.substringBefore('@') ?: firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "",
            profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
            bio = "",
            location = "",
            lastLoginDate = System.currentTimeMillis(),
            creationDate = System.currentTimeMillis(),
            pets = emptyMap(), // Change to Map structure
            achievements = emptyList(),
            petCareStats = emptyMap(),
            learningProgress = emptyMap()
        )
    }
}