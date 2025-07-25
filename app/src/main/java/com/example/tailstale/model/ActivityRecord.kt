package com.example.tailstale.model

import java.util.UUID

data class ActivityRecord(
    val id: String = UUID.randomUUID().toString(),
    val petId: String = "",
    val petName: String = "",
    val activityType: ActivityType = ActivityType.FEEDING, // Default value for Firebase
    val activityName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0, // Duration in milliseconds
    val details: Map<String, Any> = emptyMap(), // Additional activity details
    val statsChanged: Map<String, Int> = emptyMap(), // What stats were affected
    val videoPlayed: String? = null, // Which video was played for this activity
    val success: Boolean = true // Whether the activity was completed successfully
) {
    // No-arg constructor for Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        petId = "",
        petName = "",
        activityType = ActivityType.FEEDING,
        activityName = "",
        timestamp = System.currentTimeMillis(),
        duration = 0,
        details = emptyMap(),
        statsChanged = emptyMap(),
        videoPlayed = null,
        success = true
    )

    fun getFormattedTime(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }

    fun getFormattedDate(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return format.format(date)
    }

    fun getDurationText(): String {
        return if (duration > 0) {
            val seconds = duration / 1000
            val minutes = seconds / 60
            when {
                minutes > 0 -> "${minutes}m ${seconds % 60}s"
                else -> "${seconds}s"
            }
        } else {
            "Instant"
        }
    }
}

enum class ActivityType(val displayName: String, val emoji: String, val description: String) {
    FEEDING("Feeding", "🍽️", "Pet was fed"),
    PLAYING("Playing", "🎾", "Pet played and had fun"),
    CLEANING("Cleaning", "🛁", "Pet was cleaned"),
    SLEEPING("Sleeping", "😴", "Pet took a rest"),
    WALKING("Walking", "🚶", "Pet went for a walk"),
    SITTING("Sitting", "🪑", "Pet sat quietly"),
    BATHING("Bathing", "🛀", "Pet had a bath"),
    HEALTH_CHECK("Health Check", "🏥", "Pet received medical attention"),
    EXERCISE("Exercise", "🏃", "Pet did physical exercise"),
    VACCINATION("Vaccination", "💉", "Pet received vaccination"),
    GROOMING("Grooming", "✂️", "Pet was groomed"),
    TRAINING("Training", "🎓", "Pet learned new skills"),
    SOCIALIZING("Socializing", "👥", "Pet interacted with others"),
    AGING("Aging", "📅", "Pet aged up naturally"),
    ILLNESS("Illness", "🤒", "Pet became ill"),
    RECOVERY("Recovery", "💚", "Pet recovered from illness")
}
