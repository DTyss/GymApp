package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.*
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.domain.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MembershipRepository - Quản lý memberships (Admin/Receptionist)
 */
@Singleton
class MembershipRepository @Inject constructor(
    private val api: GymApiService
) {

    /**
     * Lấy danh sách tất cả memberships (với filter)
     */
    suspend fun getMemberships(
        userId: String? = null,
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 50
    ): Resource<PaginatedMemberships> {
        return safeApiCall {
            api.getMemberships(userId, status, page, pageSize)
        }
    }

    /**
     * Lấy chi tiết 1 membership
     */
    suspend fun getMembershipById(id: String): Resource<MembershipDetail> {
        return safeApiCall {
            api.getMembershipById(id)
        }
    }

    /**
     * Tạo membership mới cho user
     */
    suspend fun createMembership(request: CreateMembershipRequest): Resource<MembershipDetail> {
        return safeApiCall {
            api.createMembership(request)
        }
    }

    /**
     * Gia hạn membership
     */
    suspend fun extendMembership(
        id: String,
        request: ExtendMembershipRequest
    ): Resource<MembershipDetail> {
        return safeApiCall {
            api.extendMembership(id, request)
        }
    }

    /**
     * Tạm dừng membership
     */
    suspend fun pauseMembership(id: String): Resource<MembershipDetail> {
        return safeApiCall {
            api.pauseMembership(id)
        }
    }

    /**
     * Kích hoạt lại membership
     */
    suspend fun resumeMembership(id: String): Resource<MembershipDetail> {
        return safeApiCall {
            api.resumeMembership(id)
        }
    }
}