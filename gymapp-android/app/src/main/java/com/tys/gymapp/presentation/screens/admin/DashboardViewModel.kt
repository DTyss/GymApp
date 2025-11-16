package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.DashboardStats
import com.tys.gymapp.data.repository.StatsRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            when (val result = statsRepository.getDashboard()) {
                is Resource.Success -> {
                    _uiState.value = DashboardUiState.Success(result.data!!)
                }
                is Resource.Error -> {
                    _uiState.value = DashboardUiState.Error(
                        message = result.message ?: "Lỗi tải dashboard"
                    )
                }
                else -> {}
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val stats: DashboardStats) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}