package com.tys.gymapp.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.data.remote.dto.Notification
import com.tys.gymapp.data.repository.NotificationRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading

            when (val result = notificationRepository.getMyNotifications()) {
                is Resource.Success -> {
                    _uiState.value = NotificationsUiState.Success(
                        notifications = result.data?.items ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = NotificationsUiState.Error(
                        message = result.message ?: "Lỗi tải thông báo"
                    )
                }
                else -> {}
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
            loadNotifications()
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            loadNotifications()
        }
    }
}

sealed class NotificationsUiState {
    object Loading : NotificationsUiState()
    data class Success(val notifications: List<Notification>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}