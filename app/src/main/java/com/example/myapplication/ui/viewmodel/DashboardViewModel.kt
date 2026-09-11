package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.repository.ClientRepository
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

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val api = RetrofitClient.apiService
                
                // Fetching all data in parallel or sequence
                val clients = ClientRepository(api).getClients()
                val motorcycles = api.getMotorcycles()
                val orders = api.getOrders()
                val inventory = api.getInventory()
                val invoices = api.getInvoices()

                _uiState.value = DashboardUiState(
                    totalClients = clients.size,
                    totalMotos = motorcycles.size,
                    activeOrders = orders.count { it.status.lowercase() == "activa" || it.status.lowercase() == "pendiente" },
                    deliveredOrders = orders.count { it.status.lowercase() == "entregada" || it.status.lowercase() == "finalizada" },
                    lowStockCount = inventory.count { it.quantity < 5 },
                    totalSales = invoices.sumOf { it.total },
                    isLoading = false
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
