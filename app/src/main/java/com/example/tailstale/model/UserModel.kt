package com.example.tailstale.model

import java.util.UUID

data class UserModel(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val coins: Int = 100,
    val gems: Int = 5,
    val level: Int = 1,
    val experience: Int = 0,
    val lastLoginDate: Long = System.currentTimeMillis(),
    val creationDate: Long = System.currentTimeMillis(),
    val pets: List<String> = emptyList(), // Use List instead of MutableList
    val achievements: List<Achievement> = emptyList(), // Use List instead of MutableList
    val inventory: Map<String, Int> = emptyMap(), // Simple map for inventory
    val learningProgress: Map<String, Any> = emptyMap() // Simple map for progress
)

data class Inventory(
    val food: MutableMap<String, Int> = mutableMapOf(),
    val toys: MutableMap<String, Int> = mutableMapOf(),
    val vaccines: MutableMap<String, Int> = mutableMapOf()
)

enum class ItemCategory { FOOD, TOY, VACCINE }