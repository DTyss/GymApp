package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.PaginatedCheckins
import com.tys.gymapp.data.remote.dto.QrPayload
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.domain.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CheckinRepository - Quản lý check-in
 */
@Singleton
class CheckinRepository @Inject constructor(
    private val api: GymApiService
) {

    /**
     * Generate QR code
     */
    suspend fun generateQr(): Resource<QrPayload> {
        return safeApiCall {
            api.generateQr()
        }
    }

    /**
     * Lấy lịch sử check-in
     */
    suspend fun getMyCheckins(
        page: Int = 1,
        pageSize: Int = 20,
        from: String? = null,
        to: String? = null
    ): Resource<PaginatedCheckins> {
        return safeApiCall {
            api.getMyCheckins(page, pageSize, from, to)
        }
    }
}