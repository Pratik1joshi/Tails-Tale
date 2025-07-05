package com.example.tailstale.repo

import com.example.tailstale.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<UserModel>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<UserModel>
    suspend fun signInWithGoogle(credential: AuthCredential): Result<UserModel>
    suspend fun signInWithGithub(credential: AuthCredential): Result<UserModel>
    suspend fun resetPassword(email: String): Result<Boolean>
    suspend fun signOut(): Result<Boolean>
    suspend fun getCurrentUser(): Result<UserModel?>
}