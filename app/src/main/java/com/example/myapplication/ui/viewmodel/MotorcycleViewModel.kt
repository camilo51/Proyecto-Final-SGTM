package com.example.myapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.common.ApiException
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.data.repository.MotorcycleRepository
import com.google.gson.JsonParseException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

object MotorcycleStatus {
    const val IN_SERVICE = "En servicio"
    const val IN_REPAIR = "En reparación"
    const val READY_FOR_DELIVERY = "Lista para entrega"
    const val DELIVERED = "Entregada"

    val values = listOf(IN_SERVICE, IN_REPAIR, READY_FOR_DELIVERY, DELIVERED)
}

data class MotorcycleUiState(
    val motorcycles: List<Motorcycle> = emptyList(),
    val filteredMotorcycles: List<Motorcycle> = emptyList(),
    val clients: List<Client> = emptyList(),
    val selectedMotorcycle: Motorcycle? = null,
    val selectedClientId: String? = null,
    val searchQuery: String = "",
    val clientSearchQuery: String = "",
    val selectedStatus: String? = null,
    val selectedBrand: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isLoadingClients: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val detailErrorMessage: String? = null,
    val operationMessage: String? = null,
    val savedMotorcycleId: String? = null
) {
    val availableBrands: List<String>
        get() = motorcycles.mapNotNull { it.brand?.trim()?.takeIf(String::isNotBlank) }
            .distinct()
            .sorted()

    val filteredClients: List<Client>
        get() {
            val query = clientSearchQuery.trim().lowercase(Locale.ROOT)
            if (query.isBlank()) return emptyList()
            return clients.filter { client ->
                listOf(client.name, client.cedula.orEmpty(), client.phone)
                    .any { value -> value.lowercase(Locale.ROOT).contains(query) }
            }.take(CLIENT_RESULTS_LIMIT)
        }
}

class MotorcycleViewModel(
    private val motorcycleRepository: MotorcycleRepository = MotorcycleRepository(RetrofitClient.apiService),
    private val clientRepository: ClientRepository = ClientRepository(RetrofitClient.apiService),
    private val testScope: CoroutineScope? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotorcycleUiState())
    val uiState: StateFlow<MotorcycleUiState> = _uiState.asStateFlow()

    private val workScope: CoroutineScope
        get() = testScope ?: viewModelScope

    fun loadMotorcycles() {
        if (_uiState.value.isLoading) return
        workScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, operationMessage = null)
            }
            try {
                val motorcycles = motorcycleRepository.getMotorcycles()
                val clients = loadClientsSafely()
                _uiState.update {
                    it.copy(
                        motorcycles = motorcycles,
                        clients = clients,
                        isLoading = false,
                        errorMessage = null
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure("GET /motorcycles", exception)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(exception))
                }
            }
        }
    }

    fun refreshMotorcycles() {
        if (_uiState.value.isRefreshing || _uiState.value.isLoading) return
        workScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                val motorcycles = motorcycleRepository.getMotorcycles()
                val clients = loadClientsSafely()
                _uiState.update {
                    it.copy(
                        motorcycles = motorcycles,
                        clients = clients,
                        isRefreshing = false,
                        errorMessage = null
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure("GET /motorcycles (refresh)", exception)
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = messageFor(exception))
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).withFilters() }
    }

    fun onStatusFilterChange(status: String?) {
        _uiState.update {
            it.copy(selectedStatus = status?.takeIf(String::isNotBlank)).withFilters()
        }
    }

    fun onBrandFilterChange(brand: String?) {
        _uiState.update {
            it.copy(selectedBrand = brand?.takeIf(String::isNotBlank)).withFilters()
        }
    }

    fun onClientSearchQueryChange(query: String) {
        _uiState.update { it.copy(clientSearchQuery = query) }
    }

    fun clearClientSearchQuery() {
        onClientSearchQueryChange("")
    }

    fun setFormClientId(clientId: String?) {
        _uiState.update {
            it.copy(selectedClientId = clientId?.trim()?.takeIf(String::isNotBlank))
        }
    }

    fun selectClient(clientId: String) {
        if (_uiState.value.clients.any { it.id == clientId }) {
            setFormClientId(clientId)
        }
    }

    fun clearSelectedClient() {
        setFormClientId(null)
    }

    fun loadClients() {
        if (_uiState.value.isLoadingClients || _uiState.value.clients.isNotEmpty()) return
        workScope.launch {
            _uiState.update { it.copy(isLoadingClients = true) }
            try {
                val clients = clientRepository.getClients()
                _uiState.update { it.copy(clients = clients, isLoadingClients = false) }
            } catch (exception: Exception) {
                logFailure("GET /clients for motorcycle owner", exception)
                _uiState.update {
                    it.copy(isLoadingClients = false, operationMessage = messageFor(exception, "No se pudieron cargar los propietarios"))
                }
            }
        }
    }

    fun selectMotorcycle(id: String) {
        _uiState.value.motorcycles.firstOrNull { it.id == id }?.let { motorcycle ->
            _uiState.update { it.copy(selectedMotorcycle = motorcycle, detailErrorMessage = null) }
        }
    }

    fun loadMotorcycle(id: String) {
        if (id.isBlank() || _uiState.value.isLoadingDetail) return
        workScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, detailErrorMessage = null) }
            try {
                val motorcycle = motorcycleRepository.getMotorcycle(id)
                _uiState.update {
                    it.copy(selectedMotorcycle = motorcycle, isLoadingDetail = false)
                }
            } catch (exception: Exception) {
                logFailure("GET /motorcycles/$id", exception)
                _uiState.update {
                    it.copy(
                        isLoadingDetail = false,
                        detailErrorMessage = messageFor(exception, "No se pudo cargar el detalle de la motocicleta")
                    )
                }
            }
        }
    }

    fun createMotorcycle(
        plate: String,
        brand: String,
        model: String,
        yearText: String,
        color: String,
        engineCcText: String,
        clientId: String?,
        status: String,
        notes: String
    ) {
        if (_uiState.value.isSaving) return
        val input = MotorcycleInput(
            plate = plate,
            brand = brand,
            model = model,
            yearText = yearText,
            color = color,
            engineCcText = engineCcText,
            clientId = clientId,
            status = status,
            notes = notes
        )
        MotorcycleValidator.validate(input)?.let { validationError ->
            _uiState.update { it.copy(operationMessage = validationError) }
            return
        }
        validateClientReference(input.clientId)?.let { validationError ->
            _uiState.update { it.copy(operationMessage = validationError) }
            return
        }

        workScope.launch {
            _uiState.update { it.copy(isSaving = true, operationMessage = null, errorMessage = null) }
            try {
                val created = motorcycleRepository.createMotorcycle(input.toMotorcycle())
                val motorcycles = motorcycleRepository.getMotorcycles()
                val clients = loadClientsSafely()
                val selected = motorcycles.firstOrNull { it.id == created.id } ?: created
                _uiState.update {
                    it.copy(
                        motorcycles = motorcycles,
                        clients = clients,
                        selectedMotorcycle = selected,
                        isSaving = false,
                        operationMessage = "Motocicleta creada correctamente",
                        savedMotorcycleId = selected.id
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure("POST /motorcycles", exception)
                _uiState.update {
                    it.copy(isSaving = false, operationMessage = messageFor(exception, "No se pudo crear la motocicleta"))
                }
            }
        }
    }

    fun createMotorcycleInput(input: MotorcycleInput) {
        val selectedClientId = _uiState.value.selectedClientId
        createMotorcycle(
            plate = input.plate,
            brand = input.brand,
            model = input.model,
            yearText = input.yearText,
            color = input.color,
            engineCcText = input.engineCcText,
            clientId = selectedClientId,
            status = input.status,
            notes = input.notes
        )
    }

    fun updateMotorcycle(motorcycle: Motorcycle) {
        if (_uiState.value.isSaving) return
        val id = motorcycle.id?.trim()?.takeIf(String::isNotBlank)
        if (id == null) {
            _uiState.update { it.copy(operationMessage = "No se puede editar una motocicleta sin identificador") }
            return
        }
        val input = MotorcycleInput.from(motorcycle)
        MotorcycleValidator.validate(input)?.let { validationError ->
            _uiState.update { it.copy(operationMessage = validationError) }
            return
        }
        validateClientReference(input.clientId)?.let { validationError ->
            _uiState.update { it.copy(operationMessage = validationError) }
            return
        }

        workScope.launch {
            _uiState.update { it.copy(isSaving = true, operationMessage = null, errorMessage = null) }
            try {
                val updated = motorcycleRepository.updateMotorcycle(id, input.toMotorcycle(id))
                val motorcycles = motorcycleRepository.getMotorcycles()
                val clients = loadClientsSafely()
                val selected = motorcycles.firstOrNull { it.id == updated.id } ?: updated
                _uiState.update {
                    it.copy(
                        motorcycles = motorcycles,
                        clients = clients,
                        selectedMotorcycle = selected,
                        isSaving = false,
                        operationMessage = "Motocicleta actualizada correctamente",
                        savedMotorcycleId = selected.id
                    ).withFilters()
                }
            } catch (exception: Exception) {
                logFailure("PUT /motorcycles/$id", exception)
                _uiState.update {
                    it.copy(isSaving = false, operationMessage = messageFor(exception, "No se pudo actualizar la motocicleta"))
                }
            }
        }
    }

    fun consumeSavedMotorcycle() {
        _uiState.update { it.copy(savedMotorcycleId = null, operationMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, detailErrorMessage = null, operationMessage = null) }
    }

    private suspend fun loadClientsSafely(): List<Client> {
        return runCatching { clientRepository.getClients() }.getOrElse { exception ->
            logFailure("GET /clients for motorcycle relations", exception)
            _uiState.value.clients
        }
    }

    private fun validateClientReference(clientId: String?): String? {
        if (clientId == null || _uiState.value.clients.isEmpty()) return null
        return if (_uiState.value.clients.any { it.id == clientId }) {
            null
        } else {
            "Selecciona un propietario válido o elige Sin propietario."
        }
    }

    private fun MotorcycleUiState.withFilters(): MotorcycleUiState {
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        val filtered = motorcycles.filter { motorcycle ->
            val owner = clients.firstOrNull { it.id == motorcycle.clientId }
            val searchableFields = listOf(
                motorcycle.id.orEmpty(),
                motorcycle.plate.orEmpty(),
                motorcycle.brand.orEmpty(),
                motorcycle.model.orEmpty(),
                owner?.name.orEmpty(),
                owner?.cedula.orEmpty(),
                owner?.phone.orEmpty()
            ).map { it.lowercase(Locale.ROOT) }
            val matchesQuery = query.isBlank() || query.split(Regex("\\s+")).all { token ->
                searchableFields.any { value -> value.contains(token) }
            }
            val matchesStatus = selectedStatus == null || motorcycle.status == selectedStatus
            val matchesBrand = selectedBrand == null || motorcycle.brand == selectedBrand
            matchesQuery && matchesStatus && matchesBrand
        }
        return copy(filteredMotorcycles = filtered)
    }

    private fun messageFor(
        exception: Exception,
        fallback: String = "No se pudieron cargar las motocicletas."
    ): String = when (exception) {
        is SocketTimeoutException -> "El servidor tardó demasiado en responder."
        is IOException -> "No hay conexión con el servidor."
        is JsonParseException -> "No se pudo interpretar la respuesta del servidor."
        is HttpException -> when (exception.code()) {
            401 -> "Tu sesión expiró. Inicia sesión nuevamente."
            403 -> "No tienes permisos para consultar motocicletas."
            404 -> "La motocicleta no existe."
            409 -> "Ya existe una motocicleta con esta placa."
            else -> fallback
        }
        is ApiException -> exception.message.ifBlank { fallback }
        else -> fallback
    }

    private fun logFailure(operation: String, exception: Throwable) {
        if (!BuildConfig.DEBUG) return
        runCatching {
            Log.e(
                TAG,
                "$operation | ${exception::class.simpleName}: ${exception.message.orEmpty().take(MAX_LOG_CHARS)}"
            )
        }
    }

    private companion object {
        const val TAG = "SgtmMotorcycles"
        const val MAX_LOG_CHARS = 240
    }
}

private const val CLIENT_RESULTS_LIMIT = 8

data class MotorcycleInput(
    val plate: String,
    val brand: String,
    val model: String,
    val yearText: String,
    val color: String,
    val engineCcText: String,
    val clientId: String?,
    val status: String,
    val notes: String
) {
    fun toMotorcycle(id: String? = null): Motorcycle = Motorcycle(
        id = id,
        clientId = clientId?.trim()?.takeIf(String::isNotBlank),
        plate = plate.trim().uppercase(Locale.ROOT).takeIf(String::isNotBlank),
        brand = brand.trim().takeIf(String::isNotBlank),
        model = model.trim().takeIf(String::isNotBlank),
        year = yearText.trim().takeIf(String::isNotBlank)?.toIntOrNull(),
        color = color.trim().takeIf(String::isNotBlank),
        engineCc = engineCcText.trim().takeIf(String::isNotBlank)?.toIntOrNull(),
        status = status.trim().takeIf(String::isNotBlank),
        notes = notes.trim().takeIf(String::isNotBlank)
    )

    companion object {
        fun from(motorcycle: Motorcycle): MotorcycleInput = MotorcycleInput(
            plate = motorcycle.plate.orEmpty(),
            brand = motorcycle.brand.orEmpty(),
            model = motorcycle.model.orEmpty(),
            yearText = motorcycle.year?.toString().orEmpty(),
            color = motorcycle.color.orEmpty(),
            engineCcText = motorcycle.engineCc?.toString().orEmpty(),
            clientId = motorcycle.clientId,
            status = motorcycle.status ?: MotorcycleStatus.IN_SERVICE,
            notes = motorcycle.notes.orEmpty()
        )
    }
}

object MotorcycleValidator {
    private val platePattern = Regex("^[A-Z]{3}[0-9]{2}[A-Z0-9]$")

    fun validate(input: MotorcycleInput, currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)): String? {
        val plate = input.plate.trim().uppercase(Locale.ROOT)
        if (plate.isNotBlank() && !platePattern.matches(plate)) {
            return "La placa debe tener el formato ABC12D."
        }
        val year = input.yearText.trim().takeIf(String::isNotBlank)?.toIntOrNull()
            ?: if (input.yearText.isBlank()) null else return "El año no es válido."
        if (year != null && year !in 1970..(currentYear + 1)) {
            return "El año debe estar entre 1970 y ${currentYear + 1}."
        }
        val engineCc = input.engineCcText.trim().takeIf(String::isNotBlank)?.toIntOrNull()
            ?: if (input.engineCcText.isBlank()) null else return "El cilindraje no es válido."
        if (engineCc != null && engineCc !in 1..9999) {
            return "El cilindraje debe estar entre 1 y 9999 cc."
        }
        if (input.status !in MotorcycleStatus.values) return "Selecciona un estado válido."
        return null
    }
}
