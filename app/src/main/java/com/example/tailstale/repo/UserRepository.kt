package com.example.tailstale.repo

import com.example.tailstale.model.UserModel

interface UserRepository {
    suspend fun getUserById(userId: String): UserModel?
    suspend fun updateUser(user: UserModel)
    suspend fun createUser(user: UserModel)
    suspend fun getAllUsers(): List<UserModel>
}