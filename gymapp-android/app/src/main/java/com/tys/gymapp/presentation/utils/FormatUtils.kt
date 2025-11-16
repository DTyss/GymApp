package com.tys.gymapp.presentation.utils

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Format price to Vietnamese currency format
 */
fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(price)} VNĐ"
}

/**
 * Format date string to readable format
 */
fun formatDate(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Get role name in Vietnamese
 */
fun getRoleName(role: String): String {
    return when (role.lowercase()) {
        "admin" -> "Quản trị viên"
        "member" -> "Thành viên"
        "trainer" -> "Huấn luyện viên"
        else -> role
    }
}

/**
 * Get status name in Vietnamese
 */
fun getStatusName(status: String): String {
    return when (status.lowercase()) {
        "active" -> "Hoạt động"
        "inactive" -> "Không hoạt động"
        "suspended" -> "Tạm khóa"
        "pending" -> "Chờ duyệt"
        else -> status
    }
}

