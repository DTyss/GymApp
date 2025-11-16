package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.Branch
import com.tys.gymapp.data.repository.PlanBranchRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageBranchesViewModel @Inject constructor(
    private val planBranchRepository: PlanBranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManageBranchesUiState>(ManageBranchesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState = _actionState.asStateFlow()

    init {
        loadBranches()
    }

    fun loadBranches() {
        viewModelScope.launch {
            _uiState.value = ManageBranchesUiState.Loading

            when (val result = planBranchRepository.getBranches(active = null)) {
                is Resource.Success -> {
                    _uiState.value = ManageBranchesUiState.Success(
                        branches = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = ManageBranchesUiState.Error(
                        message = result.message ?: "Lỗi tải danh sách chi nhánh"
                    )
                }
                else -> {}
            }
        }
    }

    fun deleteBranch(branchId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            when (val result = planBranchRepository.deleteBranch(branchId)) {
                is Resource.Success -> {
                    _actionState.value = ActionState.Success("Đã xóa chi nhánh")
                    loadBranches() // Reload list
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

sealed class ManageBranchesUiState {
    object Loading : ManageBranchesUiState()
    data class Success(val branches: List<Branch>) : ManageBranchesUiState()
    data class Error(val message: String) : ManageBranchesUiState()
}

