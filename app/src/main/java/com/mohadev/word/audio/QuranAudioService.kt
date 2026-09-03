package com.mohadev.word.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * خدمة تشغيل الصوت في الخلفية (Foreground Service) لضمان استمرار تلاوة
 * القرآن الكريم أو البث الإذاعي عند إطفاء الشاشة أو مغادرة التطبيق،
 * بما يتوافق مع متطلبات Android 14+ الخاصة بخدمات تشغيل الوسائط.
 */
class QuranAudioService : Service() {

    companion object {
        const val CHANNEL_ID = "quran_audio_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_TITLE = "AUDIO_TITLE"
        const val EXTRA_SUBTITLE = "AUDIO_SUBTITLE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "القرآن الكريم"
        val subtitle = intent?.getStringExtra(EXTRA_SUBTITLE) ?: "تلاوة مباركة"

        val notification = buildNotification(title, subtitle)
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private fun buildNotification(title: String, subtitle: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تشغيل القرآن الكريم",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار استمرار تلاوة القرآن الكريم في الخلفية"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
