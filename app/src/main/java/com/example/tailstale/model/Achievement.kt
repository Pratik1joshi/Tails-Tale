package com.example.tailstale.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val difficulty: AchievementDifficulty,
    var isUnlocked: Boolean = false,
    var dateUnlocked: Long? = null,
    val requirements: Map<String, Any> = emptyMap(), // e.g., "feedCount" to 15
    val icon: String = "🏆", // Achievement icon
    val rewardCoins: Int = 0 // Coins earned for unlocking
)

// User statistics for tracking achievements
data class UserStats(
    val userId: String = "",
    var feedCount: Int = 0,
    var playCount: Int = 0,
    var cleanCount: Int = 0,
    var sleepCount: Int = 0,
    var walkCount: Int = 0,
    var vaccineCount: Int = 0,
    var treatmentCount: Int = 0,
    var totalInteractions: Int = 0,
    var petsAdded: Int = 0,
    var daysActive: Int = 0,
    var consecutiveDays: Int = 0,
    var lastActiveDate: String = "",
    var highestPetHealth: Int = 0,
    var totalCoinsEarned: Int = 0,
    var achievementsUnlocked: Int = 0,
    val unlockedAchievements: MutableList<String> = mutableListOf(),
    var createdAt: Long = System.currentTimeMillis(),
    var lastUpdated: Long = System.currentTimeMillis()
)

enum class AchievementCategory(val displayName: String) {
    FEEDING("Feeding Expert"),
    HEALTH("Health Care"),
    TRAINING("Training Pro"),
    BONDING("Pet Bonding"),
    CARE("Daily Care"),
    MILESTONE("Milestones"),
    SPECIAL("Special Events")
}

enum class AchievementDifficulty(val displayName: String, val multiplier: Int) {
    BEGINNER("Beginner", 1),
    INTERMEDIATE("Intermediate", 2),
    ADVANCED("Advanced", 3),
    EXPERT("Expert", 5)
}