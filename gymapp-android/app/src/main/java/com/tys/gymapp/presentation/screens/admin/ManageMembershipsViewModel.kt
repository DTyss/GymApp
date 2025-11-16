package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.MembershipDetail
import com.tys.gymapp.data.repository.MembershipRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageMembershipsViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManageMembershipsUiState>(ManageMembershipsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<MembershipActionState>(MembershipActionState.Idle)
    val actionState = _actionState.asStateFlow()

    // Filters
    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter = _statusFilter.asStateFlow()

    init {
        loadMemberships()
    }

    fun loadMemberships(userId: String? = null, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = ManageMembershipsUiState.Loading

            when (val result = membershipRepository.getMemberships(userId, status)) {
                is Resource.Success -> {
                    _uiState.value = ManageMembershipsUiState.Success(
                        memberships = result.data?.items ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = ManageMembershipsUiState.Error(
                        message = result.message ?: "Lỗi tải memberships"
                    )
                }
                else -> {}
            }
        }
    }

    fun pauseMembership(id: String) {
        viewModelScope.launch {
            _actionState.value = MembershipActionState.Loading

            when (val result = membershipRepository.pauseMembership(id)) {
                is Resource.Success -> {
                    _actionState.value = MembershipActionState.Success("Đã tạm dừng membership")
                    loadMemberships()
                }
                is Resource.Error -> {
                    _actionState.value = MembershipActionState.Error(
                        result.message ?: "Tạm dừng thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun resumeMembership(id: String) {
        viewModelScope.launch {
            _actionState.value = MembershipActionState.Loading

            when (val result = membershipRepository.resumeMembership(id)) {
                is Resource.Success -> {
                    _actionState.value = MembershipActionState.Success("Đã kích hoạt lại membership")
                    loadMemberships()
                }
                is Resource.Error -> {
                    _actionState.value = MembershipActionState.Error(
                        result.message ?: "Kích hoạt thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
        loadMemberships(status = status)
    }

    fun resetActionState() {
        _actionState.value = MembershipActionState.Idle
    }
}

sealed class ManageMembershipsUiState {
    object Loading : ManageMembershipsUiState()
    data class Success(val memberships: List<MembershipDetail>) : ManageMembershipsUiState()
    data class Error(val message: String) : ManageMembershipsUiState()
}

sealed class MembershipActionState {
    object Idle : MembershipActionState()
    object Loading : MembershipActionState()
    data class Success(val message: String) : MembershipActionState()
    data class Error(val message: String) : MembershipActionState()
}