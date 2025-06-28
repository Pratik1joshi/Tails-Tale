package com.example.tailstale.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val difficulty: AchievementDifficulty,
    val rewardCoins: Int = 0,
    val rewardGems: Int = 0,
    val rewardExperience: Int = 0,
    var isUnlocked: Boolean = false,
    var dateUnlocked: Long? = null,
    val requirements: Map<String, Any> = emptyMap() // e.g., "feedCount" to 50
)

enum class AchievementCategory(val displayName: String) {
    CARETAKER("Pet Caretaker"),
    HEALTH("Health Expert"),
    TRAINER("Pet Trainer"),
    COLLECTOR("Collector"),
    SOCIAL("Social")
}

enum class AchievementDifficulty(val displayName: String, val multiplier: Double) {
    EASY("Easy", 1.0),
    MEDIUM("Medium", 1.5),
    HARD("Hard", 2.0),
    LEGENDARY("Legendary", 3.0)
}