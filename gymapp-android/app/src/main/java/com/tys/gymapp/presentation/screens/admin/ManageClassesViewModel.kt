package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.*
import com.tys.gymapp.data.repository.ClassRepository
import com.tys.gymapp.data.repository.PlanBranchRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageClassesViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val planBranchRepository: PlanBranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManageClassesUiState>(ManageClassesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState = _actionState.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches = _branches.asStateFlow()

    init {
        loadClasses()
        loadBranches()
    }

    fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = ManageClassesUiState.Loading

            when (val result = classRepository.getClasses()) {
                is Resource.Success -> {
                    _uiState.value = ManageClassesUiState.Success(
                        classes = result.data?.items ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = ManageClassesUiState.Error(
                        message = result.message ?: "Lỗi tải lớp học"
                    )
                }
                else -> {}
            }
        }
    }

    private fun loadBranches() {
        viewModelScope.launch {
            when (val result = planBranchRepository.getBranches()) {
                is Resource.Success -> {
                    _branches.value = result.data ?: emptyList()
                }
                else -> {}
            }
        }
    }

    fun deleteClass(classId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            when (val result = classRepository.deleteClass(classId)) {
                is Resource.Success -> {
                    _actionState.value = ActionState.Success("Đã xóa lớp học")
                    loadClasses() // Reload list
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

sealed class ManageClassesUiState {
    object Loading : ManageClassesUiState()
    data class Success(val classes: List<ClassItem>) : ManageClassesUiState()
    data class Error(val message: String) : ManageClassesUiState()
}

sealed class ActionState {
    object Idle : ActionState()
    object Loading : ActionState()
    data class Success(val message: String) : ActionState()
    data class Error(val message: String) : ActionState()
}