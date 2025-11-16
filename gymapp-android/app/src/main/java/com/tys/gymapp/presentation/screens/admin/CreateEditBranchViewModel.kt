package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.Branch
import com.tys.gymapp.data.remote.dto.CreateBranchRequest
import com.tys.gymapp.data.remote.dto.UpdateBranchRequest
import com.tys.gymapp.data.repository.PlanBranchRepository
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.presentation.utils.NavigationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEditBranchViewModel @Inject constructor(
    private val planBranchRepository: PlanBranchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val branchJson: String? = savedStateHandle["branchJson"]
    private val branch: Branch? = branchJson?.let { NavigationUtils.jsonToBranch(it) }
    val editMode = branch != null
    val branchId: String? = branch?.id

    private val _uiState = MutableStateFlow<CreateEditBranchUiState>(CreateEditBranchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Form state
    val name = MutableStateFlow(branch?.name ?: "")
    val address = MutableStateFlow(branch?.address ?: "")
    val isActive = MutableStateFlow(branch?.isActive ?: true)

    fun createBranch() {
        viewModelScope.launch {
            _uiState.value = CreateEditBranchUiState.Loading

            // Validation
            if (name.value.isBlank()) {
                _uiState.value = CreateEditBranchUiState.Error("Vui lòng nhập tên chi nhánh")
                return@launch
            }

            if (name.value.length < 3) {
                _uiState.value = CreateEditBranchUiState.Error("Tên chi nhánh phải có ít nhất 3 ký tự")
                return@launch
            }

            val request = CreateBranchRequest(
                name = name.value,
                address = address.value.takeIf { it.isNotBlank() },
                isActive = isActive.value
            )

            when (val result = planBranchRepository.createBranch(request)) {
                is Resource.Success -> {
                    _uiState.value = CreateEditBranchUiState.Success
                }
                is Resource.Error -> {
                    _uiState.value = CreateEditBranchUiState.Error(
                        result.message ?: "Tạo chi nhánh thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun updateBranch(branchId: String) {
        viewModelScope.launch {
            _uiState.value = CreateEditBranchUiState.Loading

            val request = UpdateBranchRequest(
                name = name.value.takeIf { it.isNotBlank() },
                address = address.value.takeIf { it.isNotBlank() },
                isActive = isActive.value
            )

            when (val result = planBranchRepository.updateBranch(branchId, request)) {
                is Resource.Success -> {
                    _uiState.value = CreateEditBranchUiState.Success
                }
                is Resource.Error -> {
                    _uiState.value = CreateEditBranchUiState.Error(
                        result.message ?: "Cập nhật thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateEditBranchUiState.Idle
    }
}

sealed class CreateEditBranchUiState {
    object Idle : CreateEditBranchUiState()
    object Loading : CreateEditBranchUiState()
    object Success : CreateEditBranchUiState()
    data class Error(val message: String) : CreateEditBranchUiState()
}

