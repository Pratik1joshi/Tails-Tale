package com.example.tailstale.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth

class PetNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString("userId") ?: return Result.failure()

            // Get the real-time notification monitor
            val monitor = RealTimeNotificationMonitor.getInstance()

            // Generate and send notifications
            monitor.generateRealTimeNotifications(applicationContext, userId)

            android.util.Log.d("PetNotificationWorker", "Background notification check completed for user: $userId")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("PetNotificationWorker", "Background notification check failed: ${e.message}")
            Result.failure()
        }
    }
}
