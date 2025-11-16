package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.*
import com.tys.gymapp.data.repository.ClassRepository
import com.tys.gymapp.data.repository.PlanBranchRepository
import com.tys.gymapp.domain.util.Resource
import com.tys.gymapp.presentation.utils.NavigationUtils
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
    private val classItem: ClassItem? = classJson?.let { NavigationUtils.jsonToClass(it) }
    val editMode = classItem != null
    val classId: String? = classItem?.id

    private val _uiState = MutableStateFlow<CreateEditClassUiState>(CreateEditClassUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches = _branches.asStateFlow()

    // Form state
    val title = MutableStateFlow(classItem?.title ?: "")
    val description = MutableStateFlow(classItem?.description ?: "")
    val selectedBranchId = MutableStateFlow<String?>(classItem?.branch?.id)
    val trainerId = MutableStateFlow(classItem?.trainer?.id ?: "")  // Current user if trainer
    val startDateTime = MutableStateFlow(classItem?.startTime?.let { formatDateTimeForInput(it) } ?: "")
    val endDateTime = MutableStateFlow(classItem?.endTime?.let { formatDateTimeForInput(it) } ?: "")
    val capacity = MutableStateFlow(classItem?.capacity?.toString() ?: "")

    init {
        loadBranches()
    }

    private fun formatDateTimeForInput(isoString: String): String {
        return try {
            val dateTime = LocalDateTime.parse(isoString.replace("Z", ""))
            dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        } catch (e: Exception) {
            isoString
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

            // Format datetime to ISO format
            val startTime = formatToISO(startDateTime.value)
            val endTime = formatToISO(endDateTime.value)

            if (startTime == null || endTime == null) {
                _uiState.value = CreateEditClassUiState.Error("Định dạng thời gian không hợp lệ")
                return@launch
            }

            val request = CreateClassRequest(
                title = title.value,
                description = description.value.ifBlank { null },
                trainerId = trainerId.value,
                branchId = selectedBranchId.value!!,
                startTime = startTime,
                endTime = endTime,
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

    fun updateClass() {
        viewModelScope.launch {
            if (classId == null) {
                _uiState.value = CreateEditClassUiState.Error("Class ID không hợp lệ")
                return@launch
            }

            _uiState.value = CreateEditClassUiState.Loading

            val startTime = startDateTime.value.takeIf { it.isNotBlank() }?.let { formatToISO(it) }
            val endTime = endDateTime.value.takeIf { it.isNotBlank() }?.let { formatToISO(it) }

            val request = UpdateClassRequest(
                title = title.value.takeIf { it.isNotBlank() },
                description = description.value.takeIf { it.isNotBlank() },
                trainerId = trainerId.value.takeIf { it.isNotBlank() },
                branchId = selectedBranchId.value,
                startTime = startTime,
                endTime = endTime,
                capacity = capacity.value.toIntOrNull()
            )

            when (val result = classRepository.updateClass(classId, request)) {
                is Resource.Success -> {
                    _uiState.value = CreateEditClassUiState.Success
                }
                is Resource.Error -> {
                    _uiState.value = CreateEditClassUiState.Error(
                        result.message ?: "Cập nhật thất bại"
                    )
                }
                else -> {}
            }
        }
    }

    private fun formatToISO(dateTimeString: String): String? {
        return try {
            val dateTime = LocalDateTime.parse(dateTimeString)
            dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
        } catch (e: Exception) {
            null
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