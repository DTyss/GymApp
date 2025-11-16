package com.tys.gymapp.presentation.screens.plans

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
class PlansViewModel @Inject constructor(
    private val planBranchRepository: PlanBranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlansUiState>(PlansUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.value = PlansUiState.Loading

            when (val result = planBranchRepository.getPlans(active = true)) {
                is Resource.Success -> {
                    _uiState.value = PlansUiState.Success(
                        plans = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = PlansUiState.Error(
                        message = result.message ?: "Lỗi tải danh sách gói tập"
                    )
                }
                else -> {}
            }
        }
    }
}

sealed class PlansUiState {
    object Loading : PlansUiState()
    data class Success(val plans: List<Plan>) : PlansUiState()
    data class Error(val message: String) : PlansUiState()
}