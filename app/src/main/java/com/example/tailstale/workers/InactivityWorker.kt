package com.example.tailstale.workers


import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tailstale.utils.NotificationHelper

class InactivityWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)

        // Send inactivity notification
        notificationHelper.sendCustomNotification(
            "Your Pet Needs You! 🐾",
            "Buddy is waiting for you! Come back and take care of your virtual pet."
        )

        return Result.success()
    }
}