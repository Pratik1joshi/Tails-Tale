package com.example.tailstale.service

import com.example.tailstale.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationService {
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    /**
     * Generate notifications based on pet's current status
     */
    fun generatePetNotifications(pet: PetModel): List<NotificationModel> {
        val notifications = mutableListOf<NotificationModel>()
        val currentTime = System.currentTimeMillis()

        // Critical Health Notifications
        if (pet.health <= 20) {
            notifications.add(NotificationModel(
                title = "Critical Health Alert!",
                message = "${pet.name}'s health is critically low (${pet.health}%). Immediate medical attention needed!",
                type = NotificationType.HEALTH_CRITICAL,
                priority = NotificationPriority.CRITICAL,
                petId = pet.id,
                petName = pet.name,
                actionType = "health_check"
            ))
        } else if (pet.health <= 40) {
            notifications.add(NotificationModel(
                title = "Health Warning",
                message = "${pet.name}'s health is declining (${pet.health}%). Consider a health check.",
                type = NotificationType.HEALTH_WARNING,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "health_check"
            ))
        }

        // Hunger Notifications
        if (pet.hunger >= 80) {
            notifications.add(NotificationModel(
                title = "Starving Pet!",
                message = "${pet.name} is starving! They haven't eaten in a while. Feed them now!",
                type = NotificationType.HUNGER,
                priority = NotificationPriority.CRITICAL,
                petId = pet.id,
                petName = pet.name,
                actionType = "feed"
            ))
        } else if (pet.hunger >= 60) {
            notifications.add(NotificationModel(
                title = "Hungry Pet",
                message = "${pet.name} is getting hungry. Time for a meal!",
                type = NotificationType.HUNGER,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "feed"
            ))
        } else if (pet.hunger >= 40) {
            notifications.add(NotificationModel(
                title = "Snack Time",
                message = "${pet.name} could use a snack soon.",
                type = NotificationType.HUNGER,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "feed"
            ))
        }

        // Energy/Sleep Notifications
        if (pet.energy <= 20) {
            notifications.add(NotificationModel(
                title = "Exhausted Pet!",
                message = "${pet.name} is exhausted and needs rest immediately!",
                type = NotificationType.ENERGY,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "sleep"
            ))
        } else if (pet.energy <= 40) {
            notifications.add(NotificationModel(
                title = "Tired Pet",
                message = "${pet.name} is getting tired. They could use some rest.",
                type = NotificationType.ENERGY,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "sleep"
            ))
        }

        // Cleanliness Notifications
        if (pet.cleanliness <= 20) {
            notifications.add(NotificationModel(
                title = "Very Dirty Pet!",
                message = "${pet.name} is very dirty and needs a bath urgently!",
                type = NotificationType.CLEANLINESS,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "clean"
            ))
        } else if (pet.cleanliness <= 40) {
            notifications.add(NotificationModel(
                title = "Bath Time",
                message = "${pet.name} is getting dirty. Time for a bath!",
                type = NotificationType.CLEANLINESS,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "clean"
            ))
        }

        // Happiness Notifications
        if (pet.happiness <= 20) {
            notifications.add(NotificationModel(
                title = "Sad Pet!",
                message = "${pet.name} is very sad and needs attention. Play with them!",
                type = NotificationType.HAPPINESS,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "play"
            ))
        } else if (pet.happiness <= 40) {
            notifications.add(NotificationModel(
                title = "Lonely Pet",
                message = "${pet.name} could use some playtime and attention.",
                type = NotificationType.HAPPINESS,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "play"
            ))
        }

        // Age-based notifications
        val daysSinceLastFed = if (pet.lastFed > 0) {
            (currentTime - pet.lastFed) / (1000 * 60 * 60 * 24)
        } else 0

        if (daysSinceLastFed >= 2) {
            notifications.add(NotificationModel(
                title = "Feeding Reminder",
                message = "${pet.name} hasn't been fed in ${daysSinceLastFed} days!",
                type = NotificationType.REMINDER,
                priority = NotificationPriority.CRITICAL,
                petId = pet.id,
                petName = pet.name,
                actionType = "feed"
            ))
        }

        return notifications
    }

    /**
     * Generate vaccination notifications
     */
    fun generateVaccinationNotifications(
        pet: PetModel,
        requiredVaccines: List<VaccineModel>,
        overdueVaccines: List<VaccineModel>
    ): List<NotificationModel> {
        val notifications = mutableListOf<NotificationModel>()

        // Critical overdue vaccines
        overdueVaccines.forEach { vaccine ->
            notifications.add(NotificationModel(
                title = "URGENT: Overdue Vaccine!",
                message = "${pet.name} is overdue for ${vaccine.name}. Risk of ${vaccine.targetDisease} infection!",
                type = NotificationType.VACCINATION,
                priority = NotificationPriority.CRITICAL,
                petId = pet.id,
                petName = pet.name,
                actionType = "vaccinate"
            ))
        }

        // Required vaccines
        requiredVaccines.forEach { vaccine ->
            notifications.add(NotificationModel(
                title = "Vaccination Due",
                message = "${pet.name} needs ${vaccine.name} vaccination. Cost: $${vaccine.cost}",
                type = NotificationType.VACCINATION,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "vaccinate"
            ))
        }

        return notifications
    }

    /**
     * Generate disease notifications
     */
    fun generateDiseaseNotifications(
        pet: PetModel,
        activeDiseases: List<Map<String, Any>>,
        riskAssessments: List<DiseaseRiskAssessment>
    ): List<NotificationModel> {
        val notifications = mutableListOf<NotificationModel>()

        // Active diseases
        activeDiseases.forEach { disease ->
            val diseaseName = disease["diseaseName"]?.toString() ?: "Unknown Disease"
            val severity = disease["severity"]?.toString() ?: "MILD"
            val daysSince = disease["daysSinceDiagnosis"] as? Int ?: 0

            val priority = when (severity) {
                "SEVERE" -> NotificationPriority.CRITICAL
                "MODERATE" -> NotificationPriority.HIGH
                else -> NotificationPriority.MEDIUM
            }

            notifications.add(NotificationModel(
                title = "Active Disease: $diseaseName",
                message = "${pet.name} has $diseaseName ($severity) for $daysSince days. Treatment needed!",
                type = NotificationType.DISEASE,
                priority = priority,
                petId = pet.id,
                petName = pet.name,
                actionType = "treat_disease"
            ))
        }

        // High-risk diseases
        riskAssessments.filter { it.isHighRisk }.take(3).forEach { risk ->
            notifications.add(NotificationModel(
                title = "Disease Risk Warning",
                message = "${pet.name} has ${risk.riskPercentage}% risk of ${risk.disease.name}. Consider vaccination.",
                type = NotificationType.DISEASE,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "vaccinate"
            ))
        }

        return notifications
    }

    /**
     * Generate aging notifications
     */
    fun generateAgingNotifications(pet: PetModel): List<NotificationModel> {
        val notifications = mutableListOf<NotificationModel>()

        // Age milestone notifications
        when (pet.age) {
            6 -> notifications.add(NotificationModel(
                title = "Vaccination Time!",
                message = "${pet.name} is 6 weeks old! Time for first DHPP vaccine.",
                type = NotificationType.AGING,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "vaccinate"
            ))
            12 -> notifications.add(NotificationModel(
                title = "Growing Up!",
                message = "${pet.name} is 3 months old! Time for adult activities and socialization.",
                type = NotificationType.AGING,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name
            ))
            52 -> notifications.add(NotificationModel(
                title = "Happy Birthday!",
                message = "${pet.name} is 1 year old! Consider annual health check.",
                type = NotificationType.AGING,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "health_check"
            ))
            84 -> notifications.add(NotificationModel(
                title = "Senior Pet Care",
                message = "${pet.name} is now 7 years old! Senior pets need special care and regular checkups.",
                type = NotificationType.AGING,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "health_check"
            ))
        }

        return notifications
    }

    /**
     * Generate system notifications
     */
    fun generateSystemNotifications(): List<NotificationModel> {
        return listOf(
            NotificationModel(
                title = "Welcome to TailsTale!",
                message = "Keep your virtual pet healthy and happy!",
                type = NotificationType.SYSTEM,
                priority = NotificationPriority.LOW,
                expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 hours
            )
        )
    }

    /**
     * Update notifications with new data
     */
    fun updateNotifications(newNotifications: List<NotificationModel>) {
        val currentTime = System.currentTimeMillis()

        // Filter out expired notifications
        val validNotifications = newNotifications.filter { notification ->
            notification.expiresAt == null || notification.expiresAt > currentTime
        }

        // Remove duplicates based on title and petId
        val uniqueNotifications = validNotifications.distinctBy { "${it.title}-${it.petId}" }

        // Sort by priority and timestamp
        val sortedNotifications = uniqueNotifications.sortedWith(
            compareByDescending<NotificationModel> { it.priority.level }
                .thenByDescending { it.timestamp }
        )

        _notifications.value = sortedNotifications
        _unreadCount.value = sortedNotifications.count { !it.isRead }
    }

    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: String) {
        _notifications.value = _notifications.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else notification
        }
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0
    }

    /**
     * Clear a notification
     */
    fun clearNotification(notificationId: String) {
        _notifications.value = _notifications.value.filter { it.id != notificationId }
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        _notifications.value = emptyList()
        _unreadCount.value = 0
    }
}
