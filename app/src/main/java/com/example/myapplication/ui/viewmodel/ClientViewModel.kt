package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.api.ApiException
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.repository.ClientRepository
import com.google.gson.JsonParseException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

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
    val errorMessage: String? = null,
    val operationMessage: String? = null,
    val creationVersion: Int = 0,
    val updateVersion: Int = 0
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

    fun createClient(
        name: String,
        cedula: String,
        phone: String,
        email: String
    ) {
        if (_uiState.value.isSaving) return

        val normalizedEmail = email.trim()
        if (normalizedEmail.isNotBlank() && !EMAIL_PATTERN.matches(normalizedEmail)) {
            _uiState.update {
                it.copy(operationMessage = "Ingresa un correo válido o déjalo vacío")
            }
            return
        }

        workScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    operationMessage = null,
                    errorMessage = null
                )
            }
            try {
                clientRepository.createClient(
                    Client(
                        name = name.trim(),
                        cedula = cedula.trim().takeIf(String::isNotBlank),
                        phone = phone.trim(),
                        email = normalizedEmail
                    )
                )

                val clients = try {
                    clientRepository.getClients()
                } catch (refreshException: Exception) {
                    logFailure("GET /clients after POST /clients", refreshException)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            operationMessage = "Cliente creado correctamente, pero no se pudo actualizar la lista.",
                            creationVersion = it.creationVersion + 1
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        clients = clients,
                        isSaving = false,
                        operationMessage = "Cliente creado correctamente",
                        creationVersion = it.creationVersion + 1
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure("POST /clients", exception)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        operationMessage = messageFor(exception, "No se pudo crear el cliente")
                    )
                }
            }
        }
    }

    fun updateClient(client: Client) {
        if (_uiState.value.isSaving) return

        val id = client.id?.trim()?.takeIf(String::isNotBlank)
        if (id == null) {
            _uiState.update {
                it.copy(operationMessage = "No se puede editar un cliente sin identificador")
            }
            return
        }

        val normalizedEmail = client.email.trim()
        if (normalizedEmail.isNotBlank() && !EMAIL_PATTERN.matches(normalizedEmail)) {
            _uiState.update {
                it.copy(operationMessage = "Ingresa un correo válido o déjalo vacío")
            }
            return
        }

        val normalizedClient = client.copy(
            id = id,
            name = client.name.trim(),
            cedula = client.cedula?.trim()?.takeIf(String::isNotBlank),
            phone = client.phone.trim(),
            email = normalizedEmail
        )

        workScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    operationMessage = null,
                    errorMessage = null
                )
            }
            try {
                clientRepository.updateClient(id, normalizedClient)

                val clients = try {
                    clientRepository.getClients()
                } catch (refreshException: Exception) {
                    logFailure("GET /clients after PUT /clients/$id", refreshException)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            operationMessage = "Cliente actualizado correctamente, pero no se pudo actualizar la lista.",
                            updateVersion = it.updateVersion + 1
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        clients = clients,
                        isSaving = false,
                        operationMessage = "Cliente actualizado correctamente",
                        updateVersion = it.updateVersion + 1
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure("PUT /clients/$id", exception)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        operationMessage = messageFor(exception, "No se pudo actualizar el cliente")
                    )
                }
            }
        }
    }

    fun clearOperationMessage() {
        _uiState.update { it.copy(operationMessage = null) }
    }

    private fun ClientUiState.withFilters(): ClientUiState {
        val normalizedQuery = searchQuery.trim().lowercase(Locale.ROOT)
        val filtered = if (normalizedQuery.isBlank()) {
            clients
        } else {
            clients.filter { client ->
                listOf(
                    client.name.orEmpty(),
                    client.cedula.orEmpty(),
                    client.email.orEmpty(),
                    client.phone.orEmpty(),
                    client.id.orEmpty()
                )
                    .any { value -> value.lowercase(Locale.ROOT).contains(normalizedQuery) }
            }
        }

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
            400 -> "Los datos enviados no son válidos"
            401 -> "Tu sesión expiró. Inicia sesión nuevamente."
            403 -> "No tienes permisos para consultar clientes"
            404 -> "No se encontraron clientes"
            409 -> "Ya existe un cliente con este número de documento."
            in 500..599 -> "El servidor presentó un error"
            else -> fallback
        }
        is ApiException -> exception.message?.ifBlank { fallback } ?: fallback
        is JsonParseException -> "No se pudo interpretar la respuesta del servidor"
        else -> fallback
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
        val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}

private const val CLIENTS_PAGE_SIZE = 10
