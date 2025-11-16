package com.tys.gymapp.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.Membership
import com.tys.gymapp.data.remote.dto.UserMe
import com.tys.gymapp.data.repository.AuthRepository
import com.tys.gymapp.data.repository.PlanBranchRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val planBranchRepository: PlanBranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // Load user info
            when (val userResult = authRepository.getMe()) {
                is Resource.Success -> {
                    // Load memberships
                    when (val membershipResult = planBranchRepository.getMyMemberships()) {
                        is Resource.Success -> {
                            _uiState.value = HomeUiState.Success(
                                user = userResult.data!!,
                                memberships = membershipResult.data ?: emptyList()
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = HomeUiState.Error(
                                membershipResult.message ?: "Lỗi tải memberships"
                            )
                        }
                        else -> {}
                    }
                }
                is Resource.Error -> {
                    _uiState.value = HomeUiState.Error(
                        userResult.message ?: "Lỗi tải thông tin người dùng"
                    )
                }
                else -> {}
            }
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val user: UserMe,
        val memberships: List<Membership>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}