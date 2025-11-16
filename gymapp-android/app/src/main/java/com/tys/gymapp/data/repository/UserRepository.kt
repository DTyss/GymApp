package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.*
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.domain.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserRepository - Quản lý users (Admin only)
 */
@Singleton
class UserRepository @Inject constructor(
    private val api: GymApiService
) {
    /**
     * Lấy danh sách users với filter
     */
    suspend fun getUsers(
        role: String? = null,
        status: String? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Resource<PaginatedUsers> {
        return safeApiCall {
            api.getUsers(role, status, search, page, pageSize)
        }
    }

    /**
     * Lấy chi tiết user
     */
    suspend fun getUserById(id: String): Resource<UserDetail> {
        return safeApiCall {
            api.getUserById(id)
        }
    }

    /**
     * Cập nhật thông tin user
     */
    suspend fun updateUser(id: String, request: UpdateUserRequest): Resource<User> {
        return safeApiCall {
            api.updateUser(id, request)
        }
    }

    /**
     * Cập nhật trạng thái user
     */
    suspend fun updateStatus(id: String, status: String): Resource<User> {
        return safeApiCall {
            api.updateUserStatus(id, UpdateStatusRequest(status))
        }
    }
}