package com.example.myapplication.ui.viewmodel.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.common.PaginationDto
import com.example.myapplication.data.model.inventory.InventoryMovementDto
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.StockAdjustmentRequest
import com.example.myapplication.data.model.inventory.StockEntryRequest
import com.example.myapplication.data.model.inventory.StockMovementAction
import com.example.myapplication.data.model.inventory.StockOutputRequest
import com.example.myapplication.data.repository.inventory.InventoryRepository
import com.example.myapplication.data.repository.inventory.InventoryRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InventoryMovementsUiState(
    val item: InventoryDto? = null,
    val movements: List<InventoryMovementDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

object StockMovementValidator {
    fun validate(
        action: StockMovementAction,
        quantity: Int?,
        notes: String,
        visibleStock: Int
    ): String? {
        if (notes.trim().length < 3) return "Indica un motivo descriptivo para el movimiento."
        if (quantity == null) return "La cantidad debe ser un número entero."
        if (action == StockMovementAction.ADJUSTMENT && quantity < 0) {
            return "El ajuste no puede dejar el stock en negativo."
        }
        if (action != StockMovementAction.ADJUSTMENT && quantity <= 0) {
            return "La cantidad debe ser mayor que cero."
        }
        if (action == StockMovementAction.OUTPUT && quantity > visibleStock) {
            return "No puedes retirar más unidades que el stock visible."
        }
        return null
    }
}

class InventoryMovementsViewModel(
    private val repository: InventoryRepository = InventoryRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryMovementsUiState())
    val uiState = _uiState.asStateFlow()
    private var currentId: Long? = null

    fun load(id: Long, page: Int = 1) {
        currentId = id
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val item = repository.getDetail(id)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(item = item.data)
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = item.message)
            }
            when (val result = repository.getMovements(id, page, 20)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    movements = result.data.movements,
                    pagination = result.data.pagination,
                    isLoading = false
                )
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun submit(
        action: StockMovementAction,
        quantityText: String,
        notes: String,
        visibleStock: Int
    ) {
        val id = currentId ?: return
        if (_uiState.value.isSubmitting) return
        val quantity = quantityText.toIntOrNull()
        StockMovementValidator.validate(action, quantity, notes, visibleStock)?.let { error ->
            _uiState.value = _uiState.value.copy(errorMessage = error)
            return
        }
        _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            val result = when (action) {
                StockMovementAction.ENTRY -> repository.registerEntry(id, StockEntryRequest(quantity!!, notes.trim()))
                StockMovementAction.OUTPUT -> repository.registerOutput(id, StockOutputRequest(quantity!!, notes.trim()))
                StockMovementAction.ADJUSTMENT -> repository.registerAdjustment(id, StockAdjustmentRequest(quantity!!, notes.trim()))
            }
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        item = result.data.item ?: _uiState.value.item,
                        successMessage = "Movimiento registrado. Stock actual: ${result.data.quantityAfter ?: "actualizado"}."
                    )
                    load(id, _uiState.value.pagination.page)
                }
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
