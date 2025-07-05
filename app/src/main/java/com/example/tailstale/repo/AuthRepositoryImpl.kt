package com.example.tailstale.repo

import com.example.tailstale.model.Inventory
import com.example.tailstale.model.UserModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): Result<UserModel> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Authentication failed")

            // Try to get existing user data from database
            val userResult = userRepository.getUserByEmail(email)
            val user = userResult.getOrNull()

            if (user != null) {
                Result.success(user)
            } else {
                // Create a new user model if not found in the database
                val newUser = createUserModelFromFirebaseUser(firebaseUser)
                userRepository.createUser(newUser).getOrThrow()
                Result.success(newUser)
            }
        } catch (e: Exception) {
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

            // Create user in our database
            val newUser = UserModel(
                id = firebaseUser.uid,
                username = email.substringBefore('@'),
                email = email,
                displayName = displayName
            )

            userRepository.createUser(newUser).getOrThrow()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(credential: AuthCredential): Result<UserModel> {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google authentication failed")

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
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            userRepository.getUserById(firebaseUser.uid)
        } else {
            Result.success(null)
        }
    }

    private fun createUserModelFromFirebaseUser(firebaseUser: FirebaseUser): UserModel {
        return UserModel(
            id = firebaseUser.uid,
            username = firebaseUser.email?.substringBefore('@') ?: firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "",
            profileImageUrl = firebaseUser.photoUrl?.toString() ?: ""
        )
    }
}