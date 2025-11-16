package com.tys.gymapp.presentation.screens.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.Checkin
import com.tys.gymapp.data.repository.CheckinRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckinHistoryViewModel @Inject constructor(
    private val checkinRepository: CheckinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckinHistoryUiState>(CheckinHistoryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadCheckins()
    }

    fun loadCheckins(from: String? = null, to: String? = null) {
        viewModelScope.launch {
            _uiState.value = CheckinHistoryUiState.Loading

            when (val result = checkinRepository.getMyCheckins(from = from, to = to)) {
                is Resource.Success -> {
                    _uiState.value = CheckinHistoryUiState.Success(
                        checkins = result.data?.items ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = CheckinHistoryUiState.Error(
                        message = result.message ?: "Lỗi tải lịch sử check-in"
                    )
                }
                else -> {}
            }
        }
    }
}

sealed class CheckinHistoryUiState {
    object Loading : CheckinHistoryUiState()
    data class Success(val checkins: List<Checkin>) : CheckinHistoryUiState()
    data class Error(val message: String) : CheckinHistoryUiState()
}