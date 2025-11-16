package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.ExtendMembershipRequest
import com.tys.gymapp.data.remote.dto.MembershipDetail
import com.tys.gymapp.data.repository.MembershipRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExtendMembershipViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val membershipId: String = savedStateHandle["membershipId"] ?: ""

    private val _membership = MutableStateFlow<MembershipDetail?>(null)
    val membership = _membership.asStateFlow()

    private val _uiState = MutableStateFlow<ExtendMembershipUiState>(ExtendMembershipUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        loadMembership()
    }

    private fun loadMembership() {
        viewModelScope.launch {
            when (val result = membershipRepository.getMembershipById(membershipId)) {
                is Resource.Success -> {
                    _membership.value = result.data
                }
                else -> {
                    _uiState.value = ExtendMembershipUiState.Error("Lỗi tải membership")
                }
            }
        }
    }

    fun extendMembership(additionalDays: Int?, additionalSessions: Int?) {
        viewModelScope.launch {
            _uiState.value = ExtendMembershipUiState.Loading

            // Validation
            if (additionalDays == null && additionalSessions == null) {
                _uiState.value = ExtendMembershipUiState.Error(
                    "Vui lòng nhập số ngày hoặc số buổi muốn gia hạn"
                )
                return@launch
            }

            if (additionalDays != null && additionalDays <= 0) {
                _uiState.value = ExtendMembershipUiState.Error("Số ngày phải lớn hơn 0")
                return@launch
            }

            if (additionalSessions != null && additionalSessions <= 0) {
                _uiState.value = ExtendMembershipUiState.Error("Số buổi phải lớn hơn 0")
                return@launch
            }

            val request = ExtendMembershipRequest(
                additionalDays = additionalDays,
                additionalSessions = additionalSessions
            )

            when (val result = membershipRepository.extendMembership(membershipId, request)) {
                is Resource.Success -> {
                    _uiState.value = ExtendMembershipUiState.Success
                }
                is Resource.Error -> {
                    _uiState.value = ExtendMembershipUiState.Error(
                        result.message ?: "Gia hạn thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _uiState.value = ExtendMembershipUiState.Idle
    }
}

sealed class ExtendMembershipUiState {
    object Idle : ExtendMembershipUiState()
    object Loading : ExtendMembershipUiState()
    object Success : ExtendMembershipUiState()
    data class Error(val message: String) : ExtendMembershipUiState()
}