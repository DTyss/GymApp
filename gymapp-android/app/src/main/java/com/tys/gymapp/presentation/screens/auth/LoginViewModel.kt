package com.tys.gymapp.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.repository.AuthRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.tys.gymapp.data.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * LoginViewModel - Quản lý logic đăng nhập
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    /**
     * Đăng nhập bằng email/phone
     */
    fun login(emailOrPhone: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            // Validate input
            if (emailOrPhone.isBlank()) {
                _loginState.value = LoginState.Error("Vui lòng nhập email hoặc số điện thoại")
                return@launch
            }

            if (password.isBlank()) {
                _loginState.value = LoginState.Error("Vui lòng nhập mật khẩu")
                return@launch
            }

            // Xác định là email hay phone
            val isEmail = emailOrPhone.contains("@")
            val email = if (isEmail) emailOrPhone else null
            val phone = if (!isEmail) emailOrPhone else null

            // Call API
            when (val result = authRepository.login(email, phone, password)) {
                is Resource.Success -> {
                    _loginState.value = LoginState.Success
                    registerFcmToken()
                }
                is Resource.Error -> {
                    _loginState.value = LoginState.Error(result.message ?: "Đăng nhập thất bại")
                }
                else -> {}
            }
        }
    }

    private fun registerFcmToken() {
        viewModelScope.launch {
            try {
                // Lấy FCM token từ Firebase
                val token = FirebaseMessaging.getInstance().token.await()

                // Gọi API đăng ký token
                notificationRepository.registerDevice(token)

                // Không cần xử lý response, silent fail OK
            } catch (e: Exception) {
                // Log error nhưng không block user flow
                e.printStackTrace()
            }
        }
    }

    /**
     * Reset state
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

/**
 * Login State
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}