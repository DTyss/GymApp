package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.CreateMembershipRequest
import com.tys.gymapp.data.remote.dto.Plan
import com.tys.gymapp.data.repository.MembershipRepository
import com.tys.gymapp.data.repository.PlanBranchRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMembershipViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository,
    private val planBranchRepository: PlanBranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateMembershipUiState>(CreateMembershipUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _plans = MutableStateFlow<List<Plan>>(emptyList())
    val plans = _plans.asStateFlow()

    init {
        loadPlans()
    }

    private fun loadPlans() {
        viewModelScope.launch {
            when (val result = planBranchRepository.getPlans()) {
                is Resource.Success -> {
                    _plans.value = result.data ?: emptyList()
                }
                else -> {}
            }
        }
    }

    fun createMembership(userId: String, planId: String, startDate: String?) {
        viewModelScope.launch {
            _uiState.value = CreateMembershipUiState.Loading

            // Validation
            if (userId.isBlank()) {
                _uiState.value = CreateMembershipUiState.Error("Vui lòng nhập User ID")
                return@launch
            }

            if (planId.isBlank()) {
                _uiState.value = CreateMembershipUiState.Error("Vui lòng chọn gói tập")
                return@launch
            }

            val request = CreateMembershipRequest(
                userId = userId,
                planId = planId,
                startDate = startDate  // null = today
            )

            when (val result = membershipRepository.createMembership(request)) {
                is Resource.Success -> {
                    _uiState.value = CreateMembershipUiState.Success
                }
                is Resource.Error -> {
                    _uiState.value = CreateMembershipUiState.Error(
                        result.message ?: "Tạo membership thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateMembershipUiState.Idle
    }
}

sealed class CreateMembershipUiState {
    object Idle : CreateMembershipUiState()
    object Loading : CreateMembershipUiState()
    object Success : CreateMembershipUiState()
    data class Error(val message: String) : CreateMembershipUiState()
}