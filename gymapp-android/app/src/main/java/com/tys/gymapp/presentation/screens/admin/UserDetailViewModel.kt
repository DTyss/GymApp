package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.UpdateUserRequest
import com.tys.gymapp.data.repository.UserRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId = savedStateHandle.get<String>("userId") ?: ""

    private val _uiState = MutableStateFlow<UserDetailUiState>(UserDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState = _actionState.asStateFlow()

    init {
        loadUserDetail()
    }

    fun loadUserDetail() {
        viewModelScope.launch {
            _uiState.value = UserDetailUiState.Loading

            when (val result = userRepository.getUserById(userId)) {
                is Resource.Success -> {
                    _uiState.value = UserDetailUiState.Success(
                        userDetail = result.data!!
                    )
                }
                is Resource.Error -> {
                    _uiState.value = UserDetailUiState.Error(
                        message = result.message ?: "Lỗi tải thông tin người dùng"
                    )
                }
                else -> {}
            }
        }
    }

    fun updateUser(request: UpdateUserRequest) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            when (val result = userRepository.updateUser(userId, request)) {
                is Resource.Success -> {
                    _actionState.value = ActionState.Success("Đã cập nhật thông tin")
                    loadUserDetail() // Reload
                }
                is Resource.Error -> {
                    _actionState.value = ActionState.Error(
                        result.message ?: "Cập nhật thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun updateStatus(status: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            when (val result = userRepository.updateStatus(userId, status)) {
                is Resource.Success -> {
                    _actionState.value = ActionState.Success("Đã cập nhật trạng thái")
                    loadUserDetail() // Reload
                }
                is Resource.Error -> {
                    _actionState.value = ActionState.Error(
                        result.message ?: "Cập nhật thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ActionState.Idle
    }
}

sealed class UserDetailUiState {
    object Loading : UserDetailUiState()
    data class Success(val userDetail: com.tys.gymapp.data.remote.dto.UserDetail) : UserDetailUiState()
    data class Error(val message: String) : UserDetailUiState()
}

