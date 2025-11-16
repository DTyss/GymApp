package com.tys.gymapp.domain.util

/**
 * Resource - Sealed class để wrap kết quả API call
 * Giúp xử lý 3 trạng thái: Loading, Success, Error
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Loading<T> : Resource<T>()

    class Success<T>(data: T) : Resource<T>(data)

    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}

/**
 * Extension function để xử lý API response
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> retrofit2.Response<T>
): Resource<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else {
                Resource.Error("Response body is null")
            }
        } else {
            // Parse error body nếu có
            val errorMessage = try {
                response.errorBody()?.string() ?: "Unknown error"
            } catch (e: Exception) {
                "Error: ${response.code()}"
            }
            Resource.Error(errorMessage)
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Network error occurred")
    }
}