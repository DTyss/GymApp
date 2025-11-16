package com.tys.gymapp.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.UserMe
import com.tys.gymapp.data.repository.AuthRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            when (val result = authRepository.getMe()) {
                is Resource.Success -> {
                    _uiState.value = ProfileUiState.Success(result.data!!)
                }
                is Resource.Error -> {
                    _uiState.value = ProfileUiState.Error(
                        message = result.message ?: "Lỗi tải hồ sơ"
                    )
                }
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = ProfileUiState.LoggedOut
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: UserMe) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object LoggedOut : ProfileUiState()
}