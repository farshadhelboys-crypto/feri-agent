package com.feri.agent

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class FeriApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Feri Agent Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "اعلان سرویس دستیار صوتی"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "feri_agent_channel"
    }
}
