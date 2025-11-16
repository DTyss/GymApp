package com.tys.gymapp.data.remote.api

import com.tys.gymapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service Interface
 * Định nghĩa tất cả endpoints gọi đến backend
 */
interface GymApiService {

    // ==================== AUTH ====================

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserLite>

    @GET("auth/me")
    suspend fun getMe(): Response<UserMe>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Map<String, Boolean>>

    @PUT("users/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserMe>

    // ==================== PLANS ====================

    @GET("plans")
    suspend fun getPlans(@Query("active") active: Boolean? = true): Response<List<Plan>>

    @GET("plans/{id}")
    suspend fun getPlanById(@Path("id") id: String): Response<Plan>

    @POST("plans")
    suspend fun createPlan(@Body request: CreatePlanRequest): Response<Plan>

    @PUT("plans/{id}")
    suspend fun updatePlan(@Path("id") id: String, @Body request: UpdatePlanRequest): Response<Plan>

    @DELETE("plans/{id}")
    suspend fun deletePlan(@Path("id") id: String): Response<Map<String, Any>>

    // ==================== BRANCHES ====================

    @GET("branches")
    suspend fun getBranches(@Query("active") active: Boolean? = true): Response<List<Branch>>

    @GET("branches/{id}")
    suspend fun getBranchById(@Path("id") id: String): Response<Branch>

    @POST("branches")
    suspend fun createBranch(@Body request: CreateBranchRequest): Response<Branch>

    @PUT("branches/{id}")
    suspend fun updateBranch(@Path("id") id: String, @Body request: UpdateBranchRequest): Response<Branch>

    @DELETE("branches/{id}")
    suspend fun deleteBranch(@Path("id") id: String): Response<Map<String, Any>>

    // ==================== CLASSES ====================

    @GET("classes")
    suspend fun getClasses(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("branchId") branchId: String? = null,
        @Query("trainerId") trainerId: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sortBy") sortBy: String = "startTime",
        @Query("sortDir") sortDir: String = "asc"
    ): Response<PaginatedClasses>

    @GET("classes/{id}")
    suspend fun getClassById(@Path("id") id: String): Response<ClassItem>

    // ==================== BOOKINGS ====================

    @POST("bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<Booking>

    @GET("bookings/my")
    suspend fun getMyBookings(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<PaginatedBookings>

    @DELETE("bookings/{id}")
    suspend fun cancelBooking(@Path("id") id: String): Response<Booking>

    // ==================== CHECKINS ====================

    @GET("checkins/qr/generate")
    suspend fun generateQr(): Response<QrPayload>

    @POST("checkins/qr")
    suspend fun verifyQrCheckin(@Body request: CheckinRequest): Response<CheckinResponse>

    @GET("checkins/my")
    suspend fun getMyCheckins(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<PaginatedCheckins>

    // ==================== NOTIFICATIONS ====================

    @GET("notifications/my")
    suspend fun getMyNotifications(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("isRead") isRead: Boolean? = null,
        @Query("sortDir") sortDir: String = "desc"
    ): Response<PaginatedNotifications>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): Response<Notification>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<Map<String, Any>>

    // ==================== DEVICES ====================

    @POST("devices")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<RegisterDeviceResponse>

    // ==================== MEMBERSHIPS ====================

    @GET("memberships/my/list")
    suspend fun getMyMemberships(): Response<List<Membership>>

    // ==================== MEMBERSHIPS MANAGEMENT ====================

    @GET("memberships")
    suspend fun getMemberships(
        @Query("userId") userId: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<PaginatedMemberships>

    @GET("memberships/{id}")
    suspend fun getMembershipById(@Path("id") id: String): Response<MembershipDetail>

    @POST("memberships")
    suspend fun createMembership(@Body request: CreateMembershipRequest): Response<MembershipDetail>

    @PUT("memberships/{id}/extend")
    suspend fun extendMembership(
        @Path("id") id: String,
        @Body request: ExtendMembershipRequest
    ): Response<MembershipDetail>

    @PUT("memberships/{id}/pause")
    suspend fun pauseMembership(@Path("id") id: String): Response<MembershipDetail>

    @PUT("memberships/{id}/resume")
    suspend fun resumeMembership(@Path("id") id: String): Response<MembershipDetail>

    // ==================== STATS ====================

    @GET("stats/dashboard")
    suspend fun getDashboard(): Response<DashboardStats>

    @GET("stats/members")
    suspend fun getMemberStats(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<MemberStats>

    @GET("stats/checkins")
    suspend fun getCheckinStats(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("branchId") branchId: String? = null
    ): Response<CheckinStats>

    @GET("stats/revenue")
    suspend fun getRevenueStats(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<RevenueStats>

    // ==================== CLASS MANAGEMENT ====================

    @POST("classes")
    suspend fun createClass(@Body request: CreateClassRequest): Response<ClassItem>

    @PUT("classes/{id}")
    suspend fun updateClass(
        @Path("id") id: String,
        @Body request: UpdateClassRequest
    ): Response<ClassItem>

    @DELETE("classes/{id}")
    suspend fun deleteClass(@Path("id") id: String): Response<Map<String, Any>>



}