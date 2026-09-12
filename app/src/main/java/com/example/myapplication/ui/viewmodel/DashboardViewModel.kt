package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.repository.inventory.InventoryRepository
import com.example.myapplication.data.repository.inventory.InventoryRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalClients: Int = 0,
    val totalMotos: Int = 0,
    val activeOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val lowStockCount: Int = 0,
    val totalSales: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val inventoryRepository: InventoryRepository = InventoryRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val inventoryAlerts = when (val result = inventoryRepository.getAlerts()) {
                    is NetworkResult.Success -> result.data
                    is NetworkResult.Error -> throw IllegalStateException(result.message)
                }

                _uiState.value = _uiState.value.copy(
                    lowStockCount = inventoryAlerts.lowStockCount,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }
}
