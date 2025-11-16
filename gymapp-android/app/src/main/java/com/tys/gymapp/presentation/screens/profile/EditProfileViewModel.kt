package com.tys.gymapp.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.UserMe
import com.tys.gymapp.data.repository.AuthRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading

            when (val result = authRepository.getMe()) {
                is Resource.Success -> {
                    _uiState.value = EditProfileUiState.Success(result.data!!)
                }
                is Resource.Error -> {
                    _uiState.value = EditProfileUiState.Error(
                        message = result.message ?: "Lỗi tải hồ sơ"
                    )
                }
                else -> {}
            }
        }
    }

    fun updateProfile(fullName: String, email: String?, phone: String?) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading

            // Validate
            if (fullName.isBlank()) {
                _updateState.value = UpdateState.Error("Họ tên không được để trống")
                return@launch
            }

            if (fullName.length < 2) {
                _updateState.value = UpdateState.Error("Họ tên phải có ít nhất 2 ký tự")
                return@launch
            }

            // Email validation
            if (!email.isNullOrBlank() && !email.contains("@")) {
                _updateState.value = UpdateState.Error("Email không hợp lệ")
                return@launch
            }

            // Phone validation
            if (!phone.isNullOrBlank() && !phone.matches(Regex("^[0-9]{10,11}$"))) {
                _updateState.value = UpdateState.Error("Số điện thoại không hợp lệ")
                return@launch
            }

            // Call API
            when (val result = authRepository.updateProfile(fullName, email, phone)) {
                is Resource.Success -> {
                    _updateState.value = UpdateState.Success
                    // Reload profile
                    loadProfile()
                }
                is Resource.Error -> {
                    _updateState.value = UpdateState.Error(
                        result.message ?: "Cập nhật thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}

sealed class EditProfileUiState {
    object Loading : EditProfileUiState()
    data class Success(val user: UserMe) : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}