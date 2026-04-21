package com.example.fraudguardian.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.fraudguardian.R

class ForegroundListenerService : Service() {
    companion object {
        const val CHANNEL_ID = "fraudguardian_listen_channel"
        const val NOTIF_ID = 233
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()

        val notif: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FraudGuardian active")
            .setContentText("Monitoring calls and SMS for suspicious activity")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(NOTIF_ID, notif)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(NotificationChannel(CHANNEL_ID, "FraudGuardian monitoring", NotificationManager.IMPORTANCE_LOW))
        }
    }
}
