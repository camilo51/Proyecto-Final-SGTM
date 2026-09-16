package com.example.myapplication.motorcycles

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.data.repository.MotorcycleRepository
import com.example.myapplication.ui.viewmodel.MotorcycleViewModel
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class MotorcycleViewModelTest {

    @Test
    fun loadMotorcycles_success_updatesState() {
        val viewModel = viewModel(
            motorcycles = listOf(motorcycle("1", "ABC12D", "Honda", "En servicio")),
            clients = listOf(client("10", "Ana Gómez"))
        )

        viewModel.loadMotorcycles()

        assertEquals(1, viewModel.uiState.value.motorcycles.size)
        assertEquals("ABC12D", viewModel.uiState.value.motorcycles.single().plate)
        assertEquals("Ana Gómez", viewModel.uiState.value.clients.single().name)
        assertTrue(viewModel.uiState.value.errorMessage == null)
    }

    @Test
    fun loadMotorcycles_failure_exposesFriendlyError() {
        val viewModel = viewModel(failure = IOException())

        viewModel.loadMotorcycles()

        assertEquals("No hay conexión con el servidor.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun search_isAppliedLocallyToPlateBrandModelAndOwner() {
        val viewModel = viewModel(
            motorcycles = listOf(
                motorcycle("1", "ABC12D", "Honda", "En servicio", model = "CB190R", clientId = "10"),
                motorcycle("2", "XYZ34F", "Yamaha", "Entregada", model = "FZ", clientId = "11")
            ),
            clients = listOf(client("10", "Ana Gómez"), client("11", "Carlos Pérez"))
        )
        viewModel.loadMotorcycles()

        viewModel.onSearchQueryChange("ana")
        assertEquals(listOf("1"), viewModel.uiState.value.filteredMotorcycles.map { it.id })
        viewModel.onSearchQueryChange("yamaha fz")
        assertEquals(listOf("2"), viewModel.uiState.value.filteredMotorcycles.map { it.id })
    }

    @Test
    fun statusFilter_isAppliedLocally() {
        val viewModel = viewModel(
            motorcycles = listOf(
                motorcycle("1", "ABC12D", "Honda", "En servicio"),
                motorcycle("2", "XYZ34F", "Yamaha", "En reparación")
            )
        )
        viewModel.loadMotorcycles()

        viewModel.onStatusFilterChange("En reparación")

        assertEquals(listOf("2"), viewModel.uiState.value.filteredMotorcycles.map { it.id })
    }

    @Test
    fun createMotorcycle_allowsOptionalOwner() {
        val created = motorcycle("3", "NEW12A", "Suzuki", "En servicio")
        val repository = FakeMotorcycleRepository(values = listOf(created), created = created)
        val viewModel = viewModel(repository = repository)

        viewModel.createMotorcycle(
            plate = "NEW12A",
            brand = "Suzuki",
            model = "GN",
            yearText = "2024",
            color = "Negro",
            engineCcText = "150",
            clientId = null,
            status = "En servicio",
            notes = ""
        )

        assertEquals(1, repository.createCalls)
        assertNull(repository.lastCreated?.clientId)
        assertEquals("Motocicleta creada correctamente", viewModel.uiState.value.operationMessage)
        assertEquals("3", viewModel.uiState.value.savedMotorcycleId)
    }

    @Test
    fun updateMotorcycle_refreshesListAndUpdatesDetail() {
        val updated = motorcycle("4", "ABC12D", "Honda", "Lista para entrega", model = "CB190R")
        val repository = FakeMotorcycleRepository(values = listOf(updated), updated = updated)
        val viewModel = viewModel(repository = repository)
        viewModel.loadMotorcycles()

        viewModel.updateMotorcycle(updated)

        assertEquals(1, repository.updateCalls)
        assertEquals("Lista para entrega", viewModel.uiState.value.selectedMotorcycle?.status)
        assertEquals("Motocicleta actualizada correctamente", viewModel.uiState.value.operationMessage)
    }

    @Test
    fun duplicatePlate_showsFriendlyConflict() {
        val conflict = HttpException(
            Response.error<Any>(409, "{\"message\":\"plate already exists\"}".toResponseBody())
        )
        val repository = FakeMotorcycleRepository(failure = conflict)
        val viewModel = viewModel(repository = repository)

        viewModel.updateMotorcycle(motorcycle("5", "ABC12D", "Honda", "En servicio"))

        assertEquals("Ya existe una motocicleta con esta placa.", viewModel.uiState.value.operationMessage)
    }

    private fun viewModel(
        repository: FakeMotorcycleRepository = FakeMotorcycleRepository(),
        motorcycles: List<Motorcycle> = emptyList(),
        clients: List<Client> = emptyList(),
        failure: Exception? = null
    ): MotorcycleViewModel {
        if (motorcycles.isNotEmpty()) repository.values = motorcycles
        if (failure != null) repository.failure = failure
        return MotorcycleViewModel(
            motorcycleRepository = repository,
            clientRepository = FakeClientRepository(clients),
            testScope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    private fun motorcycle(
        id: String,
        plate: String,
        brand: String,
        status: String,
        model: String = "Modelo",
        clientId: String? = null
    ) = Motorcycle(
        id = id,
        plate = plate,
        brand = brand,
        model = model,
        year = 2024,
        status = status,
        clientId = clientId
    )

    private fun client(id: String, name: String) = Client(id = id, name = name, phone = "3000000000")
}

private class FakeMotorcycleRepository(
    var values: List<Motorcycle> = emptyList(),
    var failure: Exception? = null,
    private val created: Motorcycle? = null,
    private val updated: Motorcycle? = null
) : MotorcycleRepository(noOpApiService) {
    var createCalls = 0
    var updateCalls = 0
    var lastCreated: Motorcycle? = null

    override suspend fun getMotorcycles(): List<Motorcycle> {
        failure?.let { throw it }
        return values
    }

    override suspend fun getMotorcycle(id: String): Motorcycle = values.first { it.id == id }

    override suspend fun createMotorcycle(motorcycle: Motorcycle): Motorcycle {
        createCalls++
        lastCreated = motorcycle
        return created ?: motorcycle.copy(id = "generated")
    }

    override suspend fun updateMotorcycle(id: String, motorcycle: Motorcycle): Motorcycle {
        updateCalls++
        failure?.let { throw it }
        return updated ?: motorcycle.copy(id = id)
    }

}

private class FakeClientRepository(
    private val values: List<Client>
) : ClientRepository(noOpApiService) {
    override suspend fun getClients(): List<Client> = values
}

private val noOpApiService: ApiService = Proxy.newProxyInstance(
    ApiService::class.java.classLoader,
    arrayOf(ApiService::class.java)
) { _, method, _ ->
    error("Llamada inesperada a ApiService: ${method.name}")
} as ApiService
