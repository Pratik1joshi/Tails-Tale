package com.example.tailstale.service

import com.example.tailstale.model.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AchievementManager {
    // Change from Firestore to Realtime Database
    private val database = FirebaseDatabase.getInstance()
    private val userStatsRef = database.getReference("userStats")
    private val achievementsRef = database.getReference("achievements")

    // All available achievements
    private val availableAchievements = listOf(
        // Beginner Achievements
        Achievement(
            id = "first_pet",
            name = "Pet Parent",
            description = "Welcome your first furry friend!",
            category = AchievementCategory.MILESTONE,
            difficulty = AchievementDifficulty.BEGINNER,
            requirements = mapOf("petsAdded" to 1),
            icon = "🐾",
            rewardCoins = 50
        ),
        Achievement(
            id = "first_feed",
            name = "First Meal",
            description = "Feed your pet for the first time",
            category = AchievementCategory.FEEDING,
            difficulty = AchievementDifficulty.BEGINNER,
            requirements = mapOf("feedCount" to 1),
            icon = "🍽️",
            rewardCoins = 25
        ),
        Achievement(
            id = "first_play",
            name = "Playtime Fun",
            description = "Play with your pet for the first time",
            category = AchievementCategory.BONDING,
            difficulty = AchievementDifficulty.BEGINNER,
            requirements = mapOf("playCount" to 1),
            icon = "🎾",
            rewardCoins = 25
        ),
        Achievement(
            id = "first_walk",
            name = "First Steps",
            description = "Take your pet for their first walk",
            category = AchievementCategory.BONDING,
            difficulty = AchievementDifficulty.BEGINNER,
            requirements = mapOf("walkCount" to 1),
            icon = "👣",
            rewardCoins = 25
        ),

        // Intermediate Achievements
        Achievement(
            id = "regular_feeder",
            name = "Regular Feeder",
            description = "Feed your pet 10 times",
            category = AchievementCategory.FEEDING,
            difficulty = AchievementDifficulty.INTERMEDIATE,
            requirements = mapOf("feedCount" to 10),
            icon = "🥘",
            rewardCoins = 100
        ),
        Achievement(
            id = "playful_owner",
            name = "Playful Owner",
            description = "Play with your pet 15 times",
            category = AchievementCategory.BONDING,
            difficulty = AchievementDifficulty.INTERMEDIATE,
            requirements = mapOf("playCount" to 15),
            icon = "⚽",
            rewardCoins = 100
        ),
        Achievement(
            id = "clean_keeper",
            name = "Clean Keeper",
            description = "Keep your pet clean - wash them 10 times",
            category = AchievementCategory.CARE,
            difficulty = AchievementDifficulty.INTERMEDIATE,
            requirements = mapOf("cleanCount" to 10),
            icon = "🛁",
            rewardCoins = 100
        ),
        Achievement(
            id = "walking_enthusiast",
            name = "Walking Enthusiast",
            description = "Take your pet for 20 walks",
            category = AchievementCategory.BONDING,
            difficulty = AchievementDifficulty.INTERMEDIATE,
            requirements = mapOf("walkCount" to 20),
            icon = "🚶",
            rewardCoins = 150
        ),
        Achievement(
            id = "regular_walker",
            name = "Regular Walker",
            description = "Take your pet for 10 walks",
            category = AchievementCategory.BONDING,
            difficulty = AchievementDifficulty.INTERMEDIATE,
            requirements = mapOf("walkCount" to 10),
            icon = "🐕‍🦺",
            rewardCoins = 100
        ),

        // Advanced Achievements
        Achievement(
            id = "dedicated_caregiver",
            name = "Dedicated Caregiver",
            description = "Feed your pet 50 times - you're truly dedicated!",
            category = AchievementCategory.FEEDING,
            difficulty = AchievementDifficulty.ADVANCED,
            requirements = mapOf("feedCount" to 50),
            icon = "👨‍⚕️",
            rewardCoins = 250
        ),
        Achievement(
            id = "health_guardian",
            name = "Health Guardian",
            description = "Give your pet 5 treatments to keep them healthy",
            category = AchievementCategory.HEALTH,
            difficulty = AchievementDifficulty.ADVANCED,
            requirements = mapOf("treatmentCount" to 5),
            icon = "💊",
            rewardCoins = 200
        ),
        Achievement(
            id = "daily_care_week",
            name = "Week of Care",
            description = "Take care of your pet for 7 consecutive days",
            category = AchievementCategory.CARE,
            difficulty = AchievementDifficulty.ADVANCED,
            requirements = mapOf("consecutiveDays" to 7),
            icon = "📅",
            rewardCoins = 300
        ),
        Achievement(
            id = "interaction_master",
            name = "Interaction Master",
            description = "Interact with your pet 100 times",
            category = AchievementCategory.BONDING,
            difficulty = AchievementDifficulty.ADVANCED,
            requirements = mapOf("totalInteractions" to 100),
            icon = "🤝",
            rewardCoins = 300
        ),

        // Expert Achievements
        Achievement(
            id = "pet_whisperer",
            name = "Pet Whisperer",
            description = "Achieve the ultimate bond - 200 interactions!",
            category = AchievementCategory.BONDING,
            difficulty = AchievementDifficulty.EXPERT,
            requirements = mapOf("totalInteractions" to 200),
            icon = "🗣️",
            rewardCoins = 500
        ),
        Achievement(
            id = "feeding_legend",
            name = "Feeding Legend",
            description = "Feed your pet 100 times - legendary status!",
            category = AchievementCategory.FEEDING,
            difficulty = AchievementDifficulty.EXPERT,
            requirements = mapOf("feedCount" to 100),
            icon = "🍖",
            rewardCoins = 500
        ),
        Achievement(
            id = "perfect_health",
            name = "Perfect Health",
            description = "Maintain your pet's health at 100% for a week",
            category = AchievementCategory.HEALTH,
            difficulty = AchievementDifficulty.EXPERT,
            requirements = mapOf("highestPetHealth" to 100, "consecutiveDays" to 7),
            icon = "💪",
            rewardCoins = 750
        ),
        Achievement(
            id = "month_streak",
            name = "Monthly Champion",
            description = "Take care of your pet for 30 consecutive days",
            category = AchievementCategory.CARE,
            difficulty = AchievementDifficulty.EXPERT,
            requirements = mapOf("consecutiveDays" to 30),
            icon = "🏆",
            rewardCoins = 1000
        ),

        // Special Achievements
        Achievement(
            id = "early_bird",
            name = "Early Bird",
            description = "Feed your pet before 8 AM",
            category = AchievementCategory.SPECIAL,
            difficulty = AchievementDifficulty.INTERMEDIATE,
            requirements = mapOf("earlyMorningFeed" to 1),
            icon = "🌅",
            rewardCoins = 150
        ),
        Achievement(
            id = "night_owl",
            name = "Night Owl",
            description = "Take care of your pet after 10 PM",
            category = AchievementCategory.SPECIAL,
            difficulty = AchievementDifficulty.INTERMEDIATE,
            requirements = mapOf("lateNightCare" to 1),
            icon = "🦉",
            rewardCoins = 150
        )
    )

    suspend fun getUserStats(userId: String): UserStats {
        return suspendCancellableCoroutine { continuation ->
            userStatsRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userStats = if (snapshot.exists()) {
                        snapshot.getValue(UserStats::class.java) ?: UserStats(userId = userId)
                    } else {
                        UserStats(userId = userId)
                    }
                    continuation.resume(userStats)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }

    suspend fun updateUserStats(userId: String, action: String, additionalData: Map<String, Any> = emptyMap()): List<Achievement> {
        val userStats = getUserStats(userId)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val currentHour = java.time.LocalTime.now().hour

        // Add debug logging
        android.util.Log.d("AchievementManager", "📈 Updating user stats for action: $action")
        android.util.Log.d("AchievementManager", "📊 Current stats before update - Walk: ${userStats.walkCount}, Feed: ${userStats.feedCount}, Play: ${userStats.playCount}")

        // Update stats based on action
        when (action) {
            "feed" -> {
                userStats.feedCount++
                if (currentHour < 8) {
                    // Early morning feed for special achievement
                }
            }
            "play" -> userStats.playCount++
            "clean" -> userStats.cleanCount++
            "sleep" -> userStats.sleepCount++
            "walk" -> userStats.walkCount++
            "vaccinate" -> userStats.vaccineCount++
            "treat" -> userStats.treatmentCount++
            "addPet" -> userStats.petsAdded++
        }

        // Update total interactions
        if (action in listOf("feed", "play", "clean", "sleep", "walk")) {
            userStats.totalInteractions++
        }

        // Log stats after update
        android.util.Log.d("AchievementManager", "📊 Stats after update - Walk: ${userStats.walkCount}, Feed: ${userStats.feedCount}, Play: ${userStats.playCount}")

        // Update consecutive days and daily activity
        if (userStats.lastActiveDate != today) {
            if (userStats.lastActiveDate == LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)) {
                userStats.consecutiveDays++
            } else {
                userStats.consecutiveDays = 1
            }
            userStats.daysActive++
            userStats.lastActiveDate = today
        }

        // Update pet health if provided
        val petHealth = additionalData["petHealth"] as? Int
        if (petHealth != null && petHealth > userStats.highestPetHealth) {
            userStats.highestPetHealth = petHealth
        }

        userStats.lastUpdated = System.currentTimeMillis()

        // Save updated stats to Realtime Database
        try {
            userStatsRef.child(userId).setValue(userStats).await()
            android.util.Log.d("AchievementManager", "💾 Stats saved to Firebase Realtime Database")
        } catch (e: Exception) {
            android.util.Log.e("AchievementManager", "❌ Failed to save stats: ${e.message}")
        }

        // Check for new achievements
        return checkAndUnlockAchievements(userId, userStats)
    }

    private suspend fun checkAndUnlockAchievements(userId: String, userStats: UserStats): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()

        for (achievement in availableAchievements) {
            // Skip if already unlocked
            if (userStats.unlockedAchievements.contains(achievement.id)) {
                continue
            }

            // Check if requirements are met
            var requirementsMet = true
            for ((key, requiredValue) in achievement.requirements) {
                val currentValue = when (key) {
                    "feedCount" -> userStats.feedCount
                    "playCount" -> userStats.playCount
                    "cleanCount" -> userStats.cleanCount
                    "sleepCount" -> userStats.sleepCount
                    "walkCount" -> userStats.walkCount
                    "vaccineCount" -> userStats.vaccineCount
                    "treatmentCount" -> userStats.treatmentCount
                    "totalInteractions" -> userStats.totalInteractions
                    "petsAdded" -> userStats.petsAdded
                    "consecutiveDays" -> userStats.consecutiveDays
                    "daysActive" -> userStats.daysActive
                    "highestPetHealth" -> userStats.highestPetHealth
                    else -> 0
                }

                if (currentValue < (requiredValue as Int)) {
                    requirementsMet = false
                    break
                }
            }

            // Unlock achievement if requirements are met
            if (requirementsMet) {
                val unlockedAchievement = achievement.copy(
                    isUnlocked = true,
                    dateUnlocked = System.currentTimeMillis()
                )

                userStats.unlockedAchievements.add(achievement.id)
                userStats.achievementsUnlocked++
                userStats.totalCoinsEarned += achievement.rewardCoins

                newlyUnlocked.add(unlockedAchievement)

                android.util.Log.d("AchievementManager", "🎉 Achievement unlocked: ${achievement.name}")

                // Save the unlocked achievement
                saveUnlockedAchievement(userId, unlockedAchievement)
            }
        }

        // Update user stats with new achievements
        if (newlyUnlocked.isNotEmpty()) {
            try {
                userStatsRef.child(userId).setValue(userStats).await()
            } catch (e: Exception) {
                android.util.Log.e("AchievementManager", "Failed to update user stats with achievements: ${e.message}")
            }
        }

        return newlyUnlocked
    }

    private suspend fun saveUnlockedAchievement(userId: String, achievement: Achievement) {
        try {
            achievementsRef.child(userId).child("unlocked").child(achievement.id).setValue(achievement).await()
        } catch (e: Exception) {
            android.util.Log.e("AchievementManager", "Failed to save achievement: ${e.message}")
        }
    }

    suspend fun getUserAchievements(userId: String): List<Achievement> {
        return suspendCancellableCoroutine { continuation ->
            achievementsRef.child(userId).child("unlocked").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val achievements = mutableListOf<Achievement>()
                    for (child in snapshot.children) {
                        val achievement = child.getValue(Achievement::class.java)
                        if (achievement != null) {
                            achievements.add(achievement)
                        }
                    }
                    continuation.resume(achievements)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(emptyList())
                }
            })
        }
    }

    suspend fun getAllAvailableAchievements(): List<Achievement> {
        return availableAchievements
    }

    suspend fun getAchievementProgress(userId: String): Map<String, Float> {
        val userStats = getUserStats(userId)
        val progress = mutableMapOf<String, Float>()

        for (achievement in availableAchievements) {
            if (userStats.unlockedAchievements.contains(achievement.id)) {
                progress[achievement.id] = 1.0f
            } else {
                var totalProgress = 0f
                var requirementCount = 0

                for ((key, requiredValue) in achievement.requirements) {
                    val currentValue = when (key) {
                        "feedCount" -> userStats.feedCount
                        "playCount" -> userStats.playCount
                        "cleanCount" -> userStats.cleanCount
                        "sleepCount" -> userStats.sleepCount
                        "walkCount" -> userStats.walkCount
                        "vaccineCount" -> userStats.vaccineCount
                        "treatmentCount" -> userStats.treatmentCount
                        "totalInteractions" -> userStats.totalInteractions
                        "petsAdded" -> userStats.petsAdded
                        "consecutiveDays" -> userStats.consecutiveDays
                        "daysActive" -> userStats.daysActive
                        "highestPetHealth" -> userStats.highestPetHealth
                        else -> 0
                    }.toFloat()

                    val required = (requiredValue as Int).toFloat()
                    totalProgress += (currentValue / required).coerceAtMost(1.0f)
                    requirementCount++
                }

                progress[achievement.id] = if (requirementCount > 0) {
                    totalProgress / requirementCount
                } else {
                    0f
                }
            }
        }

        return progress
    }
}
