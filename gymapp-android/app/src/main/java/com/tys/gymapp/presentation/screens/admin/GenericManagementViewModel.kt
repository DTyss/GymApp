package com.tys.gymapp.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tys.gymapp.domain.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class GenericManagementViewModel<T> : ViewModel() {

    private val _uiState = MutableStateFlow<ManagementUiState<T>>(ManagementUiState.Loading())
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState = _actionState.asStateFlow()

    init {
        loadItems()
    }

    abstract suspend fun fetchItems(): Resource<List<T>>
    abstract suspend fun deleteItem(id: String): Resource<Any>

    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = ManagementUiState.Loading()

            when (val result = fetchItems()) {
                is Resource.Success -> {
                    _uiState.value = ManagementUiState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _uiState.value = ManagementUiState.Error(
                        result.message ?: "Lỗi tải dữ liệu"
                    )
                }
                else -> {}
            }
        }
    }

    fun deleteItemById(id: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            when (val result = deleteItem(id)) {
                is Resource.Success -> {
                    _actionState.value = ActionState.Success("Đã xóa thành công")
                    loadItems()
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

sealed class ManagementUiState<T> {
    class Loading<T> : ManagementUiState<T>()
    data class Success<T>(val items: List<T>) : ManagementUiState<T>()
    data class Error<T>(val message: String) : ManagementUiState<T>()
}