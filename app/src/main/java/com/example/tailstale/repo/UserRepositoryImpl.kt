package com.example.tailstale.repo

import com.example.tailstale.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(private val db: FirebaseFirestore) : UserRepository {
    override suspend fun getUserById(userId: String): UserModel? {
        val snapshot = db.collection("users").document(userId).get().await()
        return snapshot.toObject(UserModel::class.java)
    }

    override suspend fun updateUser(user: UserModel) {
        db.collection("users").document(user.id).set(user).await()
    }

    override suspend fun createUser(user: UserModel) {
        db.collection("users").document(user.id).set(user).await()
    }

    override suspend fun getAllUsers(): List<UserModel> {
        val snapshot = db.collection("users").get().await()
        return snapshot.toObjects(UserModel::class.java)
    }
}