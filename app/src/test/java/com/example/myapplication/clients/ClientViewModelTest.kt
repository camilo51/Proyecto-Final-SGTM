package com.example.myapplication.clients

import com.example.myapplication.data.api.ApiResponse
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.repository.ClientDataSource
import com.example.myapplication.ui.viewmodel.ClientViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ClientViewModelTest {

    @Test
    fun loadClients_success_updatesState() {
        val repository = FakeClientRepository(
            values = listOf(client("1", "Ana Gómez"), client("2", "Carlos Pérez"))
        )
        val viewModel = viewModel(repository)

        viewModel.loadClients()

        assertEquals(2, viewModel.uiState.value.clients.size)
        assertEquals(2, viewModel.uiState.value.filteredClients.size)
        assertTrue(viewModel.uiState.value.errorMessage == null)
        assertEquals(1, repository.calls)
    }

    @Test
    fun pagination_limitsVisibleClientsAndChangesPage() {
        val repository = FakeClientRepository(
            values = (1..25).map { id -> client(id.toString(), "Cliente $id") }
        )
        val viewModel = viewModel(repository)

        viewModel.loadClients()

        assertEquals(25, viewModel.uiState.value.filteredClients.size)
        assertEquals(10, viewModel.uiState.value.visibleClients.size)
        assertEquals(3, viewModel.uiState.value.totalPages)
        assertEquals("1", viewModel.uiState.value.visibleClients.first().id)

        viewModel.onPageChange(2)

        assertEquals(2, viewModel.uiState.value.currentPage)
        assertEquals("11", viewModel.uiState.value.visibleClients.first().id)
        assertEquals("20", viewModel.uiState.value.visibleClients.last().id)

        viewModel.onPageChange(3)

        assertEquals(5, viewModel.uiState.value.visibleClients.size)
        assertEquals("21", viewModel.uiState.value.visibleClients.first().id)
    }

    @Test
    fun loadClients_failure_exposesFriendlyError() {
        val viewModel = viewModel(FakeClientRepository(failure = IOException()))

        viewModel.loadClients()

        assertEquals("No hay conexión con el servidor", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.clients.isEmpty())
    }

    @Test
    fun search_isAppliedLocallyToRealClientFields() {
        val repository = FakeClientRepository(
            values = listOf(
                client("1", "Ana Gómez", phone = "3001112233"),
                client("2", "Carlos Pérez", email = "carlos@example.com")
            )
        )
        val viewModel = viewModel(repository)
        viewModel.loadClients()

        viewModel.onSearchQueryChange("carlos@example.com")

        assertEquals(listOf("2"), viewModel.uiState.value.filteredClients.map { it.id })
        assertEquals(1, repository.calls)
    }

    @Test
    fun searchWithoutMatches_canBeCleared() {
        val viewModel = viewModel(
            FakeClientRepository(values = listOf(client("1", "Ana Gómez")))
        )
        viewModel.loadClients()

        viewModel.onSearchQueryChange("no existe")
        assertTrue(viewModel.uiState.value.filteredClients.isEmpty())

        viewModel.clearSearch()

        assertEquals(1, viewModel.uiState.value.filteredClients.size)
    }

    @Test
    fun createClient_success_addsClientWithoutLocalRequiredFields() {
        val created = client("3", "Cliente completo")
        val repository = FakeClientRepository(values = listOf(created), created = created)
        val viewModel = viewModel(repository)

        viewModel.createClient("", "", "", "")

        assertEquals(1, repository.createCalls)
        assertEquals("Cliente creado correctamente", viewModel.uiState.value.operationMessage)
        assertEquals(1, viewModel.uiState.value.creationVersion)
        assertEquals("3", viewModel.uiState.value.clients.single().id)
        assertTrue(repository.lastCreated?.name?.isEmpty() == true)
    }

    @Test
    fun createClient_preservesCedulaInRepositoryPayload() {
        val repository = FakeClientRepository(
            values = listOf(client("11", "Cliente con cédula")),
            created = client("11", "Cliente con cédula")
        )
        val viewModel = viewModel(repository)

        viewModel.createClient("Cliente con cédula", "123456789", "3000000000", "")

        assertEquals("123456789", repository.lastCreated?.cedula)
    }

    @Test
    fun createClient_invalidEmail_doesNotCallRepository() {
        val repository = FakeClientRepository()
        val viewModel = viewModel(repository)

        viewModel.createClient("Cliente", "", "3000000000", "correo-invalido")

        assertEquals("Ingresa un correo válido o déjalo vacío", viewModel.uiState.value.operationMessage)
        assertEquals(0, repository.createCalls)
    }

    @Test
    fun createClient_refreshesListInsteadOfAddingIncompletePostResponse() {
        val completeClient = client("4", "Cliente completo")
        val repository = FakeClientRepository(
            values = listOf(completeClient),
            created = Client()
        )
        val viewModel = viewModel(repository)

        viewModel.createClient("Nombre enviado", "123", "3000000000", "cliente@example.com")

        assertEquals("Cliente completo", viewModel.uiState.value.clients.single().name)
        assertEquals(1, repository.calls)
    }

    @Test
    fun apiResponse_deserializesWrappedClientList() {
        val json = """
            {
              "success": true,
              "message": "OK",
              "data": [
                {"id": "1", "name": "Ana Gómez", "document": "123456789", "email": "ana@example.com", "phone": "3000000000"}
              ]
            }
        """.trimIndent()
        val type = object : TypeToken<ApiResponse<List<Client>>>() {}.type

        val response = Gson().fromJson<ApiResponse<List<Client>>>(json, type)

        assertTrue(response.success)
        assertEquals("Ana Gómez", response.data?.single()?.name)
        assertEquals("123456789", response.data?.single()?.cedula)
    }

    @Test
    fun clientPayload_mapsCedulaToBackendDocumentField() {
        val json = Gson().toJson(
            Client(
                name = "Ana Gómez",
                cedula = "123456789",
                email = "ana@example.com",
                phone = "3000000000"
            )
        )

        assertTrue(json.contains("\"document\":\"123456789\""))
        assertTrue(json.contains("\"email\":\"ana@example.com\""))
        assertFalse(json.contains("\"cedula\""))
    }

    @Test
    fun loadClients_unauthorized_exposesSessionMessage() {
        val httpException = HttpException(
            Response.error<Any>(401, "unauthorized".toResponseBody())
        )
        val viewModel = viewModel(FakeClientRepository(failure = httpException))

        viewModel.loadClients()

        assertEquals(
            "Tu sesión expiró. Inicia sesión nuevamente.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun loadClients_malformedJson_exposesParsingMessage() {
        val viewModel = viewModel(
            FakeClientRepository(failure = com.google.gson.JsonParseException("invalid json"))
        )

        viewModel.loadClients()

        assertEquals(
            "No se pudo interpretar la respuesta del servidor",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun createClient_refreshFailure_keepsExistingListAndExplainsPartialSuccess() {
        val existingClient = client("5", "Cliente existente")
        val repository = FakeClientRepository(
            values = listOf(existingClient),
            created = client("6", "Respuesta parcial"),
            refreshFailure = IOException()
        )
        val viewModel = viewModel(repository)
        viewModel.loadClients()

        viewModel.createClient("Nuevo", "123", "3000000000", "nuevo@example.com")

        assertEquals(
            "Cliente creado correctamente, pero no se pudo actualizar la lista.",
            viewModel.uiState.value.operationMessage
        )
        assertEquals(listOf("5"), viewModel.uiState.value.clients.map { it.id })
    }

    @Test
    fun updateClient_success_refreshesClientsAndReportsSuccess() {
        val updatedClient = client("7", "Ana actualizada", phone = "3112223344")
        val repository = FakeClientRepository(
            values = listOf(updatedClient),
            updated = updatedClient
        )
        val viewModel = viewModel(repository)
        viewModel.loadClients()

        viewModel.updateClient(updatedClient)

        assertEquals(1, repository.updateCalls)
        assertEquals("Cliente actualizado correctamente", viewModel.uiState.value.operationMessage)
        assertEquals(1, viewModel.uiState.value.updateVersion)
        assertEquals("Ana actualizada", viewModel.uiState.value.clients.single().name)
        assertEquals("3112223344", viewModel.uiState.value.clients.single().phone)
    }

    @Test
    fun updateClient_invalidEmail_doesNotCallRepository() {
        val repository = FakeClientRepository()
        val viewModel = viewModel(repository)

        viewModel.updateClient(client("8", "Cliente", email = "correo-invalido"))

        assertEquals("Ingresa un correo válido o déjalo vacío", viewModel.uiState.value.operationMessage)
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun updateClient_duplicateDocument_showsFriendlyConflict() {
        val repository = FakeClientRepository(
            failure = HttpException(Response.error<Any>(409, "conflict".toResponseBody()))
        )
        val viewModel = viewModel(repository)

        viewModel.updateClient(client("9", "Cliente", email = "cliente@example.com"))

        assertEquals(
            "Ya existe un cliente con este número de documento.",
            viewModel.uiState.value.operationMessage
        )
        assertEquals(1, repository.updateCalls)
    }

    @Test
    fun updateClient_unauthorized_showsSessionMessage() {
        val repository = FakeClientRepository(
            failure = HttpException(Response.error<Any>(401, "unauthorized".toResponseBody()))
        )
        val viewModel = viewModel(repository)

        viewModel.updateClient(client("10", "Cliente"))

        assertEquals(
            "Tu sesión expiró. Inicia sesión nuevamente.",
            viewModel.uiState.value.operationMessage
        )
    }

    private fun viewModel(repository: ClientDataSource): ClientViewModel = ClientViewModel(
        clientRepository = repository,
        testScope = CoroutineScope(Dispatchers.Unconfined)
    )

    private fun client(
        id: String,
        name: String,
        email: String = "${name.lowercase().replace(' ', '.')}@example.com",
        phone: String = "3000000000"
    ) = Client(
                id = id,
                name = name,
                email = email,
                phone = phone
            )
}

private class FakeClientRepository(
    private val values: List<Client> = emptyList(),
    private val failure: Exception? = null,
    private val created: Client? = null,
    private val refreshFailure: Exception? = null,
    private val updated: Client? = null
) : ClientDataSource {
    var calls = 0
    var createCalls = 0
    var updateCalls = 0
    var lastCreated: Client? = null

    override suspend fun getClients(): List<Client> {
        calls++
        if (createCalls > 0) {
            refreshFailure?.let { throw it }
        }
        failure?.let { throw it }
        return values
    }

    override suspend fun createClient(client: Client): Client {
        createCalls++
        lastCreated = client
        failure?.let { throw it }
        return created ?: client.copy(id = "generated")
    }

    override suspend fun updateClient(id: String, client: Client): Client {
        updateCalls++
        failure?.let { throw it }
        return updated ?: client
    }
}
