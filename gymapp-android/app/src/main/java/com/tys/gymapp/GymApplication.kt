package com.tys.gymapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import com.tys.gymapp.data.repository.NotificationRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class - Entry point của app
 * @HiltAndroidApp: Khởi tạo Hilt dependency injection
 */
@HiltAndroidApp
class GymApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        @Inject
        lateinit var notificationRepository: NotificationRepository

        // Tạo notification channel cho Android 8.0+
        createNotificationChannel()
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                viewModelScope.launch {
                    notificationRepository.registerDevice(token)
                }
            }
        }
    }

    /**
     * Tạo notification channel cho FCM
     * Bắt buộc từ Android 8.0 (API 26) trở lên
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = "Gym Notifications"
            val channelDescription = "Thông báo từ phòng Gym"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}