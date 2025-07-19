package com.example.tailstale.utils

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(private val context: Context) : DefaultLifecycleObserver {

    private val notificationScheduler = NotificationScheduler(context)

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // App went to background, schedule inactivity notification
        notificationScheduler.scheduleInactivityReminder()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // App came to foreground, cancel inactivity notification
        notificationScheduler.cancelInactivityReminder()
    }
}