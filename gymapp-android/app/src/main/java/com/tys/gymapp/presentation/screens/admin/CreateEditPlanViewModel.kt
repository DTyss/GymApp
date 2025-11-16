package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.CreatePlanRequest
import com.tys.gymapp.data.remote.dto.Plan
import com.tys.gymapp.data.remote.dto.UpdatePlanRequest
import com.tys.gymapp.data.repository.PlanBranchRepository
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.presentation.utils.NavigationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEditPlanViewModel @Inject constructor(
    private val planBranchRepository: PlanBranchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val planJson: String? = savedStateHandle["planJson"]
    private val plan: Plan? = planJson?.let { NavigationUtils.jsonToPlan(it) }
    val editMode = plan != null
    val planId: String? = plan?.id

    private val _uiState = MutableStateFlow<CreateEditPlanUiState>(CreateEditPlanUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Form state
    val name = MutableStateFlow(plan?.name ?: "")
    val price = MutableStateFlow(plan?.price?.toString() ?: "")
    val sessions = MutableStateFlow(plan?.sessions?.toString() ?: "")
    val durationDays = MutableStateFlow(plan?.durationDays?.toString() ?: "")
    val isActive = MutableStateFlow(plan?.isActive ?: true)

    fun createPlan() {
        viewModelScope.launch {
            _uiState.value = CreateEditPlanUiState.Loading

            // Validation
            if (name.value.isBlank()) {
                _uiState.value = CreateEditPlanUiState.Error("Vui lòng nhập tên gói")
                return@launch
            }

            val priceValue = price.value.toDoubleOrNull()
            if (priceValue == null || priceValue <= 0) {
                _uiState.value = CreateEditPlanUiState.Error("Giá không hợp lệ")
                return@launch
            }

            val sessionsValue = sessions.value.toIntOrNull()
            if (sessionsValue == null || sessionsValue <= 0) {
                _uiState.value = CreateEditPlanUiState.Error("Số buổi không hợp lệ")
                return@launch
            }

            val durationValue = durationDays.value.toIntOrNull()
            if (durationValue == null || durationValue <= 0) {
                _uiState.value = CreateEditPlanUiState.Error("Thời hạn không hợp lệ")
                return@launch
            }

            val request = CreatePlanRequest(
                name = name.value,
                price = priceValue,
                sessions = sessionsValue,
                durationDays = durationValue,
                isActive = isActive.value
            )

            when (val result = planBranchRepository.createPlan(request)) {
                is Resource.Success -> {
                    _uiState.value = CreateEditPlanUiState.Success
                }
                is Resource.Error -> {
                    _uiState.value = CreateEditPlanUiState.Error(
                        result.message ?: "Tạo gói tập thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun updatePlan(planId: String) {
        viewModelScope.launch {
            _uiState.value = CreateEditPlanUiState.Loading

            val request = UpdatePlanRequest(
                name = name.value.takeIf { it.isNotBlank() },
                price = price.value.toDoubleOrNull(),
                sessions = sessions.value.toIntOrNull(),
                durationDays = durationDays.value.toIntOrNull(),
                isActive = isActive.value
            )

            when (val result = planBranchRepository.updatePlan(planId, request)) {
                is Resource.Success -> {
                    _uiState.value = CreateEditPlanUiState.Success
                }
                is Resource.Error -> {
                    _uiState.value = CreateEditPlanUiState.Error(
                        result.message ?: "Cập nhật thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateEditPlanUiState.Idle
    }
}

sealed class CreateEditPlanUiState {
    object Idle : CreateEditPlanUiState()
    object Loading : CreateEditPlanUiState()
    object Success : CreateEditPlanUiState()
    data class Error(val message: String) : CreateEditPlanUiState()
}

