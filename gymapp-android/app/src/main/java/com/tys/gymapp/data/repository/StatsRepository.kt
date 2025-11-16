package com.tys.gymapp.data.repository

import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.*
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.domain.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

/**
 * StatsRepository - Quản lý statistics/dashboard data
 */
@Singleton
class StatsRepository @Inject constructor(
    private val api: GymApiService
) {

    suspend fun getDashboard(): Resource<DashboardStats> {
        return safeApiCall {
            api.getDashboard()
        }
    }

    suspend fun getMemberStats(from: String? = null, to: String? = null): Resource<MemberStats> {
        return safeApiCall {
            api.getMemberStats(from, to)
        }
    }

    suspend fun getCheckinStats(
        from: String? = null,
        to: String? = null,
        branchId: String? = null
    ): Resource<CheckinStats> {
        return safeApiCall {
            api.getCheckinStats(from, to, branchId)
        }
    }

    suspend fun getRevenueStats(from: String? = null, to: String? = null): Resource<RevenueStats> {
        return safeApiCall {
            api.getRevenueStats(from, to)
        }
    }
}