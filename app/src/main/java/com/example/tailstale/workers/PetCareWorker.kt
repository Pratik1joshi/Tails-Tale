package com.example.tailstale.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tailstale.utils.NotificationHelper

class PetCareWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)

        // Get stored pet stats (you'll need to implement persistent storage)
        val health = inputData.getInt("health", 85)
        val hunger = inputData.getInt("hunger", 40)
        val happiness = inputData.getInt("happiness", 70)

        // Send notification based on pet's current state
        notificationHelper.sendPetNeedsNotification(health, hunger, happiness)

        return Result.success()
    }
}