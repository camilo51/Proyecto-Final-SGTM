package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DashboardUiState(
    val totalClients: Int = 0,
    val totalMotos: Int = 0,
    val activeOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val lowStockCount: Int = 0,
    val totalSales: Double = 0.0,
    val dailySales: Double = 0.0,
    val fortnightlySales: Double = 0.0,
    val monthlySales: Double = 0.0,
    val yearlySales: Double = 0.0,
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
                val clients = api.getClients()
                val motorcycles = api.getMotorcycles()
                val orders = api.getOrders()
                val inventory = api.getInventory()
                val invoices = api.getInvoices()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val now = Calendar.getInstance()
                
                val startOfDay = now.clone() as Calendar
                startOfDay.set(Calendar.HOUR_OF_DAY, 0)
                startOfDay.set(Calendar.MINUTE, 0)
                startOfDay.set(Calendar.SECOND, 0)
                startOfDay.set(Calendar.MILLISECOND, 0)

                val startOfMonth = now.clone() as Calendar
                startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
                startOfMonth.set(Calendar.MINUTE, 0)
                startOfMonth.set(Calendar.SECOND, 0)
                startOfMonth.set(Calendar.MILLISECOND, 0)

                val startOfYear = now.clone() as Calendar
                startOfYear.set(Calendar.DAY_OF_YEAR, 1)
                startOfYear.set(Calendar.HOUR_OF_DAY, 0)
                startOfYear.set(Calendar.MINUTE, 0)
                startOfYear.set(Calendar.SECOND, 0)
                startOfYear.set(Calendar.MILLISECOND, 0)

                val fifteenDaysAgo = now.clone() as Calendar
                fifteenDaysAgo.add(Calendar.DAY_OF_YEAR, -15)

                var daily = 0.0
                var fortnightly = 0.0
                var monthly = 0.0
                var yearly = 0.0

                invoices.forEach { invoice ->
                    try {
                        val invoiceDateStr = invoice.date.substringBefore("T") // Handle ISO dates like 2023-10-27T10:00:00
                        val date = dateFormat.parse(invoiceDateStr)
                        if (date != null) {
                            val invoiceCal = Calendar.getInstance()
                            invoiceCal.time = date
                            
                            if (invoiceCal >= startOfDay) {
                                daily += invoice.total
                            }
                            if (invoiceCal.after(fifteenDaysAgo)) {
                                fortnightly += invoice.total
                            }
                            if (invoiceCal >= startOfMonth) {
                                monthly += invoice.total
                            }
                            if (invoiceCal >= startOfYear) {
                                yearly += invoice.total
                            }
                        }
                    } catch (e: Exception) {
                        // Skip if date parsing fails
                    }
                }

                _uiState.value = DashboardUiState(
                    totalClients = clients.size,
                    totalMotos = motorcycles.size,
                    activeOrders = orders.count { it.status.lowercase() == "activa" || it.status.lowercase() == "pendiente" },
                    deliveredOrders = orders.count { it.status.lowercase() == "entregada" || it.status.lowercase() == "finalizada" },
                    lowStockCount = inventory.count { it.quantity < 5 },
                    totalSales = invoices.sumOf { it.total },
                    dailySales = daily,
                    fortnightlySales = fortnightly,
                    monthlySales = monthly,
                    yearlySales = yearly,
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
