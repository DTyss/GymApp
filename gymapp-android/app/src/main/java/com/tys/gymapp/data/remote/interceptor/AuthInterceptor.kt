package com.tys.gymapp.data.remote.interceptor

import com.tys.gymapp.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * AuthInterceptor - OkHttp Interceptor
 * Tự động thêm JWT token vào header của mọi request
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Lấy token (blocking vì Interceptor không support suspend)
        val token = runBlocking {
            tokenManager.getTokenSync()
        }

        // Nếu có token, thêm vào header
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}