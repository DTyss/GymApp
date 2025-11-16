package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.SavedStateHandle
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CreateEditClassViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val planBranchRepository: PlanBranchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classJson: String? = savedStateHandle["classJson"]
    private val editMode = classJson != null

    private val _uiState = MutableStateFlow<CreateEditClassUiState>(CreateEditClassUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches = _branches.asStateFlow()

    // Form state
    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val selectedBranchId = MutableStateFlow<String?>(null)
    val trainerId = MutableStateFlow("")  // Current user if trainer
    val startDateTime = MutableStateFlow("")
    val endDateTime = MutableStateFlow("")
    val capacity = MutableStateFlow("")

    init {
        loadBranches()
        // TODO: Load trainers list for dropdown
        // TODO: Parse classJson if edit mode and populate fields
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

    fun createClass() {
        viewModelScope.launch {
            _uiState.value = CreateEditClassUiState.Loading

            // Validation
            if (title.value.isBlank()) {
                _uiState.value = CreateEditClassUiState.Error("Vui lòng nhập tên lớp")
                return@launch
            }

            if (selectedBranchId.value == null) {
                _uiState.value = CreateEditClassUiState.Error("Vui lòng chọn chi nhánh")
                return@launch
            }

            if (trainerId.value.isBlank()) {
                _uiState.value = CreateEditClassUiState.Error("Trainer ID không hợp lệ")
                return@launch
            }

            if (startDateTime.value.isBlank() || endDateTime.value.isBlank()) {
                _uiState.value = CreateEditClassUiState.Error("Vui lòng chọn thời gian")
                return@launch
            }

            val cap = capacity.value.toIntOrNull()
            if (cap == null || cap <= 0) {
                _uiState.value = CreateEditClassUiState.Error("Sức chứa không hợp lệ")
                return@launch
            }

            val request = CreateClassRequest(
                title = title.value,
                description = description.value.ifBlank { null },
                trainerId = trainerId.value,
                branchId = selectedBranchId.value!!,
                startTime = startDateTime.value,
                endTime = endDateTime.value,
                capacity = cap
            )

            when (val result = classRepository.createClass(request)) {
                is Resource.Success -> {
                    _uiState.value = CreateEditClassUiState.Success
                }
                is Resource.Error -> {
                    _uiState.value = CreateEditClassUiState.Error(
                        result.message ?: "Tạo lớp thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateEditClassUiState.Idle
    }
}

sealed class CreateEditClassUiState {
    object Idle : CreateEditClassUiState()
    object Loading : CreateEditClassUiState()
    object Success : CreateEditClassUiState()
    data class Error(val message: String) : CreateEditClassUiState()
}