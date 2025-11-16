package com.tys.gymapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tys.gymapp.MainActivity
import com.tys.gymapp.R
import com.tys.gymapp.data.local.TokenManager
import com.tys.gymapp.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Messaging Service
 * Xử lý FCM token và nhận push notifications
 */
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var tokenManager: TokenManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Được gọi khi FCM token mới được generate
     * - Lần đầu cài app
     * - App reinstall
     * - User clear data
     * - Token expire
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Register token với backend nếu user đã login
        serviceScope.launch {
            val isLoggedIn = tokenManager.isLoggedIn()
            if (isLoggedIn) {
                notificationRepository.registerDevice(token)
            }
        }
    }

    /**
     * Được gọi khi nhận push notification
     * - App ở foreground: Show notification manually
     * - App ở background: FCM tự hiển thị
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Extract notification data
        val title = message.notification?.title ?: message.data["title"] ?: "GymApp"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        // Hiển thị notification
        showNotification(title, body)
    }

    /**
     * Hiển thị notification trên device
     */
    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getString(R.string.default_notification_channel_id)

        // Create notification channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Gym Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo từ phòng Gym"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent khi user click notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Có thể thêm extras để navigate đến NotificationsScreen
            putExtra("navigate_to", "notifications")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Show notification với unique ID
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}