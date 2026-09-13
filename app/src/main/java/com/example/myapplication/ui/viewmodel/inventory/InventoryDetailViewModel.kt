package com.example.myapplication.ui.viewmodel.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.inventory.DeleteInventoryRequest
import com.example.myapplication.data.model.inventory.InventoryAlertSummary
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.InventoryMovementDto
import com.example.myapplication.data.repository.inventory.InventoryRefreshBus
import com.example.myapplication.data.repository.inventory.InventoryRepository
import com.example.myapplication.data.repository.inventory.InventoryRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InventoryDetailUiState(
    val item: InventoryDto? = null,
    val recentMovements: List<InventoryMovementDto> = emptyList(),
    val alerts: InventoryAlertSummary = InventoryAlertSummary(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val deletionCompleted: Boolean = false
)

class InventoryDetailViewModel(
    private val repository: InventoryRepository = InventoryRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryDetailUiState())
    val uiState = _uiState.asStateFlow()
    private var currentId: Long? = null

    init {
        viewModelScope.launch {
            InventoryRefreshBus.changes.collect { currentId?.let(::load) }
        }
    }

    fun load(id: Long) {
        currentId = id
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val detail = repository.getDetail(id)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(item = detail.data)
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = detail.message)
            }
            when (val movements = repository.getMovements(id, page = 1, limit = 5)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    recentMovements = movements.data.movements
                )
                else -> Unit
            }
            when (val alerts = repository.getAlerts()) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(alerts = alerts.data)
                else -> Unit
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun delete(reason: String) {
        val itemId = currentId ?: return
        if (_uiState.value.isDeleting) return
        if (reason.trim().length < 3) {
            _uiState.value = _uiState.value.copy(errorMessage = "Indica un motivo descriptivo para eliminar el repuesto.")
            return
        }
        _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.delete(itemId, DeleteInventoryRequest(reason.trim()))) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deletionCompleted = true
                )
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun consumeDeletion() {
        _uiState.value = _uiState.value.copy(deletionCompleted = false)
    }
}
