package com.macro.automation

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class AutomationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Macro Automation Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "macro_automation_channel"
    }
}
