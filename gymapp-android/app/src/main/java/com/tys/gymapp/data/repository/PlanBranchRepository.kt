package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.*
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.domain.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PlanBranchRepository - Quản lý plans, branches, memberships
 */
@Singleton
class PlanBranchRepository @Inject constructor(
    private val api: GymApiService
) {

    // ==================== PLANS ====================

    /**
     * Lấy danh sách plans
     */
    suspend fun getPlans(active: Boolean? = true): Resource<List<Plan>> {
        return safeApiCall {
            api.getPlans(active)
        }
    }

    /**
     * Lấy chi tiết plan
     */
    suspend fun getPlanById(id: String): Resource<Plan> {
        return safeApiCall {
            api.getPlanById(id)
        }
    }

    /**
     * Tạo plan mới
     */
    suspend fun createPlan(request: CreatePlanRequest): Resource<Plan> {
        return safeApiCall {
            api.createPlan(request)
        }
    }

    /**
     * Cập nhật plan
     */
    suspend fun updatePlan(id: String, request: UpdatePlanRequest): Resource<Plan> {
        return safeApiCall {
            api.updatePlan(id, request)
        }
    }

    /**
     * Xóa plan
     */
    suspend fun deletePlan(id: String): Resource<Map<String, Any>> {
        return safeApiCall {
            api.deletePlan(id)
        }
    }

    // ==================== BRANCHES ====================

    /**
     * Lấy danh sách branches
     */
    suspend fun getBranches(active: Boolean? = true): Resource<List<Branch>> {
        return safeApiCall {
            api.getBranches(active)
        }
    }

    /**
     * Lấy chi tiết branch
     */
    suspend fun getBranchById(id: String): Resource<Branch> {
        return safeApiCall {
            api.getBranchById(id)
        }
    }

    /**
     * Tạo branch mới
     */
    suspend fun createBranch(request: CreateBranchRequest): Resource<Branch> {
        return safeApiCall {
            api.createBranch(request)
        }
    }

    /**
     * Cập nhật branch
     */
    suspend fun updateBranch(id: String, request: UpdateBranchRequest): Resource<Branch> {
        return safeApiCall {
            api.updateBranch(id, request)
        }
    }

    /**
     * Xóa branch
     */
    suspend fun deleteBranch(id: String): Resource<Map<String, Any>> {
        return safeApiCall {
            api.deleteBranch(id)
        }
    }

    // ==================== MEMBERSHIPS ====================

    /**
     * Lấy memberships của user
     */
    suspend fun getMyMemberships(): Resource<List<Membership>> {
        return safeApiCall {
            api.getMyMemberships()
        }
    }
}