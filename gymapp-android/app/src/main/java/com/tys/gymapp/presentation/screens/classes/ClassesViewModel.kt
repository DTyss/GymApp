package com.tys.gymapp.presentation.screens.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.ClassItem
import com.tys.gymapp.data.repository.BookingRepository
import com.tys.gymapp.data.repository.ClassRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState = _bookingState.asStateFlow()

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = ClassesUiState.Loading
            when (val result = classRepository.getClasses()) {
                is Resource.Success -> {
                    _uiState.value = ClassesUiState.Success(result.data?.items ?: emptyList())
                }
                is Resource.Error -> {
                    _uiState.value = ClassesUiState.Error(result.message ?: "Lỗi tải lớp học")
                }
                else -> {}
            }
        }
    }

    fun bookClass(classId: String) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            when (val result = bookingRepository.createBooking(classId)) {
                is Resource.Success -> {
                    _bookingState.value = BookingState.Success("Đặt lớp thành công!")
                    loadClasses() // Reload để cập nhật available slots
                }
                is Resource.Error -> {
                    _bookingState.value = BookingState.Error(result.message ?: "Đặt lớp thất bại")
                }
                else -> {}
            }
        }
    }

    fun resetBookingState() {
        _bookingState.value = BookingState.Idle
    }
}

sealed class ClassesUiState {
    object Loading : ClassesUiState()
    data class Success(val classes: List<ClassItem>) : ClassesUiState()
    data class Error(val message: String) : ClassesUiState()
}

sealed class BookingState {
    object Idle : BookingState()
    object Loading : BookingState()
    data class Success(val message: String) : BookingState()
    data class Error(val message: String) : BookingState()
}