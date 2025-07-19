package com.example.tailstale.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tailstale.MainActivity
import com.example.tailstale.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "PET_CARE_CHANNEL"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "NotificationHelper"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pet Care Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications to remind you to take care of your virtual pet"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below, permission is granted by default
            true
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = NotificationManagerCompat.from(context)
        return notificationManager.areNotificationsEnabled()
    }

    fun sendPetNeedsNotification(health: Int, hunger: Int, happiness: Int) {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Notification permission not granted")
            return
        }

        if (!areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled in system settings")
            return
        }

        val message = when {
            hunger < 30 -> "Come on Master, your pet is hungry! 🍖"
            health < 40 -> "Your pet needs medical attention! 🏥"
            happiness < 30 -> "Your pet is sad and needs some love! 💔"
            health < 60 -> "Your pet needs you! Time for some care! 🐾"
            else -> "Don't forget about your virtual pet! 🐕"
        }

        val title = when {
            hunger < 30 -> "Buddy is Hungry!"
            health < 40 -> "Health Alert!"
            happiness < 30 -> "Buddy is Sad!"
            else -> "Pet Care Reminder"
        }

        sendNotification(title, message)
    }

    fun sendCustomNotification(title: String, message: String) {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Notification permission not granted for custom notification")
            return
        }

        if (!areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled in system settings")
            return
        }

        sendNotification(title, message)
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // You can change this to a custom pet icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification sent successfully: $title")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when sending notification", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
        }
    }
}