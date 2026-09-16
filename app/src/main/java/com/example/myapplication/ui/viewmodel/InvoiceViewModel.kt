package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Invoice
import com.example.myapplication.data.model.InvoiceRequest
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.data.repository.InvoiceRepository
import com.example.myapplication.data.repository.OrderRepository
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvoiceUiState(
    val invoices: List<Invoice> = emptyList(),
    val filteredInvoices: List<Invoice> = emptyList(),
    val clients: List<Client> = emptyList(),
    val orders: List<Order> = emptyList(),
    val searchQuery: String = "",
    val paidTodayTotal: Double = 0.0,
    val pendingTotal: Double = 0.0,
    val selectedInvoice: Invoice? = null,
    val isLoadingDetail: Boolean = false,
    val invoiceUpdatedVersion: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingReferences: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val operationMessage: String? = null,
    val creationVersion: Int = 0
)

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository = InvoiceRepository(RetrofitClient.apiService),
    private val clientRepository: ClientRepository = ClientRepository(RetrofitClient.apiService),
    private val orderRepository: OrderRepository = OrderRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState = _uiState.asStateFlow()

    fun loadInvoices() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val invoices = invoiceRepository.getInvoices()
                _uiState.update {
                    it.copy(invoices = invoices, isLoading = false).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = exception.userMessage("No se pudieron cargar las facturas"))
                }
            }
        }
    }

    fun refreshInvoices() {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                val invoices = invoiceRepository.getInvoices()
                _uiState.update {
                    it.copy(invoices = invoices, isRefreshing = false).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = exception.userMessage("No se pudieron actualizar las facturas"))
                }
            }
        }
    }

    fun loadReferences() {
        if (_uiState.value.isLoadingReferences) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReferences = true) }
            try {
                val (clients, orders) = coroutineScope {
                    val clientsRequest = async { clientRepository.getClients() }
                    val ordersRequest = async { orderRepository.getOrders() }
                    clientsRequest.await() to ordersRequest.await()
                }
                _uiState.update { it.copy(clients = clients, orders = orders, isLoadingReferences = false).withFilters() }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingReferences = false,
                        errorMessage = it.errorMessage ?: exception.userMessage("No se pudieron cargar las órdenes disponibles")
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).withFilters() }
    }

    fun createInvoice(orderId: String, paymentMethod: String, notes: String) {
        if (_uiState.value.isSaving) return

        val order = _uiState.value.orders.firstOrNull { it.id == orderId }
        when {
            order == null -> {
                _uiState.update { it.copy(operationMessage = "Selecciona una orden válida") }
                return
            }
            _uiState.value.invoices.any { invoice -> invoice.orderId == orderId && !invoice.isCancelled() } -> {
                _uiState.update { it.copy(operationMessage = "Ya existe una factura vigente asociada a esta orden de trabajo") }
                return
            }
            paymentMethod.isBlank() -> {
                _uiState.update { it.copy(operationMessage = "Selecciona un modo de pago") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, operationMessage = null, errorMessage = null) }
            try {
                val created = invoiceRepository.createInvoice(
                    InvoiceRequest(
                        orderId = orderId,
                        paymentMethod = paymentMethod,
                        notes = notes.trim().takeIf(String::isNotBlank)
                    )
                )
                _uiState.update {
                    it.copy(
                        invoices = (it.invoices + created).distinctBy(Invoice::id),
                        isSaving = false,
                        operationMessage = "Factura creada correctamente",
                        creationVersion = it.creationVersion + 1
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, operationMessage = exception.userMessage("No se pudo crear la factura"))
                }
            }
        }
    }

    fun loadInvoice(id: String) {
        if (id.isBlank() || _uiState.value.isLoadingDetail) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, selectedInvoice = null, errorMessage = null) }
            try {
                val invoice = invoiceRepository.getInvoice(id)
                _uiState.update { it.copy(isLoadingDetail = false, selectedInvoice = invoice) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoadingDetail = false, errorMessage = exception.userMessage("No se pudo cargar la factura")) }
            }
        }
    }

    fun payInvoice() {
        val invoice = _uiState.value.selectedInvoice ?: return
        val id = invoice.id?.takeIf(String::isNotBlank) ?: return
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, operationMessage = null) }
            try {
                val updated = invoiceRepository.payInvoice(id)
                _uiState.update {
                    it.copy(
                        invoices = it.invoices.map { current -> if (current.id == id) updated else current },
                        selectedInvoice = updated,
                        isSaving = false,
                        operationMessage = "Factura marcada como pagada",
                        invoiceUpdatedVersion = it.invoiceUpdatedVersion + 1
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isSaving = false, operationMessage = exception.userMessage("No se pudo actualizar la factura")) }
            }
        }
    }

    fun cancelInvoice() {
        val invoice = _uiState.value.selectedInvoice ?: return
        val id = invoice.id?.takeIf(String::isNotBlank) ?: return
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, operationMessage = null) }
            try {
                val updated = invoiceRepository.cancelInvoice(id)
                _uiState.update {
                    it.copy(
                        invoices = it.invoices.map { current -> if (current.id == id) updated else current },
                        selectedInvoice = updated,
                        isSaving = false,
                        operationMessage = "Factura anulada",
                        invoiceUpdatedVersion = it.invoiceUpdatedVersion + 1
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isSaving = false, operationMessage = exception.userMessage("No se pudo anular la factura")) }
            }
        }
    }

    fun clearOperationMessage() {
        _uiState.update { it.copy(operationMessage = null) }
    }
}

private fun InvoiceUiState.withFilters(): InvoiceUiState {
    val query = searchQuery.trim().lowercase()
    val clientsById = clients.associateBy { it.id }
    val ordersById = orders.associateBy { it.id }
    val filtered = invoices.filter { invoice ->
        query.isBlank() || listOf(
            invoice.id.orEmpty(),
            invoice.status,
            invoice.paymentMethod,
            invoice.notes.orEmpty(),
            clientsById[invoice.clientId]?.name.orEmpty(),
            ordersById[invoice.orderId]?.orderNumber.orEmpty(),
            invoice.orderId
        ).any { value -> value.lowercase().contains(query) }
    }
    return copy(
        filteredInvoices = filtered,
        paidTodayTotal = invoices.filter(Invoice::isPaidToday).sumOf(Invoice::total),
        pendingTotal = invoices.filter(Invoice::isPending).sumOf(Invoice::total)
    )
}

private fun Exception.userMessage(fallback: String): String = message?.takeIf(String::isNotBlank) ?: fallback

private fun Invoice.isPaidToday(): Boolean = !isCancelled() &&
    isPaid() && date?.substringBefore('T')?.substringBefore(' ') == LocalDate.now().toString()

private fun Invoice.isPending(): Boolean = !isCancelled() && !isPaid()

private fun Invoice.isCancelled(): Boolean = listOf(paymentStatus, status).any { value ->
    value.lowercase().contains("anulad") || value.lowercase().contains("cancel")
}

private fun Invoice.isPaid(): Boolean = !isCancelled() && listOf(paymentStatus, status).any { value ->
    value.lowercase().let { normalized ->
        normalized.contains("pagad") || normalized.contains("paid") || normalized.contains("complet")
    }
}
