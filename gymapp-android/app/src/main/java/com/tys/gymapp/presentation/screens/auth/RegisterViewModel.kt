package com.tys.gymapp.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.tys.gymapp.data.repository.AuthRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.tys.gymapp.data.repository.NotificationRepository
import kotlinx.coroutines.tasks.await

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState = _registerState.asStateFlow()

    fun register(
        emailOrPhone: String,
        fullName: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            // Validate
            if (fullName.isBlank()) {
                _registerState.value = RegisterState.Error("Vui lòng nhập họ tên")
                return@launch
            }

            if (emailOrPhone.isBlank()) {
                _registerState.value = RegisterState.Error("Vui lòng nhập email hoặc số điện thoại")
                return@launch
            }

            if (password.isBlank()) {
                _registerState.value = RegisterState.Error("Vui lòng nhập mật khẩu")
                return@launch
            }

            if (password.length < 6) {
                _registerState.value = RegisterState.Error("Mật khẩu phải có ít nhất 6 ký tự")
                return@launch
            }

            if (password != confirmPassword) {
                _registerState.value = RegisterState.Error("Mật khẩu không khớp")
                return@launch
            }

            // Xác định email hay phone
            val isEmail = emailOrPhone.contains("@")
            val email = if (isEmail) emailOrPhone else null
            val phone = if (!isEmail) emailOrPhone else null

            // Call API
            when (val result = authRepository.register(email, phone, fullName, password)) {
                is Resource.Success -> {
                    // Sau khi đăng ký thành công, tự động login
                    autoLogin(email, phone, password)
                }
                is Resource.Error -> {
                    _registerState.value = RegisterState.Error(result.message ?: "Đăng ký thất bại")
                }
                else -> {}
            }
        }
    }

    private suspend fun autoLogin(email: String?, phone: String?, password: String) {
        when (authRepository.login(email, phone, password)) {
            is Resource.Success -> {
                _registerState.value = RegisterState.Success

                registerFcmToken()
            }
            is Resource.Error -> {
                _registerState.value = RegisterState.Error("Đăng ký thành công nhưng đăng nhập thất bại. Vui lòng đăng nhập thủ công.")
            }
            else -> {}
        }
    }

    private fun registerFcmToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                notificationRepository.registerDevice(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}