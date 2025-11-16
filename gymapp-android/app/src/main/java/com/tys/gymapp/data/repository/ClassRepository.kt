package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.ClassItem
import com.tys.gymapp.data.remote.dto.CreateClassRequest
import com.tys.gymapp.data.remote.dto.PaginatedClasses
import com.tys.gymapp.data.remote.dto.UpdateClassRequest
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.domain.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ClassRepository - Quản lý data lớp học
 */
@Singleton
class ClassRepository @Inject constructor(
    private val api: GymApiService
) {

    /**
     * Lấy danh sách lớp học
     */
    suspend fun getClasses(
        from: String? = null,
        to: String? = null,
        branchId: String? = null,
        trainerId: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
        sortBy: String = "startTime",
        sortDir: String = "asc"
    ): Resource<PaginatedClasses> {
        return safeApiCall {
            api.getClasses(from, to, branchId, trainerId, page, pageSize, sortBy, sortDir)
        }
    }

    /**
     * Lấy chi tiết 1 lớp học
     */
    suspend fun getClassById(id: String): Resource<ClassItem> {
        return safeApiCall {
            api.getClassById(id)
        }
    }

    /**
     * Create class
     */
    suspend fun createClass(request: CreateClassRequest): Resource<ClassItem> {
        return safeApiCall {
            api.createClass(request)
        }
    }

    /**
     * Update class
     */
    suspend fun updateClass(id: String, request: UpdateClassRequest): Resource<ClassItem> {
        return safeApiCall {
            api.updateClass(id, request)
        }
    }

    /**
     * Delete class
     */
    suspend fun deleteClass(id: String): Resource<Map<String, Any>> {
        return safeApiCall {
            api.deleteClass(id)
        }
    }
}