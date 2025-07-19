package com.example.tailstale.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val difficulty: AchievementDifficulty,
    var isUnlocked: Boolean = false,
    var dateUnlocked: Long? = null,
    val requirements: Map<String, Any> = emptyMap() // e.g., "feedCount" to 15
)

enum class AchievementCategory(val displayName: String) {
    FEEDING("Feeding Expert"),
    HEALTH("Health Care"),
    TRAINING("Training Pro"),
    BONDING("Pet Bonding"),
    CARE("Daily Care")
}

enum class AchievementDifficulty(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    EXPERT("Expert")
}