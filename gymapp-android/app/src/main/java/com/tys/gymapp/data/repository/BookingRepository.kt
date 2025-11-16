package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.Booking
import com.tys.gymapp.data.remote.dto.CreateBookingRequest
import com.tys.gymapp.data.remote.dto.PaginatedBookings
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.domain.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BookingRepository - Quản lý bookings
 */
@Singleton
class BookingRepository @Inject constructor(
    private val api: GymApiService
) {

    /**
     * Tạo booking mới
     */
    suspend fun createBooking(classId: String): Resource<Booking> {
        return safeApiCall {
            api.createBooking(CreateBookingRequest(classId))
        }
    }

    /**
     * Lấy danh sách bookings của user
     */
    suspend fun getMyBookings(
        page: Int = 1,
        pageSize: Int = 20,
        sortBy: String = "createdAt",
        sortDir: String = "desc"
    ): Resource<PaginatedBookings> {
        return safeApiCall {
            api.getMyBookings(page, pageSize, sortBy, sortDir)
        }
    }

    /**
     * Hủy booking
     */
    suspend fun cancelBooking(id: String): Resource<Booking> {
        return safeApiCall {
            api.cancelBooking(id)
        }
    }
}