package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.Branch
import com.tys.gymapp.data.remote.dto.Membership
import com.tys.gymapp.data.remote.dto.Plan
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
     * Lấy memberships của user
     */
    suspend fun getMyMemberships(): Resource<List<Membership>> {
        return safeApiCall {
            api.getMyMemberships()
        }
    }
}