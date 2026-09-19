package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.data.repository.OrderRepository
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
    val salesDaily: Double = 0.0,
    val salesBiweekly: Double = 0.0,
    val salesMonthly: Double = 0.0,
    val salesAnnual: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val inventoryRepository: InventoryRepository = InventoryRepositoryImpl(),
    private val clientRepository: ClientRepository = ClientRepository(RetrofitClient.apiService),
    private val orderRepository: OrderRepository = OrderRepository(RetrofitClient.apiService)
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
                val clients = clientRepository.getClients()
                val orders = orderRepository.getOrders()
                val inventoryAlerts = when (val result = inventoryRepository.getAlerts()) {
                    is NetworkResult.Success -> result.data
                    is NetworkResult.Error -> throw IllegalStateException(result.message)
                }

                _uiState.value = _uiState.value.copy(
                    totalClients = clients.size,
                    activeOrders = orders.count { order ->
                        order.status.equals("activa", ignoreCase = true) ||
                            order.status.equals("pendiente", ignoreCase = true)
                    },
                    deliveredOrders = orders.count { order ->
                        order.status.equals("entregada", ignoreCase = true) ||
                            order.status.equals("finalizada", ignoreCase = true)
                    },
                    lowStockCount = inventoryAlerts.lowStockCount,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar datos: ${exception.message}"
                )
            }
        }
    }
}
