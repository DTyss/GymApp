package com.tys.gymapp.data.repository

import com.tys.gymapp.data.local.TokenManager
import com.tys.gymapp.data.remote.api.GymApiService
import com.tys.gymapp.data.remote.dto.*
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.domain.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthRepository - Xử lý logic authentication
 * Single source of truth cho auth data
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: GymApiService,
    private val tokenManager: TokenManager
) {

    /**
     * Login
     */
    suspend fun login(email: String?, phone: String?, password: String): Resource<LoginResponse> {
        val result = safeApiCall {
            api.login(LoginRequest(email, phone, password))
        }

        // Nếu login thành công, lưu token
        if (result is Resource.Success) {
            result.data?.let { loginResponse ->
                tokenManager.saveToken(
                    token = loginResponse.token,
                    userId = loginResponse.user.id,
                    userName = loginResponse.user.fullName,
                    role = loginResponse.user.role
                )
            }
        }

        return result
    }

    /**
     * Register
     */
    suspend fun register(
        email: String?,
        phone: String?,
        fullName: String,
        password: String
    ): Resource<UserLite> {
        return safeApiCall {
            api.register(RegisterRequest(email, phone, fullName, password))
        }
    }

    /**
     * Get current user info
     */
    suspend fun getMe(): Resource<UserMe> {
        return safeApiCall {
            api.getMe()
        }
    }

    /**
     * Change password
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Resource<Map<String, Boolean>> {
        return safeApiCall {
            api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
        }
    }

    /**
     * Logout - Clear token
     */
    suspend fun logout() {
        tokenManager.clearToken()
    }

    /**
     * Check if user is logged in
     */
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * Get token as Flow
     */
    fun getToken(): Flow<String?> {
        return tokenManager.getToken()
    }

    /**
     * Get user name
     */
    fun getUserName(): Flow<String?> {
        return tokenManager.getUserName()
    }

    /**
     * Get user role
     */
    fun getUserRole(): Flow<String?> {
        return tokenManager.getUserRole()
    }

    /**
     * Update profile
     */
    suspend fun updateProfile(
        fullName: String? = null,
        email: String? = null,
        phone: String? = null
    ): Resource<UserMe> {
        return safeApiCall {
            api.updateProfile(UpdateProfileRequest(fullName, email, phone))
        }
    }
}