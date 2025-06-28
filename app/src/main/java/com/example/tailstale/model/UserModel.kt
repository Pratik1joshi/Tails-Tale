package com.example.tailstale.model

import java.util.UUID

data class UserModel(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    var displayName: String,
    var profileImageUrl: String = "",
    var coins: Int = 100,
    var gems: Int = 5,
    var level: Int = 1,
    var experience: Int = 0,
    var lastLoginDate: Long = System.currentTimeMillis(),
    var creationDate: Long = System.currentTimeMillis(),
    val pets: MutableList<String> = mutableListOf(),
    val inventory: Inventory = Inventory(),
    val achievements: MutableList<Achievement> = mutableListOf(),
    val learningProgress: LearningProgress = LearningProgress()
)

data class Inventory(
    val food: MutableMap<String, Int> = mutableMapOf(),
    val toys: MutableMap<String, Int> = mutableMapOf(),
    val vaccines: MutableMap<String, Int> = mutableMapOf()
)

enum class ItemCategory { FOOD, TOY, VACCINE }