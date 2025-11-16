package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.User
import com.tys.gymapp.data.repository.UserRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageUsersViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManageUsersUiState>(ManageUsersUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState = _actionState.asStateFlow()

    private val _roleFilter = MutableStateFlow<String?>(null)
    val roleFilter = _roleFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = ManageUsersUiState.Loading

            when (val result = userRepository.getUsers(
                role = _roleFilter.value,
                status = _statusFilter.value,
                search = _searchQuery.value.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success -> {
                    _uiState.value = ManageUsersUiState.Success(
                        users = result.data?.items ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = ManageUsersUiState.Error(
                        message = result.message ?: "Lỗi tải danh sách người dùng"
                    )
                }
                else -> {}
            }
        }
    }

    fun setRoleFilter(role: String?) {
        _roleFilter.value = role
        loadUsers()
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
        loadUsers()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // Debounce search - reload after user stops typing
    }

    fun updateUserStatus(userId: String, status: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            when (val result = userRepository.updateStatus(userId, status)) {
                is Resource.Success -> {
                    _actionState.value = ActionState.Success("Đã cập nhật trạng thái")
                    loadUsers()
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

sealed class ManageUsersUiState {
    object Loading : ManageUsersUiState()
    data class Success(val users: List<User>) : ManageUsersUiState()
    data class Error(val message: String) : ManageUsersUiState()
}

