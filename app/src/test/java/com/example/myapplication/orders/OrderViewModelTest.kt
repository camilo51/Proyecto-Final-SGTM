package com.example.myapplication.orders

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.data.repository.MotorcycleRepository
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.ui.viewmodel.OrderViewModel
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderViewModelTest {
    @Test
    fun loadOrders_success_updatesStateAndRelations() {
        val source = FakeOrderRepository(listOf(order("1", "Pendiente")))
        val viewModel = viewModel(source)

        viewModel.loadOrders()

        assertEquals(1, viewModel.uiState.value.orders.size)
        assertEquals("1", viewModel.uiState.value.orders.single().id)
        assertEquals("Pendiente", viewModel.uiState.value.statuses.single())
        assertTrue(viewModel.uiState.value.errorMessage == null)
    }

    @Test
    fun loadOrders_failure_exposesFriendlyError() {
        val viewModel = viewModel(FakeOrderRepository(failure = IOException()))

        viewModel.loadOrders()

        assertEquals("No hay conexión con el servidor", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.orders.isEmpty())
    }

    @Test
    fun searchAndStatusFilter_areAppliedLocally() {
        val source = FakeOrderRepository(
            listOf(
                order("1", "Pendiente", description = "Cambio de aceite"),
                order("2", "Entregada", description = "Freno trasero").copy(motorcycleId = "21")
            )
        )
        val viewModel = viewModel(source)
        viewModel.loadOrders()
        viewModel.loadReferences()

        viewModel.onSearchQueryChange("freno")
        assertEquals(listOf("2"), viewModel.uiState.value.filteredOrders.map { it.id })

        viewModel.onSearchQueryChange("ABC123")
        assertEquals(listOf("1"), viewModel.uiState.value.filteredOrders.map { it.id })

        viewModel.onSearchQueryChange("")
        viewModel.onStatusFilterChange("Pendiente")
        assertEquals(listOf("1"), viewModel.uiState.value.filteredOrders.map { it.id })
    }

    @Test
    fun createOrder_successDisablesDuplicatePathAndUpdatesList() {
        val source = FakeOrderRepository(created = order("8", "Pendiente"))
        val viewModel = viewModel(source)

        viewModel.createOrder("10", "20", "Revisión general", "Pendiente", "45000")

        assertEquals(1, source.createCalls)
        assertEquals("8", viewModel.uiState.value.savedOrderId)
        assertEquals("8", viewModel.uiState.value.orders.single().id)
    }

    @Test
    fun invalidCreate_isRejectedBeforeRepositoryCall() {
        val source = FakeOrderRepository()
        val viewModel = viewModel(source)

        viewModel.createOrder("", "20", "Descripción", "Pendiente", "100")

        assertEquals(0, source.createCalls)
        assertEquals("Selecciona un cliente", viewModel.uiState.value.operationMessage)
    }

    private fun viewModel(source: FakeOrderRepository): OrderViewModel = OrderViewModel(
        orderRepository = source,
        clientRepository = FakeClientRepository,
        motorcycleRepository = FakeMotorcycleRepository,
        testScope = CoroutineScope(Dispatchers.Unconfined)
    )

    private fun order(id: String, status: String, description: String = "Revisión") = Order(
        id = id,
        clientId = "10",
        motorcycleId = "20",
        description = description,
        status = status,
        total = 45000.0
    )
}

private class FakeOrderRepository(
    private val values: List<Order> = emptyList(),
    private val failure: Exception? = null,
    private val created: Order? = null
) : OrderRepository(noOpApiService) {
    var createCalls = 0

    override suspend fun getOrders(): List<Order> {
        failure?.let { throw it }
        return values
    }

    override suspend fun getOrder(id: String): Order = values.first { it.id == id }

    override suspend fun createOrder(order: Order): Order {
        createCalls++
        return created ?: order.copy(id = "generated")
    }

    override suspend fun updateOrder(id: String, order: Order): Order = order.copy(id = id)

    override suspend fun deleteOrder(id: String) = Unit
}

private object FakeClientRepository : ClientRepository(noOpApiService) {
    override suspend fun getClients(): List<Client> = listOf(
        Client(id = "10", name = "Cliente de prueba", email = "test@example.com", phone = "3000000000")
    )
}

private object FakeMotorcycleRepository : MotorcycleRepository(noOpApiService) {
    override suspend fun getMotorcycles(): List<Motorcycle> = listOf(
        Motorcycle(id = "20", brand = "Honda", model = "CB", year = 2024, plate = "ABC123", clientId = "10")
    )
}

private val noOpApiService: ApiService = Proxy.newProxyInstance(
    ApiService::class.java.classLoader,
    arrayOf(ApiService::class.java)
) { _, method, _ ->
    error("Llamada inesperada a ApiService: ${method.name}")
} as ApiService
