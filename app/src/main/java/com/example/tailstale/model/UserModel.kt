package com.example.tailstale.model

import java.util.UUID

data class UserModel(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val bio: String = "", // Added bio property
    val location: String = "", // Added location property
    val lastLoginDate: Long = System.currentTimeMillis(),
    val creationDate: Long = System.currentTimeMillis(),
    val pets: List<String> = emptyList(), // Use List instead of MutableList
    val achievements: List<Achievement> = emptyList(), // Use List instead of MutableList
    val petCareStats: Map<String, Int> = emptyMap(), // Pet care statistics (feedCount, playCount, etc.)
    val learningProgress: Map<String, Any> = emptyMap() // Simple map for progress
)

data class Inventory(
    val food: MutableMap<String, Int> = mutableMapOf(),
    val toys: MutableMap<String, Int> = mutableMapOf(),
    val vaccines: MutableMap<String, Int> = mutableMapOf()
)

enum class ItemCategory { FOOD, TOY, VACCINE }