package com.example.myapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.ClientRequest
import com.example.myapplication.data.model.common.ApiException
import com.example.myapplication.data.model.common.ApiResponse
import com.example.myapplication.data.repository.ClientRepository
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ClientFormState(
    val clientId: String? = null,
    val documentType: String = "CC",
    val document: String = "",
    val name: String = "",
    val lastName: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val completionVersion: Int = 0
)

data class ClientUiState(
    val clients: List<Client> = emptyList(),
    val filteredClients: List<Client> = emptyList(),
    val visibleClients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val pageSize: Int = CLIENTS_PAGE_SIZE,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val deletingClientId: String? = null,
    val errorMessage: String? = null,
    val operationMessage: String? = null,
    val creationVersion: Int = 0,
    val updateVersion: Int = 0,
    val deleteVersion: Int = 0,
    val form: ClientFormState = ClientFormState()
) {
    val totalPages: Int
        get() = if (filteredClients.isEmpty()) {
            0
        } else {
            (filteredClients.size + pageSize - 1) / pageSize
        }
}

class ClientViewModel(
    private val clientRepository: ClientRepository = ClientRepository(RetrofitClient.apiService),
    private val testScope: CoroutineScope? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    private val workScope: CoroutineScope
        get() = testScope ?: viewModelScope

    fun loadClients() {
        if (_uiState.value.isLoading) return

        workScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val clients = clientRepository.getClients()
                _uiState.update {
                    it.copy(
                        clients = clients,
                        isLoading = false,
                        errorMessage = null
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure("GET /clients", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = messageFor(exception)
                    )
                }
            }
        }
    }

    fun refreshClients() {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return

        workScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                val clients = clientRepository.getClients()
                _uiState.update {
                    it.copy(
                        clients = clients,
                        isRefreshing = false,
                        errorMessage = null
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure("GET /clients (refresh)", exception)
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = messageFor(exception)
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).withFilters() }
    }

    fun clearSearch() {
        onSearchQueryChange("")
    }

    fun onPageChange(page: Int) {
        _uiState.update { state ->
            val lastPage = state.totalPages.coerceAtLeast(1)
            val nextPage = page.coerceIn(1, lastPage)
            state.copy(
                currentPage = nextPage,
                visibleClients = state.pageItems(nextPage)
            )
        }
    }

    fun startCreateForm() {
        _uiState.update {
            it.copy(
                operationMessage = null,
                form = ClientFormState()
            )
        }
    }

    fun startEditForm(clientId: String) {
        val client = _uiState.value.clients.firstOrNull { it.id == clientId }
        if (client != null) {
            _uiState.update {
                it.copy(
                    operationMessage = null,
                    form = client.toFormState()
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                operationMessage = null,
                form = ClientFormState(clientId = clientId, isLoading = true)
            )
        }
        workScope.launch {
            try {
                val loadedClient = clientRepository.getClient(clientId)
                _uiState.update { state -> state.copy(form = loadedClient.toFormState()) }
            } catch (exception: Exception) {
                logFailure("GET /clients/$clientId", exception)
                _uiState.update { state ->
                    state.copy(
                        form = state.form.copy(
                            isLoading = false,
                            errorMessage = messageFor(exception, "No se pudo cargar el cliente")
                        )
                    )
                }
            }
        }
    }

    fun onDocumentTypeChange(value: String) = updateForm { it.copy(documentType = value) }
    fun onDocumentChange(value: String) = updateForm { it.copy(document = value) }
    fun onNameChange(value: String) = updateForm { it.copy(name = value) }
    fun onLastNameChange(value: String) = updateForm { it.copy(lastName = value) }
    fun onPhoneChange(value: String) = updateForm { it.copy(phone = value) }

    fun createClient() {
        saveForm()
    }

    /** Compatibility entry point for callers that still provide form values directly. */
    fun createClient(
        name: String,
        document: String,
        phone: String,
        @Suppress("UNUSED_PARAMETER") legacyEmail: String = ""
    ) {
        _uiState.update {
            it.copy(
                form = it.form.copy(
                    clientId = null,
                    documentType = "CC",
                    document = document,
                    name = name,
                    lastName = "",
                    phone = phone,
                    errorMessage = null
                )
            )
        }
        saveForm()
    }

    fun updateClient() {
        saveForm()
    }

    /** Compatibility entry point for the previous list/edit flow. */
    fun updateClient(client: Client) {
        _uiState.update { it.copy(form = client.toFormState()) }
        saveForm()
    }

    private fun saveForm() {
        val state = _uiState.value
        if (state.isSaving || state.form.isLoading) return

        validateForm(state.form)?.let { validationMessage ->
            _uiState.update { it.copy(form = it.form.copy(errorMessage = validationMessage)) }
            return
        }

        val request = state.form.toRequest()
        val clientId = state.form.clientId
        workScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    operationMessage = null,
                    errorMessage = null,
                    form = it.form.copy(errorMessage = null)
                )
            }
            try {
                if (clientId == null) {
                    clientRepository.createClient(request)
                } else {
                    clientRepository.updateClient(clientId, request)
                }

                val refreshedClients = try {
                    clientRepository.getClients()
                } catch (refreshException: Exception) {
                    logFailure("GET /clients after client mutation", refreshException)
                    null
                }

                _uiState.update { current ->
                    val nextFormVersion = current.form.completionVersion + 1
                    current.copy(
                        clients = refreshedClients ?: current.clients,
                        isSaving = false,
                        operationMessage = if (clientId == null) {
                            if (refreshedClients == null) {
                                "Cliente creado correctamente, pero no se pudo actualizar la lista."
                            } else {
                                "Cliente creado correctamente"
                            }
                        } else {
                            if (refreshedClients == null) {
                                "Cliente actualizado correctamente, pero no se pudo actualizar la lista."
                            } else {
                                "Cliente actualizado correctamente"
                            }
                        },
                        creationVersion = if (clientId == null) current.creationVersion + 1 else current.creationVersion,
                        updateVersion = if (clientId != null) current.updateVersion + 1 else current.updateVersion,
                        form = current.form.copy(
                            isLoading = false,
                            errorMessage = null,
                            completionVersion = nextFormVersion
                        )
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure(if (clientId == null) "POST /clients" else "PUT /clients/$clientId", exception)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        operationMessage = null,
                        form = it.form.copy(
                            errorMessage = messageFor(
                                exception,
                                if (clientId == null) "No se pudo crear el cliente" else "No se pudo actualizar el cliente"
                            )
                        )
                    )
                }
            }
        }
    }

    fun consumeFormCompletion() {
        _uiState.update { it.copy(form = it.form.copy(completionVersion = 0)) }
    }

    fun clearOperationMessage() {
        _uiState.update { it.copy(operationMessage = null) }
    }

    fun deleteClient(clientId: String, reason: String) {
        if (_uiState.value.isDeleting || clientId.isBlank()) return
        if (reason.trim().isBlank()) {
            _uiState.update { it.copy(operationMessage = "Ingresa un motivo para eliminar el cliente") }
            return
        }

        workScope.launch {
            _uiState.update {
                it.copy(
                    isDeleting = true,
                    deletingClientId = clientId,
                    operationMessage = null,
                    errorMessage = null
                )
            }
            try {
                clientRepository.deleteClient(clientId, reason.trim())
                val refreshedClients = runCatching { clientRepository.getClients() }.getOrNull()
                _uiState.update { state ->
                    state.copy(
                        clients = refreshedClients ?: state.clients.filterNot { client -> client.id == clientId },
                        isDeleting = false,
                        deletingClientId = null,
                        operationMessage = if (refreshedClients == null) {
                            "Cliente eliminado correctamente, pero no se pudo actualizar la lista."
                        } else {
                            "Cliente eliminado correctamente"
                        },
                        deleteVersion = state.deleteVersion + 1
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure("DELETE /clients/$clientId", exception)
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        deletingClientId = null,
                        operationMessage = messageFor(exception, "No se pudo eliminar el cliente")
                    )
                }
            }
        }
    }

    private fun updateForm(transform: (ClientFormState) -> ClientFormState) {
        _uiState.update { it.copy(form = transform(it.form).copy(errorMessage = null)) }
    }

    private fun validateForm(form: ClientFormState): String? {
        val document = form.document.trim()
        if (document.isNotBlank() && document.length !in 5..20) {
            return "El documento debe tener entre 5 y 20 caracteres"
        }
        if (form.name.trim().length > 100 || form.lastName.trim().length > 100) {
            return "Nombre y apellido admiten máximo 100 caracteres"
        }
        if (form.phone.trim().isNotBlank() && !COLOMBIAN_PHONE_PATTERN.matches(form.phone.trim())) {
            return "Ingresa un teléfono colombiano válido de 10 dígitos que inicie en 3"
        }
        return null
    }

    private fun ClientFormState.toRequest(): ClientRequest = ClientRequest(
        documentType = documentType.ifBlank { "CC" },
        document = document.trim().takeIf(String::isNotBlank),
        name = name.trim().takeIf(String::isNotBlank),
        lastName = lastName.trim().takeIf(String::isNotBlank),
        phone = phone.trim().takeIf(String::isNotBlank)
    )

    private fun Client.toFormState(): ClientFormState = ClientFormState(
        clientId = id,
        documentType = documentType ?: "CC",
        document = document.orEmpty(),
        name = name.orEmpty(),
        lastName = lastName.orEmpty(),
        phone = phone.orEmpty()
    )

    private fun ClientUiState.withFilters(): ClientUiState {
        val filtered = filterClientsForQuery(clients, searchQuery)

        return copy(
            filteredClients = filtered,
            currentPage = 1,
            visibleClients = filtered.take(pageSize)
        )
    }

    private fun ClientUiState.pageItems(page: Int): List<Client> {
        val startIndex = (page - 1) * pageSize
        return filteredClients.drop(startIndex).take(pageSize)
    }

    private fun messageFor(
        exception: Exception,
        fallback: String = "No se pudieron cargar los clientes"
    ): String = when (exception) {
        is SocketTimeoutException -> "El servidor tardó demasiado en responder"
        is IOException -> "No hay conexión con el servidor"
        is HttpException -> when (exception.code()) {
            400, 422 -> httpErrorDetail(exception) ?: "Los datos enviados no son válidos"
            401 -> "Tu sesión expiró. Inicia sesión nuevamente."
            403 -> "No tienes permisos para realizar esta operación"
            404 -> "Cliente no encontrado"
            409 -> "Ya existe un cliente con este documento."
            in 500..599 -> "El servidor presentó un error"
            else -> httpErrorDetail(exception) ?: fallback
        }
        is ApiException -> exception.message.ifBlank { fallback }
        is JsonParseException -> "No se pudo interpretar la respuesta del servidor"
        else -> fallback
    }

    private fun httpErrorDetail(exception: HttpException): String? = runCatching {
        val body = exception.response()?.errorBody()?.string().orEmpty()
        if (body.isBlank()) return@runCatching null
        val response = Gson().fromJson<ApiResponse<Any>>(
            body,
            object : TypeToken<ApiResponse<Any>>() {}.type
        )
        val fieldErrors = response.errors.mapNotNull { error ->
            val field = error.field?.let(::fieldLabel)
            error.message?.let { message -> if (field == null) message else "$field: $message" }
        }
        fieldErrors.takeIf { it.isNotEmpty() }?.joinToString(". ")
            ?: response.message.takeIf(String::isNotBlank)
    }.getOrNull()

    private fun fieldLabel(field: String): String = when (field) {
        "document_type" -> "Tipo de documento"
        "document" -> "Documento"
        "last_name" -> "Apellido"
        "phone" -> "Teléfono"
        else -> field
    }

    private fun logFailure(operation: String, exception: Exception) {
        if (!BuildConfig.DEBUG) return

        val code = when (exception) {
            is HttpException -> "HTTP ${exception.code()}"
            is ApiException -> "API ${exception.statusCode ?: "sin código"}"
            else -> exception::class.simpleName ?: "Exception"
        }
        runCatching {
            Log.e(
                TAG,
                "[CLIENTS_DEBUG] $operation | $code | ${exception.message.orEmpty().take(MAX_LOG_CHARS)}"
            )
        }
    }

    private companion object {
        const val TAG = "SgtmClients"
        const val MAX_LOG_CHARS = 240
        val COLOMBIAN_PHONE_PATTERN = Regex("^3\\d{9}$")
    }
}

private const val CLIENTS_PAGE_SIZE = 10
