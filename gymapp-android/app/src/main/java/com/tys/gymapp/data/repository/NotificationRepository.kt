package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.Notification
import com.tys.gymapp.data.remote.dto.PaginatedNotifications
import com.tys.gymapp.data.remote.dto.RegisterDeviceRequest
import com.tys.gymapp.data.remote.dto.RegisterDeviceResponse
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.domain.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificationRepository - Quản lý notifications
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val api: GymApiService
) {

    /**
     * Lấy danh sách notifications
     */
    suspend fun getMyNotifications(
        page: Int = 1,
        pageSize: Int = 20,
        isRead: Boolean? = null,
        sortDir: String = "desc"
    ): Resource<PaginatedNotifications> {
        return safeApiCall {
            api.getMyNotifications(page, pageSize, isRead, sortDir)
        }
    }

    /**
     * Đánh dấu đã đọc
     */
    suspend fun markAsRead(id: String): Resource<Notification> {
        return safeApiCall {
            api.markNotificationAsRead(id)
        }
    }

    /**
     * Đánh dấu tất cả đã đọc
     */
    suspend fun markAllAsRead(): Resource<Map<String, Any>> {
        return safeApiCall {
            api.markAllNotificationsAsRead()
        }
    }

    /**
     * Đăng ký FCM token
     */
    suspend fun registerDevice(fcmToken: String): Resource<RegisterDeviceResponse> {
        return safeApiCall {
            api.registerDevice(RegisterDeviceRequest(fcmToken, "android"))
        }
    }
}