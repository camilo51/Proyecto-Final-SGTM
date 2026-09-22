package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.AuditFilters
import com.example.myapplication.data.model.AuditLog
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Employee
import com.example.myapplication.data.model.Invoice
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.InventoryListQuery
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.data.repository.AuditRepository
import com.example.myapplication.data.repository.EmployeeRepository
import com.example.myapplication.data.repository.InvoiceRepository
import com.example.myapplication.data.repository.MotorcycleRepository
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.data.repository.inventory.InventoryRepository
import com.example.myapplication.data.repository.inventory.InventoryRepositoryImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val searchIndex: List<GlobalSearchResult> = emptyList(),
    val isSearchLoading: Boolean = false
) {
    val searchResults: List<GlobalSearchResult>
        get() = filterGlobalSearchResults(searchIndex, searchQuery)
}

class DashboardViewModel(
    private val inventoryRepository: InventoryRepository = InventoryRepositoryImpl(),
    private val clientRepository: ClientRepository = ClientRepository(RetrofitClient.apiService),
    private val orderRepository: OrderRepository = OrderRepository(RetrofitClient.apiService),
    private val motorcycleRepository: MotorcycleRepository = MotorcycleRepository(RetrofitClient.apiService),
    private val invoiceRepository: InvoiceRepository = InvoiceRepository(RetrofitClient.apiService),
    private val employeeRepository: EmployeeRepository = EmployeeRepository(RetrofitClient.apiService),
    private val userRepository: UserRepository = UserRepository(RetrofitClient.apiService),
    private val auditRepository: AuditRepository = AuditRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSearchLoading = true)
            try {
                val clients = clientRepository.getClients()
                val orders = orderRepository.getOrders()
                val inventoryAlerts = when (val result = inventoryRepository.getAlerts()) {
                    is NetworkResult.Success -> result.data
                    is NetworkResult.Error -> throw IllegalStateException(result.message)
                }
                val searchData = coroutineScope {
                    SearchData(
                        motorcycles = async { runCatching { motorcycleRepository.getMotorcycles() }.getOrDefault(emptyList()) }.await(),
                        inventory = async { runCatching { loadInventory() }.getOrDefault(emptyList()) }.await(),
                        invoices = async { runCatching { invoiceRepository.getInvoices() }.getOrDefault(emptyList()) }.await(),
                        employees = async { runCatching { employeeRepository.getEmployees() }.getOrDefault(emptyList()) }.await(),
                        users = async { runCatching { userRepository.getUsers() }.getOrDefault(emptyList()) }.await(),
                        auditLogs = async {
                            runCatching {
                                when (val result = auditRepository.getAuditLogs(AuditFilters(), page = 1, limit = 100)) {
                                    is NetworkResult.Success -> result.data.items
                                    is NetworkResult.Error -> emptyList()
                                }
                            }.getOrDefault(emptyList())
                        }.await()
                    )
                }

                _uiState.value = _uiState.value.copy(
                    totalClients = clients.size,
                    totalMotos = searchData.motorcycles.size,
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
                    errorMessage = null,
                    searchIndex = buildSearchIndex(clients, orders, searchData),
                    isSearchLoading = false
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSearchLoading = false,
                    errorMessage = "Error al cargar datos: ${exception.message}"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private suspend fun loadInventory(): List<InventoryDto> {
        return when (val result = inventoryRepository.list(InventoryListQuery(limit = 100))) {
            is NetworkResult.Success -> result.data.items
            is NetworkResult.Error -> emptyList()
        }
    }

    private data class SearchData(
        val motorcycles: List<Motorcycle>,
        val inventory: List<InventoryDto>,
        val invoices: List<Invoice>,
        val employees: List<Employee>,
        val users: List<User>,
        val auditLogs: List<AuditLog>
    )

    private fun buildSearchIndex(
        clients: List<Client>,
        orders: List<Order>,
        data: SearchData
    ): List<GlobalSearchResult> = buildList {
        clients.forEach { client ->
            val id = client.id.orEmpty()
            if (id.isNotBlank()) add(
                GlobalSearchResult(
                    GlobalSearchModule.CLIENTS,
                    id,
                    listOf(client.name.orEmpty(), client.lastName.orEmpty()).joinToString(" ").trim()
                        .ifBlank { "Cliente $id" },
                    listOf(client.document, client.phone).filterNotNull().filter(String::isNotBlank).joinToString(" · ").takeIf(String::isNotBlank),
                    listOf(client.name, client.lastName, client.document, client.phone, id).filterNotNull().joinToString(" ")
                )
            )
        }
        data.motorcycles.forEach { motorcycle ->
            val id = motorcycle.id.orEmpty()
            if (id.isNotBlank()) add(
                GlobalSearchResult(
                    GlobalSearchModule.MOTORCYCLES,
                    id,
                    motorcycle.plate?.takeIf(String::isNotBlank) ?: "Motocicleta $id",
                    listOf(motorcycle.brand, motorcycle.model, motorcycle.status).filterNotNull().filter(String::isNotBlank).joinToString(" · ").takeIf(String::isNotBlank),
                    listOf(id, motorcycle.plate, motorcycle.brand, motorcycle.model, motorcycle.color, motorcycle.status, motorcycle.clientId).filterNotNull().joinToString(" ")
                )
            )
        }
        orders.forEach { order ->
            val id = order.id.orEmpty()
            if (id.isNotBlank()) add(
                GlobalSearchResult(
                    GlobalSearchModule.ORDERS,
                    id,
                    order.orderNumber?.takeIf(String::isNotBlank) ?: "Orden $id",
                    listOf(order.status, order.clientId, order.motorcycleId).filter(String::isNotBlank).joinToString(" · ").takeIf(String::isNotBlank),
                    listOf(id, order.orderNumber, order.clientId, order.motorcycleId, order.description, order.status).filterNotNull().joinToString(" ")
                )
            )
        }
        data.inventory.forEach { item ->
            val id = item.id.toString()
            add(
                GlobalSearchResult(
                    GlobalSearchModule.INVENTORY,
                    id,
                    item.name?.takeIf(String::isNotBlank) ?: item.code?.takeIf(String::isNotBlank) ?: "Repuesto $id",
                    listOf(item.code, item.brand, item.category, item.status).filterNotNull().filter(String::isNotBlank).joinToString(" · ").takeIf(String::isNotBlank),
                    listOf(id, item.code, item.name, item.brand, item.category, item.description, item.status).filterNotNull().joinToString(" ")
                )
            )
        }
        data.invoices.forEach { invoice ->
            val id = invoice.id.orEmpty()
            if (id.isNotBlank()) add(
                GlobalSearchResult(
                    GlobalSearchModule.INVOICES,
                    id,
                    invoice.invoiceNumber?.takeIf(String::isNotBlank) ?: "Factura $id",
                    listOf(invoice.paymentStatus, invoice.clientId, invoice.orderId).filter(String::isNotBlank).joinToString(" · ").takeIf(String::isNotBlank),
                    listOf(id, invoice.invoiceNumber, invoice.clientId, invoice.orderId, invoice.paymentMethod, invoice.paymentStatus, invoice.status).filterNotNull().joinToString(" ")
                )
            )
        }
        data.employees.forEach { employee ->
            val id = employee.id.orEmpty()
            if (id.isNotBlank()) add(
                GlobalSearchResult(
                    GlobalSearchModule.EMPLOYEES,
                    id,
                    listOf(employee.name, employee.lastName).joinToString(" ").trim().ifBlank { "Empleado $id" },
                    listOf(employee.specialty, employee.email, employee.phone).filterNotNull().filter(String::isNotBlank).joinToString(" · ").takeIf(String::isNotBlank),
                    listOf(id, employee.name, employee.lastName, employee.specialty, employee.email, employee.phone, employee.role).filterNotNull().joinToString(" ")
                )
            )
        }
        data.users.forEach { user ->
            val id = user.id.orEmpty()
            if (id.isNotBlank()) add(
                GlobalSearchResult(
                    GlobalSearchModule.USERS,
                    id,
                    user.name,
                    listOf(user.email, user.role).filterNotNull().filter(String::isNotBlank).joinToString(" · ").takeIf(String::isNotBlank),
                    listOf(id, user.name, user.email, user.role).filterNotNull().joinToString(" ")
                )
            )
        }
        add(
            GlobalSearchResult(
                GlobalSearchModule.REPORTS,
                "reports",
                "Reportes",
                "Resumen de ventas e inventario",
                "reports reportes ventas inventario ganancias"
            )
        )
        data.auditLogs.forEach { audit ->
            val id = audit.id.orEmpty()
            if (id.isNotBlank()) add(
                GlobalSearchResult(
                    GlobalSearchModule.AUDIT,
                    id,
                    audit.action?.takeIf(String::isNotBlank) ?: "Auditoría $id",
                    listOf(audit.userName, audit.tableName, audit.description).filterNotNull().filter(String::isNotBlank).joinToString(" · ").takeIf(String::isNotBlank),
                    listOf(id, audit.userId, audit.userName, audit.action, audit.tableName, audit.recordId, audit.description, audit.createdAt).filterNotNull().joinToString(" ")
                )
            )
        }
    }

}
