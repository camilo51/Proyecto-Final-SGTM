package com.example.myapplication.clients

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.ClientRequest
import com.example.myapplication.data.model.common.ApiResponse
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.ui.viewmodel.ClientViewModel
import com.google.gson.Gson
import java.io.IOException
import java.lang.reflect.Proxy
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
    fun loadClients_success_updatesStateAndSearchesRealFields() {
        val repository = FakeClientRepository(
            values = listOf(
                client("1", "Ana", lastName = "Gómez", phone = "3001112233"),
                client("2", "Carlos", lastName = "Pérez", document = "12345678")
            )
        )
        val viewModel = viewModel(repository)

        viewModel.loadClients()
        viewModel.onSearchQueryChange("Pérez")

        assertEquals(listOf("2"), viewModel.uiState.value.filteredClients.map { it.id })
        assertEquals(1, repository.getCalls)
    }

    @Test
    fun pagination_limitsVisibleClientsAndChangesPage() {
        val repository = FakeClientRepository(
            values = (1..25).map { id -> client(id.toString(), "Cliente $id") }
        )
        val viewModel = viewModel(repository)

        viewModel.loadClients()
        viewModel.onPageChange(3)

        assertEquals(3, viewModel.uiState.value.totalPages)
        assertEquals(5, viewModel.uiState.value.visibleClients.size)
        assertEquals("21", viewModel.uiState.value.visibleClients.first().id)
    }

    @Test
    fun loadClients_failure_exposesFriendlyError() {
        val viewModel = viewModel(FakeClientRepository(failure = IOException()))

        viewModel.loadClients()

        assertEquals("No hay conexión con el servidor", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun createClient_validFormSendsBackendContractAndRefreshesList() {
        val created = client("3", "Jhon", document = "12345678", phone = "3004567890")
        val repository = FakeClientRepository(values = listOf(created))
        val viewModel = viewModel(repository)

        viewModel.startCreateForm()
        viewModel.onDocumentTypeChange("CC")
        viewModel.onDocumentChange("12345678")
        viewModel.onNameChange("jhon")
        viewModel.onPhoneChange("3004567890")
        viewModel.createClient()

        assertEquals(1, repository.createCalls)
        assertEquals("CC", repository.lastCreated?.documentType)
        assertEquals("12345678", repository.lastCreated?.document)
        assertEquals("jhon", repository.lastCreated?.name)
        assertEquals("3004567890", repository.lastCreated?.phone)
        assertEquals("Cliente creado correctamente", viewModel.uiState.value.operationMessage)
        assertEquals(1, viewModel.uiState.value.form.completionVersion)
    }

    @Test
    fun createClient_blankOptionalFieldsAreOmittedFromPayload() {
        val repository = FakeClientRepository(values = listOf(client("4", "Sin datos")))
        val viewModel = viewModel(repository)

        viewModel.startCreateForm()
        viewModel.createClient()

        val json = Gson().toJson(repository.lastCreated)
        assertEquals(1, repository.createCalls)
        assertTrue(json.contains("\"document_type\":\"CC\""))
        assertFalse(json.contains("\"document\""))
        assertFalse(json.contains("\"phone\""))
        assertFalse(json.contains("\"email\""))
        assertFalse(json.contains("\"address\""))
        assertFalse(json.contains("\"city\""))
        assertFalse(json.contains("\"notes\""))
    }

    @Test
    fun createClient_invalidColombianPhone_isRejectedBeforePost() {
        val repository = FakeClientRepository()
        val viewModel = viewModel(repository)

        viewModel.startCreateForm()
        viewModel.onNameChange("jhon")
        viewModel.onPhoneChange("34567890")
        viewModel.createClient()

        assertEquals(0, repository.createCalls)
        assertTrue(viewModel.uiState.value.form.errorMessage.orEmpty().contains("teléfono colombiano"))
    }

    @Test
    fun createClient_backendValidationError_exposesFieldDetail() {
        val errorBody = """
            {
              "success": false,
              "message": "Errores de validación",
              "errors": [{"field":"phone","message":"Ingresa un teléfono colombiano válido"}]
            }
        """.trimIndent()
        val repository = FakeClientRepository(
            failure = HttpException(Response.error<Any>(400, errorBody.toResponseBody()))
        )
        val viewModel = viewModel(repository)

        viewModel.startCreateForm()
        viewModel.onPhoneChange("3004567890")
        viewModel.createClient()

        assertEquals(
            "Teléfono: Ingresa un teléfono colombiano válido",
            viewModel.uiState.value.form.errorMessage
        )
    }

    @Test
    fun createClient_duplicateDocument_showsFriendlyMessage() {
        val repository = FakeClientRepository(
            failure = HttpException(
                Response.error<Any>(
                    409,
                    """{"success":false,"message":"El documento 12345678 ya está registrado"}"""
                        .toResponseBody()
                )
            )
        )
        val viewModel = viewModel(repository)

        viewModel.startCreateForm()
        viewModel.onDocumentChange("12345678")
        viewModel.createClient()

        assertEquals(
            "Ya existe un cliente con este documento.",
            viewModel.uiState.value.form.errorMessage
        )
    }

    @Test
    fun updateClient_success_usesSameFormAndRefreshesClients() {
        val updated = client("7", "Ana actualizada", lastName = "Pérez", phone = "3112223344")
        val repository = FakeClientRepository(values = listOf(updated))
        val viewModel = viewModel(repository)

        viewModel.startEditForm("7")
        viewModel.onNameChange("Ana actualizada")
        viewModel.createClient()

        assertEquals(1, repository.updateCalls)
        assertEquals("Ana actualizada", repository.lastUpdated?.name)
        assertEquals("Cliente actualizado correctamente", viewModel.uiState.value.operationMessage)
    }

    @Test
    fun deleteClient_requiresReasonAndSendsItToBackend() {
        val repository = FakeClientRepository(values = listOf(client("11", "Eliminar")))
        val viewModel = viewModel(repository)
        viewModel.loadClients()

        viewModel.deleteClient("11", "Cliente duplicado")

        assertEquals(1, repository.deleteCalls)
        assertEquals("Cliente duplicado", repository.lastDeleteReason)
        assertTrue(viewModel.uiState.value.clients.isEmpty())
        assertEquals("Cliente eliminado correctamente", viewModel.uiState.value.operationMessage)
    }

    @Test
    fun deleteClient_blankReason_doesNotCallRepository() {
        val repository = FakeClientRepository(values = listOf(client("12", "Eliminar")))
        val viewModel = viewModel(repository)

        viewModel.deleteClient("12", " ")

        assertEquals(0, repository.deleteCalls)
        assertEquals("Ingresa un motivo para eliminar el cliente", viewModel.uiState.value.operationMessage)
    }

    private fun viewModel(repository: ClientRepository): ClientViewModel = ClientViewModel(
        clientRepository = repository,
        testScope = CoroutineScope(Dispatchers.Unconfined)
    )

    private fun client(
        id: String,
        name: String,
        lastName: String = "",
        document: String? = null,
        phone: String = "3000000000"
    ) = Client(
        id = id,
        documentType = "CC",
        document = document,
        name = name,
        lastName = lastName,
        phone = phone,
        status = "Activo"
    )
}

private class FakeClientRepository(
    private val values: List<Client> = emptyList(),
    private val failure: Exception? = null
) : ClientRepository(noOpApiService) {
    private val deletedIds = mutableSetOf<String>()
    var getCalls = 0
    var createCalls = 0
    var updateCalls = 0
    var deleteCalls = 0
    var lastCreated: ClientRequest? = null
    var lastUpdated: ClientRequest? = null
    var lastDeleteReason: String? = null

    override suspend fun getClients(): List<Client> {
        getCalls++
        failure?.let { throw it }
        return values.filterNot { it.id in deletedIds }
    }

    override suspend fun getClient(id: String): Client {
        return values.first { it.id == id }
    }

    override suspend fun createClient(request: ClientRequest): Client {
        createCalls++
        lastCreated = request
        failure?.let { throw it }
        return values.firstOrNull() ?: Client(id = "generated")
    }

    override suspend fun updateClient(id: String, request: ClientRequest): Client {
        updateCalls++
        lastUpdated = request
        failure?.let { throw it }
        return values.first { it.id == id }
    }

    override suspend fun deleteClient(id: String, reason: String) {
        deleteCalls++
        lastDeleteReason = reason
        failure?.let { throw it }
        deletedIds += id
    }
}

private val noOpApiService: ApiService = Proxy.newProxyInstance(
    ApiService::class.java.classLoader,
    arrayOf(ApiService::class.java)
) { _, method, _ ->
    error("Llamada inesperada a ApiService: ${method.name}")
} as ApiService
