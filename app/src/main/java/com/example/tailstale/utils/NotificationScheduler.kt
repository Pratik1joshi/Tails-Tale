package com.example.tailstale.utils

import android.content.Context
import androidx.work.*
import com.example.tailstale.workers.PetCareWorker
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    companion object {
        const val PET_CARE_WORK = "pet_care_notification_work"
    }

    fun schedulePetCareReminders(health: Int, hunger: Int, happiness: Int) {
        val inputData = workDataOf(
            "health" to health,
            "hunger" to hunger,
            "happiness" to happiness
        )

        val workRequest = PeriodicWorkRequestBuilder<PetCareWorker>(
            2, TimeUnit.HOURS // Send notification every 2 hours
        )
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PET_CARE_WORK,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelPetCareReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(PET_CARE_WORK)
    }

    fun scheduleOneTimeReminder(delayMinutes: Long, health: Int, hunger: Int, happiness: Int) {
        val inputData = workDataOf(
            "health" to health,
            "hunger" to hunger,
            "happiness" to happiness
        )

        val workRequest = OneTimeWorkRequestBuilder<PetCareWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
