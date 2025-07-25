package com.example.tailstale.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.tailstale.R
import com.example.tailstale.model.NotificationModel
import com.example.tailstale.model.NotificationPriority
import com.example.tailstale.model.NotificationType
import com.example.tailstale.model.PetModel
import com.example.tailstale.repo.PetRepositoryImpl
import com.example.tailstale.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class RealTimeNotificationMonitor private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: RealTimeNotificationMonitor? = null

        fun getInstance(): RealTimeNotificationMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RealTimeNotificationMonitor().also { INSTANCE = it }
            }
        }

        const val CHANNEL_ID_CRITICAL = "pet_critical_notifications"
        const val CHANNEL_ID_HEALTH = "pet_health_notifications"
        const val CHANNEL_ID_CARE = "pet_care_notifications"
        const val CHANNEL_ID_INFO = "pet_info_notifications"
    }

    private val _realTimeNotifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val realTimeNotifications: StateFlow<List<NotificationModel>> = _realTimeNotifications

    private val _criticalAlerts = MutableStateFlow<List<NotificationModel>>(emptyList())
    val criticalAlerts: StateFlow<List<NotificationModel>> = _criticalAlerts

    private val notificationService = NotificationService()
    private val petRepository = PetRepositoryImpl()
    private val petHealthService = com.example.tailstale.service.PetHealthService(
        com.example.tailstale.repo.DiseaseRepositoryImpl(),
        com.example.tailstale.repo.VaccineRepositoryImpl()
    )

    // SharedPreferences key for storing read notifications
    private val PREFS_NAME = "notification_read_status"
    private val READ_NOTIFICATIONS_KEY = "read_notifications"

    /**
     * Initialize notification channels for different priority levels
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Critical notifications (High priority, sound, vibration)
            val criticalChannel = NotificationChannel(
                CHANNEL_ID_CRITICAL,
                "Critical Pet Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical health alerts for your pet"
                enableVibration(true)
                setVibrationPattern(longArrayOf(0, 500, 200, 500))
                setShowBadge(true)
            }

            // Health notifications (Default priority)
            val healthChannel = NotificationChannel(
                CHANNEL_ID_HEALTH,
                "Pet Health",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Health updates and vaccination reminders"
                setShowBadge(true)
            }

            // Care notifications (Default priority)
            val careChannel = NotificationChannel(
                CHANNEL_ID_CARE,
                "Pet Care",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Feeding, cleaning, and play reminders"
                setShowBadge(true)
            }

            // Info notifications (Low priority)
            val infoChannel = NotificationChannel(
                CHANNEL_ID_INFO,
                "Pet Information",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General pet information and tips"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(listOf(
                criticalChannel, healthChannel, careChannel, infoChannel
            ))
        }
    }

    /**
     * Start real-time monitoring for a user's pets
     */
    fun startRealTimeMonitoring(context: Context, userId: String) {
        // Create notification channels
        createNotificationChannels(context)

        // Schedule periodic background checks
        scheduleBackgroundMonitoring(context, userId)

        // Start immediate monitoring
        scheduleImmediateCheck(context, userId)
    }

    /**
     * Schedule immediate notification check
     */
    private fun scheduleImmediateCheck(context: Context, userId: String) {
        val immediateWork = OneTimeWorkRequestBuilder<PetNotificationWorker>()
            .setInputData(workDataOf("userId" to userId))
            .build()

        WorkManager.getInstance(context).enqueue(immediateWork)
    }

    /**
     * Schedule periodic background monitoring
     */
    private fun scheduleBackgroundMonitoring(context: Context, userId: String) {
        val periodicWork = PeriodicWorkRequestBuilder<PetNotificationWorker>(
            15, TimeUnit.MINUTES // Check every 15 minutes
        )
            .setInputData(workDataOf("userId" to userId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "pet_monitoring_$userId",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWork
        )
    }

    /**
     * Stop monitoring for a user
     */
    fun stopMonitoring(context: Context, userId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("pet_monitoring_$userId")
    }

    /**
     * Mark notification as read to prevent regeneration
     */
    fun markAsRead(context: Context, notificationId: String) {
        _realTimeNotifications.value = _realTimeNotifications.value.map { notification ->
            if (notification.id == notificationId) {
                // Mark as read persistently
                markNotificationAsReadPersistent(context, notification)
                notification.copy(isRead = true)
            } else notification
        }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead(context: Context) {
        _realTimeNotifications.value = _realTimeNotifications.value.map { notification ->
            // Mark each notification as read persistently
            markNotificationAsReadPersistent(context, notification)
            notification.copy(isRead = true)
        }
    }

    /**
     * Clear a specific notification
     */
    fun clearNotification(notificationId: String) {
        _realTimeNotifications.value = _realTimeNotifications.value.filter { it.id != notificationId }
    }

    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        _realTimeNotifications.value = emptyList()
        _criticalAlerts.value = emptyList()
    }

    /**
     * Enhanced notification generation that respects read status and cleared notifications
     */
    suspend fun generateRealTimeNotifications(context: Context, userId: String): List<NotificationModel> {
        val allNotifications = mutableListOf<NotificationModel>()

        try {
            // Get user's pets
            petRepository.getPetsByUserId(userId).fold(
                onSuccess = { pets ->
                    android.util.Log.d("RealTimeNotificationMonitor", "🐾 Found ${pets.size} pets for user $userId")

                    pets.forEach { pet ->
                        android.util.Log.d("RealTimeNotificationMonitor",
                            "📊 Pet: ${pet.name} - Health:${pet.health}% Hunger:${pet.hunger}% Energy:${pet.energy}% Happiness:${pet.happiness}% Cleanliness:${pet.cleanliness}%")

                        // Generate notifications for each pet
                        val petNotifications = generatePetRealTimeNotifications(pet)
                            .filter { newNotification ->
                                // Check persistent storage first - if marked as read, don't include
                                if (isNotificationRead(context, newNotification)) {
                                    return@filter false
                                }

                                // Also check current in-memory state for immediate updates
                                val existingNotification = _realTimeNotifications.value.find {
                                    it.title == newNotification.title &&
                                            it.petId == newNotification.petId &&
                                            it.type == newNotification.type
                                }

                                // Only include if not already read in memory either
                                existingNotification == null || !existingNotification.isRead
                            }

                        android.util.Log.d("RealTimeNotificationMonitor",
                            "🔔 Generated ${petNotifications.size} notifications for ${pet.name}")

                        allNotifications.addAll(petNotifications)

                        // Send system notifications for critical alerts (only if not already read)
                        petNotifications.filter {
                            it.priority == NotificationPriority.CRITICAL &&
                            !isNotificationRead(context, it)
                        }.forEach { notification ->
                            android.util.Log.d("RealTimeNotificationMonitor",
                                "🚨 Sending CRITICAL notification: ${notification.title}")
                            sendSystemNotification(context, notification)
                        }
                    }

                    if (pets.isEmpty()) {
                        android.util.Log.w("RealTimeNotificationMonitor", "⚠️ No pets found for user $userId")
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("RealTimeNotificationMonitor", "Failed to load pets: ${error.message}")
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("RealTimeNotificationMonitor", "Error generating notifications: ${e.message}")
        }

        // Update state flows with filtered notifications
        updateNotifications(context, allNotifications)

        android.util.Log.d("RealTimeNotificationMonitor",
            "📱 Total notifications generated: ${allNotifications.size}")

        return allNotifications
    }

    /**
     * Update notifications while preserving read status
     */
    private fun updateNotifications(context: Context, newNotifications: List<NotificationModel>) {
        val currentTime = System.currentTimeMillis()
        val existingNotifications = _realTimeNotifications.value

        // Merge new notifications with existing ones, preserving read status
        val mergedNotifications = newNotifications.map { newNotification ->
            val existing = existingNotifications.find {
                it.title == newNotification.title &&
                        it.petId == newNotification.petId &&
                        it.type == newNotification.type
            }

            if (existing != null && existing.isRead) {
                // Keep the existing read status
                existing
            } else {
                // Use the new notification
                newNotification
            }
        }.distinctBy { "${it.title}-${it.petId}-${it.type}" }

        // Filter out expired notifications
        val validNotifications = mergedNotifications.filter { notification ->
            notification.expiresAt == null || notification.expiresAt > currentTime
        }

        // Sort by priority and timestamp
        val sortedNotifications = validNotifications.sortedWith(
            compareByDescending<NotificationModel> { it.priority.level }
                .thenByDescending { it.timestamp }
        )

        _realTimeNotifications.value = sortedNotifications
        _criticalAlerts.value = sortedNotifications.filter {
            it.priority == NotificationPriority.CRITICAL && !it.isRead
        }

        android.util.Log.d("RealTimeNotificationMonitor", "📱 Updated notifications: ${sortedNotifications.size} total, ${_criticalAlerts.value.size} critical")
    }

    /**
     * Generate comprehensive real-time notifications for a single pet
     */
    private suspend fun generatePetRealTimeNotifications(pet: PetModel): List<NotificationModel> {
        val notifications = mutableListOf<NotificationModel>()

        // 1. Critical Health Alerts (Real-time)
        notifications.addAll(generateCriticalHealthAlerts(pet))

        // 2. Stat-based notifications (Real-time)
        notifications.addAll(generateStatBasedNotifications(pet))

        // 3. Time-based notifications (Real-time)
        notifications.addAll(generateTimeBasedNotifications(pet))

        // 4. Health system notifications - FIXED: Only if pet age qualifies for vaccines
        try {
            val requiredVaccines = petHealthService.getRequiredVaccines(pet)
            val overdueVaccines = petHealthService.getOverdueVaccines(pet)

            android.util.Log.d("RealTimeNotificationMonitor", "📋 Pet ${pet.name} (age ${pet.age}): Required vaccines: ${requiredVaccines.size}, Overdue: ${overdueVaccines.size}")

            if (requiredVaccines.isNotEmpty() || overdueVaccines.isNotEmpty()) {
                notifications.addAll(generateHealthSystemNotifications(pet))
            }
        } catch (e: Exception) {
            android.util.Log.e("RealTimeNotificationMonitor", "Error checking vaccines for ${pet.name}: ${e.message}")
        }

        // 5. Aging notifications
        notifications.addAll(generateAgingNotifications(pet))

        return notifications
    }

    /**
     * Generate critical health alerts that require immediate attention
     */
    private suspend fun generateCriticalHealthAlerts(pet: PetModel): List<NotificationModel> {
        val alerts = mutableListOf<NotificationModel>()

        // Critical health levels
        if (pet.health <= 15) {
            alerts.add(NotificationModel(
                title = "🚨 EMERGENCY: ${pet.name} is Dying!",
                message = "Health is critically low (${pet.health}%). Immediate veterinary care required!",
                type = NotificationType.HEALTH_CRITICAL,
                priority = NotificationPriority.CRITICAL,
                petId = pet.id,
                petName = pet.name,
                actionType = "emergency_health_check"
            ))
        }

        // Starvation alert
        if (pet.hunger >= 90) {
            alerts.add(NotificationModel(
                title = "🚨 STARVATION ALERT: ${pet.name}",
                message = "Your pet is starving! They need food immediately to survive!",
                type = NotificationType.HUNGER,
                priority = NotificationPriority.CRITICAL,
                petId = pet.id,
                petName = pet.name,
                actionType = "emergency_feed"
            ))
        }

        // Severe dehydration
        if (pet.energy <= 10) {
            alerts.add(NotificationModel(
                title = "🚨 COLLAPSE WARNING: ${pet.name}",
                message = "Your pet is about to collapse from exhaustion! They need rest now!",
                type = NotificationType.ENERGY,
                priority = NotificationPriority.CRITICAL,
                petId = pet.id,
                petName = pet.name,
                actionType = "emergency_rest"
            ))
        }

        // Disease progression
        val activeDiseases = getActiveDiseases(pet)
        activeDiseases.forEach { disease ->
            val daysSince = disease["daysSinceDiagnosis"] as? Int ?: 0
            val severity = disease["severity"]?.toString() ?: "MILD"

            if (severity == "SEVERE" && daysSince >= 7) {
                alerts.add(NotificationModel(
                    title = "🚨 URGENT: Disease Worsening!",
                    message = "${pet.name}'s ${disease["diseaseName"]} is severe and untreated for $daysSince days!",
                    type = NotificationType.DISEASE,
                    priority = NotificationPriority.CRITICAL,
                    petId = pet.id,
                    petName = pet.name,
                    actionType = "treat_disease"
                ))
            }
        }

        return alerts
    }

    /**
     * Generate stat-based notifications based on real-time pet stats
     */
    private fun generateStatBasedNotifications(pet: PetModel): List<NotificationModel> {
        val notifications = mutableListOf<NotificationModel>()
        val currentTime = System.currentTimeMillis()

        // Dynamic hunger notifications
        when {
            pet.hunger >= 80 -> notifications.add(NotificationModel(
                title = "🍽️ ${pet.name} is Very Hungry",
                message = "Hunger level: ${pet.hunger}%. Time to feed your pet!",
                type = NotificationType.HUNGER,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "feed"
            ))
            pet.hunger >= 60 -> notifications.add(NotificationModel(
                title = "🥪 ${pet.name} is Getting Hungry",
                message = "Hunger level: ${pet.hunger}%. Consider feeding soon.",
                type = NotificationType.HUNGER,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "feed"
            ))
        }

        // Dynamic energy notifications
        when {
            pet.energy <= 20 -> notifications.add(NotificationModel(
                title = "😴 ${pet.name} is Exhausted",
                message = "Energy level: ${pet.energy}%. Your pet needs rest!",
                type = NotificationType.ENERGY,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "sleep"
            ))
            pet.energy <= 40 -> notifications.add(NotificationModel(
                title = "💤 ${pet.name} is Tired",
                message = "Energy level: ${pet.energy}%. Time for a nap.",
                type = NotificationType.ENERGY,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "sleep"
            ))
        }

        // Dynamic happiness notifications
        when {
            pet.happiness <= 20 -> notifications.add(NotificationModel(
                title = "😢 ${pet.name} is Very Sad",
                message = "Happiness: ${pet.happiness}%. Your pet needs attention and play time!",
                type = NotificationType.HAPPINESS,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "play"
            ))
            pet.happiness <= 40 -> notifications.add(NotificationModel(
                title = "🙁 ${pet.name} is Feeling Down",
                message = "Happiness: ${pet.happiness}%. Some playtime would help!",
                type = NotificationType.HAPPINESS,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "play"
            ))
        }

        // Dynamic cleanliness notifications
        when {
            pet.cleanliness <= 20 -> notifications.add(NotificationModel(
                title = "🛁 ${pet.name} is Very Dirty",
                message = "Cleanliness: ${pet.cleanliness}%. Bath time is overdue!",
                type = NotificationType.CLEANLINESS,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "clean"
            ))
            pet.cleanliness <= 40 -> notifications.add(NotificationModel(
                title = "🧼 ${pet.name} Needs Cleaning",
                message = "Cleanliness: ${pet.cleanliness}%. Consider a bath soon.",
                type = NotificationType.CLEANLINESS,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "clean"
            ))
        }

        return notifications
    }

    /**
     * Generate time-based notifications
     */
    private fun generateTimeBasedNotifications(pet: PetModel): List<NotificationModel> {
        val notifications = mutableListOf<NotificationModel>()
        val currentTime = System.currentTimeMillis()
        val hoursInMs = 60 * 60 * 1000L

        // Time since last fed
        if (pet.lastFed > 0) {
            val hoursSinceFed = (currentTime - pet.lastFed) / hoursInMs
            when {
                hoursSinceFed >= 12 -> notifications.add(NotificationModel(
                    title = "⏰ Feeding Overdue!",
                    message = "${pet.name} hasn't been fed in ${hoursSinceFed} hours!",
                    type = NotificationType.REMINDER,
                    priority = NotificationPriority.HIGH,
                    petId = pet.id,
                    petName = pet.name,
                    actionType = "feed"
                ))
                hoursSinceFed >= 6 -> notifications.add(NotificationModel(
                    title = "🕕 Feeding Reminder",
                    message = "${pet.name} last ate ${hoursSinceFed} hours ago.",
                    type = NotificationType.REMINDER,
                    priority = NotificationPriority.MEDIUM,
                    petId = pet.id,
                    petName = pet.name,
                    actionType = "feed"
                ))
            }
        }

        // Time since last played
        if (pet.lastPlayed > 0) {
            val hoursSincePlayed = (currentTime - pet.lastPlayed) / hoursInMs
            if (hoursSincePlayed >= 8) {
                notifications.add(NotificationModel(
                    title = "🎾 Play Time Reminder",
                    message = "${pet.name} hasn't played in ${hoursSincePlayed} hours. They might be lonely!",
                    type = NotificationType.REMINDER,
                    priority = NotificationPriority.MEDIUM,
                    petId = pet.id,
                    petName = pet.name,
                    actionType = "play"
                ))
            }
        }

        // Time since last cleaned
        if (pet.lastCleaned > 0) {
            val daysSinceCleaned = (currentTime - pet.lastCleaned) / (24 * hoursInMs)
            if (daysSinceCleaned >= 3) {
                notifications.add(NotificationModel(
                    title = "🧽 Cleaning Reminder",
                    message = "${pet.name} hasn't been cleaned in ${daysSinceCleaned} days.",
                    type = NotificationType.REMINDER,
                    priority = NotificationPriority.MEDIUM,
                    petId = pet.id,
                    petName = pet.name,
                    actionType = "clean"
                ))
            }
        }

        return notifications
    }

    /**
     * Generate health system notifications (vaccines, diseases)
     */
    private suspend fun generateHealthSystemNotifications(pet: PetModel): List<NotificationModel> {
        val notifications = mutableListOf<NotificationModel>()

        try {
            // Get required vaccines
            val requiredVaccines = petHealthService.getRequiredVaccines(pet)
            requiredVaccines.forEach { vaccine ->
                notifications.add(NotificationModel(
                    title = "💉 Vaccination Due: ${vaccine.name}",
                    message = "${pet.name} needs ${vaccine.name}. Cost: $${vaccine.cost}",
                    type = NotificationType.VACCINATION,
                    priority = NotificationPriority.HIGH,
                    petId = pet.id,
                    petName = pet.name,
                    actionType = "vaccinate"
                ))
            }

            // Get overdue vaccines
            val overdueVaccines = petHealthService.getOverdueVaccines(pet)
            overdueVaccines.forEach { vaccine ->
                notifications.add(NotificationModel(
                    title = "🚨 URGENT: Overdue Vaccine!",
                    message = "${pet.name} is overdue for ${vaccine.name}! Risk of infection!",
                    type = NotificationType.VACCINATION,
                    priority = NotificationPriority.CRITICAL,
                    petId = pet.id,
                    petName = pet.name,
                    actionType = "vaccinate"
                ))
            }

        } catch (e: Exception) {
            android.util.Log.e("RealTimeNotificationMonitor", "Error generating health notifications: ${e.message}")
        }

        return notifications
    }

    /**
     * Generate aging-related notifications
     */
    private fun generateAgingNotifications(pet: PetModel): List<NotificationModel> {
        val notifications = mutableListOf<NotificationModel>()

        // Age milestone notifications
        when (pet.age) {
            6 -> notifications.add(NotificationModel(
                title = "🎉 6 Weeks Old!",
                message = "${pet.name} is 6 weeks old! Time for first vaccinations.",
                type = NotificationType.AGING,
                priority = NotificationPriority.HIGH,
                petId = pet.id,
                petName = pet.name,
                actionType = "vaccinate"
            ))
            12 -> notifications.add(NotificationModel(
                title = "🎂 3 Months Old!",
                message = "${pet.name} is growing up! Time for socialization and training.",
                type = NotificationType.AGING,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "play"
            ))
            52 -> notifications.add(NotificationModel(
                title = "🎊 Happy First Birthday!",
                message = "${pet.name} is 1 year old! Consider annual health check.",
                type = NotificationType.AGING,
                priority = NotificationPriority.MEDIUM,
                petId = pet.id,
                petName = pet.name,
                actionType = "health_check"
            ))
            84 -> notifications.add(NotificationModel(
                title = "👴 Senior Pet Care",
                message = "${pet.name} is now 7 years old! Senior pets need special care.",
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
     * Send a test notification to verify the notification system is working
     */
    fun sendTestNotification(context: Context) {
        android.util.Log.d("RealTimeNotificationMonitor", "🧪 Sending test notification...")

        // Check for POST_NOTIFICATIONS permission first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                android.util.Log.e("RealTimeNotificationMonitor", "❌ POST_NOTIFICATIONS permission not granted!")
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            12345, // Unique ID for test notification
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val testNotification = NotificationCompat.Builder(context, CHANNEL_ID_CRITICAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🧪 TailsTale Test Notification")
            .setContentText("If you can see this, notifications are working! 🎉")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle().bigText("This is a test notification to verify that the TailsTale notification system is working properly on your device."))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        try {
            @Suppress("MissingPermission")
            notificationManager.notify(12345, testNotification)
            android.util.Log.d("RealTimeNotificationMonitor", "✅ Test notification sent successfully!")
        } catch (e: Exception) {
            android.util.Log.e("RealTimeNotificationMonitor", "❌ Failed to send test notification: ${e.message}")
        }
    }

    /**
     * Send a test pet notification to verify pet notifications work
     */
    fun sendTestPetNotification(context: Context) {
        android.util.Log.d("RealTimeNotificationMonitor", "🐾 Sending test pet notification...")

        val testPetNotification = NotificationModel(
            title = "🚨 TEST: Your Pet Needs Help!",
            message = "This is a test critical pet notification. Your pet's health is critically low!",
            type = NotificationType.HEALTH_CRITICAL,
            priority = NotificationPriority.CRITICAL,
            petId = "test_pet_id",
            petName = "Test Pet",
            actionType = "health_check"
        )

        sendSystemNotification(context, testPetNotification)
        android.util.Log.d("RealTimeNotificationMonitor", "✅ Test pet notification sent!")
    }

    /**
     * Send system notification to device
     */
    private fun sendSystemNotification(context: Context, notification: NotificationModel) {
        // Check for POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                android.util.Log.w("RealTimeNotificationMonitor",
                    "POST_NOTIFICATIONS permission not granted. Cannot show notification: ${notification.title}")
                return
            }
        }

        val channelId = when (notification.priority) {
            NotificationPriority.CRITICAL -> CHANNEL_ID_CRITICAL
            NotificationPriority.HIGH -> CHANNEL_ID_HEALTH
            NotificationPriority.MEDIUM -> CHANNEL_ID_CARE
            NotificationPriority.LOW -> CHANNEL_ID_INFO
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_id", notification.id)
            putExtra("pet_id", notification.petId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val systemNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Use the proper notification icon
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(when (notification.priority) {
                NotificationPriority.CRITICAL -> NotificationCompat.PRIORITY_MAX
                NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                NotificationPriority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
                NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            })
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Safe to call notify() here because we've already checked the permission above
        @Suppress("MissingPermission")
        notificationManager.notify(notification.hashCode(), systemNotification)
    }

    /**
     * Get active diseases for a pet
     */
    private fun getActiveDiseases(pet: PetModel): List<Map<String, Any>> {
        val activeDiseases = mutableListOf<Map<String, Any>>()

        pet.diseaseHistory.forEach { (_, diseaseData) ->
            val diseaseMap = diseaseData as? Map<String, Any>
            if (diseaseMap != null) {
                val diagnosedDate = diseaseMap["diagnosedDate"] as? Long ?: 0L
                val daysSince = (System.currentTimeMillis() - diagnosedDate) / (1000 * 60 * 60 * 24)
                val status = diseaseMap["status"]?.toString()

                if (daysSince <= 30 && status != "TREATED") {
                    activeDiseases.add(diseaseMap + mapOf("daysSinceDiagnosis" to daysSince.toInt()))
                }
            }
        }

        return activeDiseases
    }

    /**
     * Get SharedPreferences instance for storing read notification status
     */
    private fun getReadStatusPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Generate unique key for notification to track read status
     */
    private fun generateNotificationKey(notification: NotificationModel): String {
        return "${notification.title}_${notification.petId}_${notification.type}_${notification.actionType}"
    }

    /**
     * Check if notification has been marked as read (persisted)
     */
    private fun isNotificationRead(context: Context, notification: NotificationModel): Boolean {
        val prefs = getReadStatusPrefs(context)
        val readNotifications = prefs.getStringSet(READ_NOTIFICATIONS_KEY, emptySet()) ?: emptySet()
        val notificationKey = generateNotificationKey(notification)
        return readNotifications.contains(notificationKey)
    }

    /**
     * Mark notification as read persistently
     */
    private fun markNotificationAsReadPersistent(context: Context, notification: NotificationModel) {
        val prefs = getReadStatusPrefs(context)
        val readNotifications = prefs.getStringSet(READ_NOTIFICATIONS_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        val notificationKey = generateNotificationKey(notification)
        readNotifications.add(notificationKey)

        prefs.edit()
            .putStringSet(READ_NOTIFICATIONS_KEY, readNotifications)
            .apply()
    }

    /**
     * Clear all read notification status (for testing or reset purposes)
     */
    private fun clearAllReadStatusPersistent(context: Context) {
        val prefs = getReadStatusPrefs(context)
        prefs.edit()
            .remove(READ_NOTIFICATIONS_KEY)
            .apply()
    }

    /**
     * Force a pet into critical condition for testing notifications
     */
    fun sendCriticalPetAlert(context: Context, petName: String = "Your Pet") {
        android.util.Log.d("RealTimeNotificationMonitor", "🚨 Forcing critical pet alert for testing...")

        val criticalAlert = NotificationModel(
            title = "🚨 EMERGENCY: $petName is Dying!",
            message = "Health is critically low (5%). Immediate veterinary care required!",
            type = NotificationType.HEALTH_CRITICAL,
            priority = NotificationPriority.CRITICAL,
            petId = "force_test_id",
            petName = petName,
            actionType = "emergency_health_check"
        )

        sendSystemNotification(context, criticalAlert)
        android.util.Log.d("RealTimeNotificationMonitor", "✅ Critical pet alert sent!")
    }
}
