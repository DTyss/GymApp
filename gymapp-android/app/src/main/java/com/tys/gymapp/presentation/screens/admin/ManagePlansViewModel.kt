package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.Plan
import com.tys.gymapp.data.repository.PlanBranchRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagePlansViewModel @Inject constructor(
    private val planBranchRepository: PlanBranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManagePlansUiState>(ManagePlansUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState = _actionState.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.value = ManagePlansUiState.Loading

            when (val result = planBranchRepository.getPlans(active = null)) {
                is Resource.Success -> {
                    _uiState.value = ManagePlansUiState.Success(
                        plans = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = ManagePlansUiState.Error(
                        message = result.message ?: "Lỗi tải danh sách gói tập"
                    )
                }
                else -> {}
            }
        }
    }

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            when (val result = planBranchRepository.deletePlan(planId)) {
                is Resource.Success -> {
                    _actionState.value = ActionState.Success("Đã xóa gói tập")
                    loadPlans() // Reload list
                }
                is Resource.Error -> {
                    _actionState.value = ActionState.Error(
                        result.message ?: "Xóa thất bại"
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

sealed class ManagePlansUiState {
    object Loading : ManagePlansUiState()
    data class Success(val plans: List<Plan>) : ManagePlansUiState()
    data class Error(val message: String) : ManagePlansUiState()
}

