package com.tys.gymapp.data.remote.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ===========================================
 * AUTH & USER MODELS
 * ===========================================
 */

data class LoginRequest(
    val email: String? = null,
    val phone: String? = null,
    val password: String
)

data class RegisterRequest(
    val email: String? = null,
    val phone: String? = null,
    val fullName: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserLite
)

data class UserLite(
    val id: String,
    val fullName: String,
    val role: String
)

data class UserMe(
    val id: String,
    val fullName: String,
    val role: String,
    val status: String,
    val email: String?,
    val phone: String?,
    val memberships: List<MembershipLite>
)

data class MembershipLite(
    val id: String,
    val endDate: String,
    val remainingSessions: Int,
    val status: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

/**
 * ===========================================
 * PLAN MODELS
 * ===========================================
 */

data class Plan(
    val id: String,
    val name: String,
    val price: Double,
    val sessions: Int,
    val durationDays: Int,
    val isActive: Boolean,
    val createdAt: String
)

/**
 * ===========================================
 * BRANCH MODELS
 * ===========================================
 */

data class Branch(
    val id: String,
    val name: String,
    val address: String?,
    val isActive: Boolean
)

/**
 * ===========================================
 * CLASS MODELS
 * ===========================================
 */

@Parcelize
data class ClassItem(
    val id: String,
    val title: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val capacity: Int,
    val available: Int,
    val branch: BranchLite,
    val trainer: TrainerLite
) : Parcelable

@Parcelize
data class BranchLite(
    val id: String,
    val name: String
) : Parcelable

@Parcelize
data class TrainerLite(
    val id: String,
    val fullName: String
) : Parcelable

data class PaginatedClasses(
    val items: List<ClassItem>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

/**
 * ===========================================
 * BOOKING MODELS
 * ===========================================
 */

data class CreateBookingRequest(
    val classId: String
)

data class Booking(
    val id: String,
    val classId: String,
    val userId: String,
    val status: String,
    val createdAt: String,
    val `class`: ClassItem? = null
)

data class PaginatedBookings(
    val items: List<Booking>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

/**
 * ===========================================
 * CHECKIN MODELS
 * ===========================================
 */

data class QrPayload(
    val userId: String,
    val nonce: String,
    val exp: Long,
    val sig: String
)

data class CheckinRequest(
    val payload: QrPayload,
    val branchId: String? = null
)

data class CheckinResponse(
    val ok: Boolean,
    val membershipId: String,
    val remainingSessions: Int
)

data class Checkin(
    val id: String,
    val userId: String,
    val branchId: String,
    val method: String,
    val status: String,
    val checkedAt: String,
    val branch: Branch? = null
)

data class PaginatedCheckins(
    val items: List<Checkin>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

/**
 * ===========================================
 * NOTIFICATION MODELS
 * ===========================================
 */

data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val sentAt: String
)

data class PaginatedNotifications(
    val items: List<Notification>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

/**
 * ===========================================
 * DEVICE MODELS
 * ===========================================
 */

data class RegisterDeviceRequest(
    val fcmToken: String,
    val platform: String = "android"
)

data class RegisterDeviceResponse(
    val ok: Boolean,
    val deviceId: String
)

/**
 * ===========================================
 * MEMBERSHIP MODELS
 * ===========================================
 */

data class Membership(
    val id: String,
    val userId: String,
    val planId: String,
    val startDate: String,
    val endDate: String,
    val remainingSessions: Int,
    val status: String,
    val plan: Plan
)

/**
 * ===========================================
 * MEMBERSHIP MANAGEMENT MODELS
 * ===========================================
 */

// Paginated response
data class PaginatedMemberships(
    val items: List<MembershipDetail>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

// Membership detail (with user and plan info)
data class MembershipDetail(
    val id: String,
    val userId: String,
    val planId: String,
    val startDate: String,
    val endDate: String,
    val remainingSessions: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val user: UserLite,
    val plan: Plan
)

// Create membership request
data class CreateMembershipRequest(
    val userId: String,
    val planId: String,
    val startDate: String? = null  // ISO date, null = today
)

// Extend membership request
data class ExtendMembershipRequest(
    val additionalDays: Int? = null,
    val additionalSessions: Int? = null
)

/**
 * ===========================================
 * PROFILE UPDATE MODELS
 * ===========================================
 */

data class UpdateProfileRequest(
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null
)

/**
 * ===========================================
 * ERROR RESPONSE
 * ===========================================
 */

data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Any? = null
)

/**
 * ===========================================
 * STATS MODELS
 * ===========================================
 */

data class DashboardStats(
    val users: UserStats,
    val classes: ClassStats,
    val checkins: CheckinStatsCount,
    val bookings: BookingStats,
    val memberships: MembershipStats
)

data class UserStats(
    val total: Int,
    val members: Int,
    val activeMembers: Int
)

data class ClassStats(
    val total: Int,
    val today: Int
)

data class CheckinStatsCount(
    val total: Int,
    val today: Int,
    val thisMonth: Int
)

data class BookingStats(
    val total: Int
)

data class MembershipStats(
    val active: Int
)

// Member Stats Detail
data class MemberStats(
    val byRole: List<RoleCount>,
    val byStatus: List<StatusCount>,
    val newMembers: Int,
    val withActiveMembership: Int
)

data class RoleCount(
    val role: String,
    val _count: CountData
)

data class StatusCount(
    val status: String,
    val _count: CountData
)

data class CountData(
    val id: Int
)

// Checkin Stats Detail
data class CheckinStats(
    val total: Int,
    val byBranch: List<BranchCheckinCount>,
    val byMethod: List<MethodCheckinCount>,
    val byDay: List<DailyCheckinCount>
)

data class BranchCheckinCount(
    val branchId: String,
    val _count: CountData
)

data class MethodCheckinCount(
    val method: String,
    val _count: CountData
)

data class DailyCheckinCount(
    val date: String,
    val count: Int
)

// Revenue Stats
data class RevenueStats(
    val totalRevenue: Double,
    val totalMemberships: Int,
    val byPlan: List<PlanRevenue>
)

data class PlanRevenue(
    val plan: String,
    val count: Int,
    val revenue: Double
)

/**
 * ===========================================
 * CLASS MANAGEMENT MODELS
 * ===========================================
 */

data class CreateClassRequest(
    val title: String,
    val description: String?,
    val trainerId: String,
    val branchId: String,
    val startTime: String,  // ISO datetime
    val endTime: String,    // ISO datetime
    val capacity: Int
)

data class UpdateClassRequest(
    val title: String? = null,
    val description: String? = null,
    val trainerId: String? = null,
    val branchId: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val capacity: Int? = null
)