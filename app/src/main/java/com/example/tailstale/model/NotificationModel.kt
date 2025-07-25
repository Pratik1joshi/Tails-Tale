package com.example.tailstale.model

import java.util.UUID

data class NotificationModel(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val priority: NotificationPriority,
    val timestamp: Long = System.currentTimeMillis(),
    val petId: String? = null,
    val petName: String? = null,
    val isRead: Boolean = false,
    val actionType: String? = null, // For navigation or action buttons
    val icon: String? = null,
    val color: String? = null,
    val expiresAt: Long? = null // Auto-dismiss time
)

enum class NotificationType(val displayName: String, val emoji: String) {
    HEALTH_CRITICAL("Critical Health", "🚨"),
    HEALTH_WARNING("Health Warning", "⚠️"),
    VACCINATION("Vaccination", "💉"),
    DISEASE("Disease Alert", "🦠"),
    HUNGER("Hunger", "🍽️"),
    THIRST("Thirst", "💧"),
    ENERGY("Energy", "😴"),
    CLEANLINESS("Cleanliness", "🛁"),
    HAPPINESS("Happiness", "😢"),
    AGING("Pet Aging", "📅"),
    ACHIEVEMENT("Achievement", "🏆"),
    REMINDER("Reminder", "🔔"),
    SYSTEM("System", "ℹ️")
}

enum class NotificationPriority(val level: Int, val color: String) {
    CRITICAL(4, "#F44336"), // Red
    HIGH(3, "#FF9800"),     // Orange
    MEDIUM(2, "#2196F3"),   // Blue
    LOW(1, "#4CAF50")       // Green
}
