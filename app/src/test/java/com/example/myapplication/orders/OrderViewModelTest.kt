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
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

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

    @Test
    fun updateOrderStatus_updatesOrderAndRelatedMotorcycle() {
        val currentOrder = order("1", "En servicio")
        val source = FakeOrderRepository(
            values = listOf(currentOrder),
            updated = currentOrder.copy(status = "En reparación")
        )
        val motorcycle = Motorcycle(
            id = "20",
            brand = "Honda",
            model = "CB",
            year = 2024,
            plate = "ABC123",
            clientId = "10",
            status = "En servicio"
        )
        val motorcycleRepository = FakeMotorcycleRepository(
            values = listOf(motorcycle),
            updated = motorcycle.copy(status = "En reparación")
        )
        val viewModel = viewModel(source, motorcycleRepository)

        viewModel.loadOrders()
        viewModel.loadReferences()
        viewModel.selectOrder("1")
        viewModel.updateOrderStatus("1", "En reparación")

        assertEquals("En reparación", viewModel.uiState.value.orders.single().status)
        assertEquals("En reparación", viewModel.uiState.value.selectedOrder?.status)
        assertEquals("En reparación", viewModel.uiState.value.motorcycles.single().status)
        assertEquals(1, motorcycleRepository.updateCalls)
        assertTrue(!viewModel.uiState.value.isUpdatingStatus)
    }

    @Test
    fun updateOrderStatus_failure_keepsPreviousStatus() {
        val currentOrder = order("1", "En servicio")
        val source = FakeOrderRepository(
            values = listOf(currentOrder),
            updateFailure = HttpException(
                Response.error<Any>(409, "conflict".toResponseBody())
            )
        )
        val viewModel = viewModel(source)

        viewModel.loadOrders()
        viewModel.selectOrder("1")
        viewModel.updateOrderStatus("1", "En reparación")

        assertEquals("En servicio", viewModel.uiState.value.orders.single().status)
        assertEquals("La operación entra en conflicto con el estado actual", viewModel.uiState.value.operationMessage)
        assertTrue(!viewModel.uiState.value.isUpdatingStatus)
    }

    @Test
    fun selectClient_withOneOwnedMotorcycle_selectsItAutomatically() {
        val motorcycle = Motorcycle(id = "20", clientId = "10", plate = "ABC123")
        val viewModel = viewModel(
            source = FakeOrderRepository(),
            motorcycleRepository = FakeMotorcycleRepository(values = listOf(motorcycle))
        )

        viewModel.loadReferences()
        viewModel.selectClient("10")

        assertEquals("10", viewModel.uiState.value.selectedClientId)
        assertEquals("20", viewModel.uiState.value.selectedMotorcycleId)
        assertEquals(listOf("20"), viewModel.uiState.value.selectedClientMotorcycles.map { it.id })
    }

    @Test
    fun selectClient_withSeveralOwnedMotorcycles_waitsForSelection() {
        val motorcycles = listOf(
            Motorcycle(id = "20", clientId = "10", plate = "ABC123"),
            Motorcycle(id = "21", clientId = "10", plate = "XYZ456"),
            Motorcycle(id = "22", clientId = "99", plate = "OTHER1")
        )
        val viewModel = viewModel(
            source = FakeOrderRepository(),
            motorcycleRepository = FakeMotorcycleRepository(values = motorcycles)
        )

        viewModel.loadReferences()
        viewModel.selectClient("10")

        assertEquals(null, viewModel.uiState.value.selectedMotorcycleId)
        assertEquals(listOf("20", "21"), viewModel.uiState.value.selectedClientMotorcycles.map { it.id })
        viewModel.selectMotorcycle("22")
        assertEquals(null, viewModel.uiState.value.selectedMotorcycleId)
        viewModel.selectMotorcycle("21")
        assertEquals("21", viewModel.uiState.value.selectedMotorcycleId)
    }

    @Test
    fun selectClient_refreshesOwnedMotorcyclesFromFilteredEndpoint() {
        val motorcycle = Motorcycle(id = "30", clientId = "10", plate = "NEW123")
        val viewModel = viewModel(
            source = FakeOrderRepository(),
            motorcycleRepository = FakeMotorcycleRepository(
                values = emptyList(),
                clientValues = listOf(motorcycle)
            )
        )

        viewModel.loadReferences()
        viewModel.selectClient("10")

        assertEquals(listOf("30"), viewModel.uiState.value.selectedClientMotorcycles.map { it.id })
        assertEquals("30", viewModel.uiState.value.selectedMotorcycleId)
    }

    @Test
    fun createOrder_rejectsMotorcycleFromAnotherClient() {
        val source = FakeOrderRepository()
        val viewModel = viewModel(
            source = source,
            motorcycleRepository = FakeMotorcycleRepository(
                values = listOf(Motorcycle(id = "20", clientId = "99"))
            )
        )
        viewModel.loadReferences()

        viewModel.createOrder("10", "20", "Revisión", "En servicio", "45000")

        assertEquals(0, source.createCalls)
        assertEquals("La motocicleta no pertenece al cliente seleccionado", viewModel.uiState.value.operationMessage)
    }

    private fun viewModel(
        source: FakeOrderRepository,
        motorcycleRepository: FakeMotorcycleRepository = FakeMotorcycleRepository(),
        clientRepository: ClientRepository = FakeClientRepository
    ): OrderViewModel = OrderViewModel(
        orderRepository = source,
        clientRepository = clientRepository,
        motorcycleRepository = motorcycleRepository,
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
    private val created: Order? = null,
    private val updated: Order? = null,
    private val updateFailure: Exception? = null
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

    override suspend fun updateOrder(id: String, order: Order): Order {
        updateFailure?.let { throw it }
        return updated ?: order.copy(id = id)
    }

    override suspend fun deleteOrder(id: String) = Unit
}

private object FakeClientRepository : ClientRepository(noOpApiService) {
    override suspend fun getClients(): List<Client> = listOf(
        Client(id = "10", name = "Cliente de prueba", email = "test@example.com", phone = "3000000000")
    )
}

private class FakeMotorcycleRepository(
    private val values: List<Motorcycle> = listOf(
        Motorcycle(id = "20", brand = "Honda", model = "CB", year = 2024, plate = "ABC123", clientId = "10")
    ),
    private val clientValues: List<Motorcycle>? = null,
    private val updated: Motorcycle? = null
) : MotorcycleRepository(noOpApiService) {
    var updateCalls = 0

    override suspend fun getMotorcycles(): List<Motorcycle> = values

    override suspend fun getMotorcyclesForClient(clientId: String): List<Motorcycle> =
        (clientValues ?: values).filter { it.clientId == clientId }

    override suspend fun getMotorcycle(id: String): Motorcycle = values.first { it.id == id }

    override suspend fun updateMotorcycle(id: String, motorcycle: Motorcycle): Motorcycle {
        updateCalls++
        return updated ?: motorcycle.copy(id = id)
    }
}

private val noOpApiService: ApiService = Proxy.newProxyInstance(
    ApiService::class.java.classLoader,
    arrayOf(ApiService::class.java)
) { _, method, _ ->
    error("Llamada inesperada a ApiService: ${method.name}")
} as ApiService
