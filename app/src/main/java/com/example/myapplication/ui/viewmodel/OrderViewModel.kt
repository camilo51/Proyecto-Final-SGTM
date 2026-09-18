package com.example.myapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.data.repository.MotorcycleRepository
import com.example.myapplication.data.repository.OrderRepository
import com.google.gson.JsonParseException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedOrder: Order? = null,
    val clients: List<Client> = emptyList(),
    val motorcycles: List<Motorcycle> = emptyList(),
    val motorcyclesForSelectedClient: List<Motorcycle> = emptyList(),
    val selectedClientId: String? = null,
    val selectedMotorcycleId: String? = null,
    val statuses: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isLoadingReferences: Boolean = false,
    val referencesLoaded: Boolean = false,
    val isLoadingClientMotorcycles: Boolean = false,
    val isSaving: Boolean = false,
    val updatingOrderId: String? = null,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val detailErrorMessage: String? = null,
    val operationMessage: String? = null,
    val savedOrderId: String? = null,
    val deletedOrderId: String? = null
) {
    val isUpdatingStatus: Boolean
        get() = updatingOrderId != null

    val selectedClient: Client?
        get() = clients.firstOrNull { it.id == selectedClientId }

    val selectedMotorcycle: Motorcycle?
        get() = motorcycles.firstOrNull { it.id == selectedMotorcycleId }

    val selectedClientMotorcycles: List<Motorcycle>
        get() = if (selectedClientId == null) emptyList() else motorcyclesForSelectedClient
}

class OrderViewModel(
    private val orderRepository: OrderRepository = OrderRepository(RetrofitClient.apiService),
    private val clientRepository: ClientRepository = ClientRepository(RetrofitClient.apiService),
    private val motorcycleRepository: MotorcycleRepository = MotorcycleRepository(RetrofitClient.apiService),
    private val testScope: CoroutineScope? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val workScope: CoroutineScope
        get() = testScope ?: viewModelScope

    fun loadOrders() {
        if (_uiState.value.isLoading) return
        workScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, operationMessage = null) }
            try {
                val orders = orderRepository.getOrders()
                _uiState.update {
                    it.copy(
                        orders = orders,
                        statuses = orders.map { order -> order.status }.filter(String::isNotBlank).distinct(),
                        isLoading = false,
                        errorMessage = null
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(exception, "No se pudieron cargar las órdenes"))
                }
            }
        }
    }

    fun refreshOrders() {
        if (_uiState.value.isRefreshing || _uiState.value.isLoading) return
        workScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                val orders = orderRepository.getOrders()
                _uiState.update {
                    it.copy(
                        orders = orders,
                        statuses = orders.map { order -> order.status }.filter(String::isNotBlank).distinct(),
                        isRefreshing = false,
                        errorMessage = null
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = messageFor(exception, "No se pudieron actualizar las órdenes"))
                }
            }
        }
    }

    fun loadReferences() {
        if (_uiState.value.isLoadingReferences || _uiState.value.referencesLoaded) return
        workScope.launch {
            _uiState.update { it.copy(isLoadingReferences = true) }
            try {
                val (clients, motorcycles) = coroutineScope {
                    val clientsDeferred = async { clientRepository.getClients() }
                    val motorcyclesDeferred = async { motorcycleRepository.getMotorcycles() }
                    clientsDeferred.await() to motorcyclesDeferred.await()
                }
                _uiState.update {
                    it.copy(
                        clients = clients,
                        motorcycles = motorcycles,
                        isLoadingReferences = false,
                        referencesLoaded = true
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingReferences = false,
                        errorMessage = messageFor(exception, "No se pudieron cargar clientes y motocicletas")
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).withFilters() }
    }

    fun onStatusFilterChange(status: String?) {
        _uiState.update { it.copy(selectedStatus = status?.takeIf(String::isNotBlank)).withFilters() }
    }

    fun resetOrderDraft() {
        _uiState.update {
            it.copy(
                selectedClientId = null,
                selectedMotorcycleId = null,
                motorcyclesForSelectedClient = emptyList(),
                isLoadingClientMotorcycles = false
            )
        }
    }

    fun selectClient(clientId: String) {
        val state = _uiState.value
        val client = state.clients.firstOrNull { it.id == clientId } ?: return
        val localMotorcycles = state.motorcycles.filter { it.clientId == clientId }
        _uiState.update {
            it.copy(
                selectedClientId = clientId,
                selectedMotorcycleId = localMotorcycles.singleOrNull()?.id,
                motorcyclesForSelectedClient = localMotorcycles,
                isLoadingClientMotorcycles = true,
                operationMessage = null
            )
        }
        workScope.launch {
            try {
                val motorcycles = motorcycleRepository.getMotorcyclesForClient(clientId)
                _uiState.update { currentState ->
                    if (currentState.selectedClientId != clientId) {
                        currentState
                    } else {
                        currentState.copy(
                            motorcycles = (currentState.motorcycles + motorcycles).distinctBy(Motorcycle::id),
                            motorcyclesForSelectedClient = motorcycles,
                            selectedMotorcycleId = motorcycles.singleOrNull()?.id,
                            isLoadingClientMotorcycles = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    if (currentState.selectedClientId == clientId) {
                        currentState.copy(
                            isLoadingClientMotorcycles = false,
                            operationMessage = messageFor(exception, "No se pudieron cargar las motocicletas del cliente")
                        )
                    } else {
                        currentState
                    }
                }
            }
        }
    }

    fun selectMotorcycle(motorcycleId: String) {
        val state = _uiState.value
        val motorcycle = state.motorcycles.firstOrNull {
            it.id == motorcycleId && it.clientId == state.selectedClientId
        } ?: return
        _uiState.update { it.copy(selectedMotorcycleId = motorcycle.id) }
    }

    fun selectOrder(id: String) {
        val order = _uiState.value.orders.firstOrNull { it.id == id }
        if (order != null) _uiState.update { it.copy(selectedOrder = order, detailErrorMessage = null) }
    }

    fun loadOrder(id: String) {
        if (id.isBlank() || _uiState.value.isLoadingDetail) return
        workScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, detailErrorMessage = null) }
            try {
                val order = orderRepository.getOrder(id)
                _uiState.update { it.copy(selectedOrder = order, isLoadingDetail = false) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isLoadingDetail = false, detailErrorMessage = messageFor(exception, "No se pudo cargar el detalle de la orden"))
                }
            }
        }
    }

    fun createOrder(
        clientId: String,
        motorcycleId: String,
        description: String,
        status: String,
        totalText: String
    ) {
        if (_uiState.value.isSaving) return
        val state = _uiState.value
        if (state.clients.isNotEmpty() && state.clients.none { it.id == clientId }) {
            _uiState.update { it.copy(operationMessage = "Selecciona un cliente válido") }
            return
        }
        if (state.motorcycles.isNotEmpty()) {
            val motorcycle = state.motorcycles.firstOrNull { it.id == motorcycleId }
            if (motorcycle == null || motorcycle.clientId != clientId) {
                _uiState.update { it.copy(operationMessage = "La motocicleta no pertenece al cliente seleccionado") }
                return
            }
        }
        val validationError = OrderValidator.validate(clientId, motorcycleId, description, status, totalText)
        if (validationError != null) {
            _uiState.update { it.copy(operationMessage = validationError) }
            return
        }
        workScope.launch {
            _uiState.update { it.copy(isSaving = true, operationMessage = null, errorMessage = null) }
            try {
                val created = orderRepository.createOrder(
                    Order(
                        clientId = clientId,
                        motorcycleId = motorcycleId,
                        description = description.trim(),
                        status = status.trim(),
                        total = totalText.toDouble()
                    )
                )
                _uiState.update {
                    it.copy(
                        orders = (it.orders + created).distinctBy(Order::id),
                        selectedOrder = created,
                        isSaving = false,
                        operationMessage = "Orden creada correctamente",
                        savedOrderId = created.id
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, operationMessage = messageFor(exception, "No se pudo crear la orden"))
                }
            }
        }
    }

    fun updateOrder(order: Order) {
        if (_uiState.value.isSaving) return
        val currentOrder = _uiState.value.orders.firstOrNull { it.id == order.id }
            ?: _uiState.value.selectedOrder?.takeIf { it.id == order.id }
        val orderToUpdate = order.copy(status = currentOrder?.status ?: order.status)
        val validationError = OrderValidator.validate(
            orderToUpdate.clientId,
            orderToUpdate.motorcycleId,
            orderToUpdate.description,
            orderToUpdate.status,
            orderToUpdate.total.toString()
        )
        if (validationError != null) {
            _uiState.update { it.copy(operationMessage = validationError) }
            return
        }
        workScope.launch {
            _uiState.update { it.copy(isSaving = true, operationMessage = null) }
            try {
                val updated = orderRepository.updateOrder(orderToUpdate.id.orEmpty(), orderToUpdate)
                _uiState.update {
                    it.copy(
                        orders = it.orders.map { current -> if (current.id == updated.id) updated else current },
                        selectedOrder = updated,
                        isSaving = false,
                        operationMessage = "Orden actualizada correctamente",
                        savedOrderId = updated.id
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isSaving = false, operationMessage = messageFor(exception, "No se pudo actualizar la orden")) }
            }
        }
    }

    fun changeStatus(status: String) {
        val order = _uiState.value.selectedOrder ?: return
        updateOrderStatus(order.id.orEmpty(), status)
    }

    fun updateOrderStatus(orderId: String, status: String) {
        val currentOrder = _uiState.value.orders.firstOrNull { it.id == orderId }
            ?: _uiState.value.selectedOrder?.takeIf { it.id == orderId }
            ?: return
        if (orderId.isBlank() || status.isBlank() || status == currentOrder.status || _uiState.value.isUpdatingStatus) return

        workScope.launch {
            _uiState.update {
                it.copy(
                    updatingOrderId = orderId,
                    operationMessage = null,
                    errorMessage = null
                )
            }
            try {
                val updatedOrder = orderRepository.updateOrder(
                    orderId,
                    currentOrder.copy(status = status)
                )
                val updatedMotorcycle = syncMotorcycleStatus(
                    updatedOrder.motorcycleId.ifBlank { currentOrder.motorcycleId },
                    status
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        orders = currentState.orders.map { order ->
                            if (order.id == updatedOrder.id) updatedOrder else order
                        },
                        selectedOrder = if (currentState.selectedOrder?.id == updatedOrder.id) {
                            updatedOrder
                        } else {
                            currentState.selectedOrder
                        },
                        motorcycles = updatedMotorcycle?.let { motorcycle ->
                            currentState.motorcycles.map { current ->
                                if (current.id == motorcycle.id) motorcycle else current
                            }
                        } ?: currentState.motorcycles,
                        updatingOrderId = null,
                        operationMessage = "Estado actualizado correctamente"
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        updatingOrderId = null,
                        operationMessage = messageFor(exception, "No se pudo actualizar el estado")
                    )
                }
            }
        }
    }

    fun deleteOrder(id: String) {
        if (_uiState.value.isDeleting) return
        workScope.launch {
            _uiState.update { it.copy(isDeleting = true, operationMessage = null) }
            try {
                orderRepository.deleteOrder(id)
                _uiState.update {
                    it.copy(
                        orders = it.orders.filterNot { order -> order.id == id },
                        selectedOrder = null,
                        isDeleting = false,
                        operationMessage = "Orden eliminada correctamente",
                        deletedOrderId = id
                    ).withFilters()
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isDeleting = false, operationMessage = messageFor(exception, "No se pudo eliminar la orden")) }
            }
        }
    }

    fun consumeOperationEvent() {
        _uiState.update { it.copy(operationMessage = null, savedOrderId = null, deletedOrderId = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(operationMessage = null, errorMessage = null, detailErrorMessage = null) }
    }

    private fun OrderUiState.withFilters(): OrderUiState {
        val normalizedQuery = searchQuery.trim().lowercase(Locale.ROOT)
        return copy(
            filteredOrders = orders.filter { order ->
                val client = clients.firstOrNull { reference -> reference.id == order.clientId }
                val motorcycle = motorcycles.firstOrNull { reference -> reference.id == order.motorcycleId }
                val matchesQuery = normalizedQuery.isBlank() || listOf(
                    order.id.orEmpty(),
                    order.clientId,
                    order.motorcycleId,
                    order.description,
                    order.status,
                    client?.name.orEmpty(),
                    client?.email.orEmpty(),
                    client?.phone.orEmpty(),
                    motorcycle?.brand.orEmpty(),
                    motorcycle?.model.orEmpty(),
                    motorcycle?.plate.orEmpty(),
                    motorcycle?.year?.toString().orEmpty()
                ).any { value -> value.lowercase(Locale.ROOT).contains(normalizedQuery) }
                val matchesStatus = selectedStatus == null || order.status == selectedStatus
                matchesQuery && matchesStatus
            }
        )
    }

    private suspend fun syncMotorcycleStatus(motorcycleId: String, status: String): Motorcycle? {
        if (motorcycleId.isBlank()) return null
        val motorcycle = _uiState.value.motorcycles.firstOrNull { it.id == motorcycleId }
            ?: motorcycleRepository.getMotorcycle(motorcycleId)
        val id = motorcycle.id?.takeIf(String::isNotBlank) ?: motorcycleId
        return motorcycleRepository.updateMotorcycle(id, motorcycle.copy(status = status))
    }

    private fun messageFor(exception: Exception, fallback: String): String = when (exception) {
        is SocketTimeoutException -> {
            logException(exception)
            "El servidor tardó demasiado en responder"
        }
        is IOException -> {
            logException(exception)
            "No hay conexión con el servidor"
        }
        is JsonParseException -> {
            logException(exception)
            "No se pudo interpretar la respuesta del servidor"
        }
        is HttpException -> when (exception.code()) {
            400 -> {
                logException(exception)
                "Los datos enviados no son válidos"
            }
            401 -> {
                logException(exception)
                "Tu sesión expiró. Inicia sesión nuevamente."
            }
            403 -> {
                logException(exception)
                "No tienes permisos para consultar órdenes"
            }
            404 -> {
                logException(exception)
                "No se encontraron las órdenes solicitadas"
            }
            409 -> {
                logException(exception)
                "La operación entra en conflicto con el estado actual"
            }
            in 500..599 -> {
                logException(exception)
                "El servidor presentó un error"
            }
            else -> {
                logException(exception)
                fallback
            }
        }
        else -> {
            logException(exception)
            fallback
        }
    }

    private fun logException(exception: Exception) {
        if (!BuildConfig.DEBUG) return

        when (exception) {
            is HttpException -> {
                val safeBody = runCatching { exception.response()?.errorBody()?.string() }
                    .getOrNull()
                    ?.let(::safeErrorMessage)
                runCatching {
                    Log.e(
                        "OrderViewModel",
                        "HTTP ${exception.code()} ${exception.message()}" +
                            (safeBody?.let { " | error=$it" } ?: "")
                    )
                }
            }
            else -> runCatching {
                Log.e(
                    "OrderViewModel",
                    "${exception::class.simpleName}: ${exception.message}"
                )
            }
        }
    }

    private fun safeErrorMessage(body: String): String {
        val message = Regex("(?i)\\\"message\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"")
            .find(body)?.groupValues?.getOrNull(1)
        return message?.let { "message=${it.take(1_000)}" } ?: "body_present=true"
    }
}

object OrderValidator {
    fun validate(
        clientId: String,
        motorcycleId: String,
        description: String,
        status: String,
        totalText: String
    ): String? {
        if (clientId.isBlank()) return "Selecciona un cliente"
        if (motorcycleId.isBlank()) return "Selecciona una motocicleta"
        if (description.isBlank()) return "La descripción es obligatoria"
        if (description.length > 2000) return "La descripción no puede superar 2000 caracteres"
        if (status.isBlank()) return "El estado es obligatorio"
        val total = totalText.toDoubleOrNull() ?: return "El total no es válido"
        if (!total.isFinite() || total < 0) return "El total no puede ser negativo"
        return null
    }
}
