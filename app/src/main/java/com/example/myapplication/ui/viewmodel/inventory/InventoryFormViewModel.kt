package com.example.myapplication.ui.viewmodel.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.inventory.CreateInventoryRequest
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.UpdateInventoryRequest
import com.example.myapplication.data.repository.inventory.InventoryRepository
import com.example.myapplication.data.repository.inventory.InventoryRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Locale

data class InventoryForm(
    val code: String = "",
    val name: String = "",
    val brand: String = "",
    val category: String = "",
    val description: String = "",
    val unit: String = "unidad",
    val quantity: String = "0",
    val minStock: String = "5",
    val unitPrice: String = "0",
    val salePrice: String = "0",
    val supplier: String = ""
)

data class InventoryFormUiState(
    val form: InventoryForm = InventoryForm(),
    val categories: List<String> = emptyList(),
    val brands: List<String> = emptyList(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedItem: InventoryDto? = null
)

object InventoryValidator {
    private val codePattern = Regex("^[A-Za-z0-9-]+$")

    fun validate(form: InventoryForm, categories: List<String> = emptyList()): Map<String, String> {
        val errors = linkedMapOf<String, String>()
        val code = form.code.trim()
        if (code.isNotEmpty() && (code.length !in 3..20 || !codePattern.matches(code))) {
            errors["code"] = "Usa entre 3 y 20 letras, números o guiones."
        }
        validateMax(form.name, 120, "name", "El nombre no puede superar 120 caracteres.", errors)
        validateMax(form.brand, 60, "brand", "La marca no puede superar 60 caracteres.", errors)
        if (form.category.isNotBlank() && categories.isNotEmpty() && form.category !in categories) {
            errors["category"] = "Selecciona una categoría válida del listado del servidor."
        }
        validateMax(form.description, 1000, "description", "La descripción no puede superar 1000 caracteres.", errors)
        validateMax(form.unit, 20, "unit", "La unidad no puede superar 20 caracteres.", errors)
        validateMax(form.supplier, 120, "supplier", "El proveedor no puede superar 120 caracteres.", errors)

        val quantity = form.quantity.toIntOrNull()
        if (quantity == null || quantity < 0) errors["quantity"] = "El stock debe ser un entero mayor o igual a cero."
        val minStock = form.minStock.toIntOrNull()
        if (minStock == null || minStock < 0) errors["minStock"] = "El stock mínimo debe ser un entero mayor o igual a cero."

        val unitPrice = form.unitPrice.toMoneyOrNull()
        val salePrice = form.salePrice.toMoneyOrNull()
        if (unitPrice == null || unitPrice < BigDecimal.ZERO) errors["unitPrice"] = "El precio de compra debe ser mayor o igual a cero."
        if (salePrice == null || salePrice < BigDecimal.ZERO) errors["salePrice"] = "El precio de venta debe ser mayor o igual a cero."
        if (unitPrice != null && salePrice != null && salePrice < unitPrice) {
            errors["salePrice"] = "El precio de venta no puede ser menor al precio de compra."
        }
        return errors
    }

    private fun validateMax(
        value: String,
        max: Int,
        field: String,
        message: String,
        errors: MutableMap<String, String>
    ) {
        if (value.length > max) errors[field] = message
    }

}

open class InventoryFormViewModel(
    private val repository: InventoryRepository = InventoryRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryFormUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadOptions()
    }

    fun updateForm(field: String, transform: (InventoryForm) -> InventoryForm) {
        _uiState.value = _uiState.value.copy(
            form = transform(_uiState.value.form),
            fieldErrors = _uiState.value.fieldErrors - field
        )
    }

    fun loadForEdit(id: Long) {
        if (_uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.getDetail(id)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    form = result.data.toForm()
                )
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun create() {
        if (_uiState.value.isSaving || _uiState.value.savedItem != null) return
        val errors = InventoryValidator.validate(_uiState.value.form, _uiState.value.categories)
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(fieldErrors = errors)
            return
        }
        val request = _uiState.value.form.toCreateRequest()
        save { repository.create(request) }
    }

    fun update(id: Long) {
        if (_uiState.value.isSaving || _uiState.value.savedItem != null) return
        val errors = InventoryValidator.validate(_uiState.value.form, _uiState.value.categories)
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(fieldErrors = errors)
            return
        }
        val request = _uiState.value.form.toUpdateRequest()
        save { repository.update(id, request) }
    }

    fun consumeSavedItem() {
        _uiState.value = _uiState.value.copy(savedItem = null)
    }

    private fun save(action: suspend () -> NetworkResult<InventoryDto>) {
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, fieldErrors = emptyMap())
        viewModelScope.launch {
            when (val result = action()) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    savedItem = result.data
                )
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.message,
                    fieldErrors = result.errors.mapNotNull { error ->
                        error.field?.let { it to (error.message ?: result.message) }
                    }.toMap()
                )
            }
        }
    }

    private fun loadOptions() {
        viewModelScope.launch {
            val categories = repository.getCategories()
            val brands = repository.getBrands()
            _uiState.value = _uiState.value.copy(
                categories = (categories as? NetworkResult.Success)?.data.orEmpty(),
                brands = (brands as? NetworkResult.Success)?.data.orEmpty()
            )
        }
    }
}

class CreateInventoryViewModel : InventoryFormViewModel()
class EditInventoryViewModel : InventoryFormViewModel()

private fun String.toMoneyOrNull(): BigDecimal? = trim().takeIf { it.isNotEmpty() }?.toBigDecimalOrNull()

private fun InventoryDto.toForm() = InventoryForm(
    code = code.orEmpty(), name = name.orEmpty(), brand = brand.orEmpty(), category = category.orEmpty(),
    description = description.orEmpty(), unit = unit ?: "unidad", quantity = quantity.toString(),
    minStock = minStock.toString(), unitPrice = unitPrice.toPlainString(), salePrice = salePrice.toPlainString(),
    supplier = supplier.orEmpty()
)

private fun InventoryForm.toCreateRequest() = CreateInventoryRequest(
    code = code.trim().ifBlank { null }?.uppercase(Locale.ROOT),
    name = name.trim().ifBlank { null }, brand = brand.trim().ifBlank { null },
    category = category.trim().ifBlank { null }, quantity = quantity.toIntOrNull(),
    minStock = minStock.toIntOrNull(), unitPrice = unitPrice.toMoneyOrNull(), salePrice = salePrice.toMoneyOrNull(),
    description = description.trim().ifBlank { null }, unit = unit.trim().ifBlank { null },
    supplier = supplier.trim().ifBlank { null }
)

private fun InventoryForm.toUpdateRequest() = UpdateInventoryRequest(
    code = code.trim().ifBlank { null }?.uppercase(Locale.ROOT),
    name = name.trim().ifBlank { null }, brand = brand.trim().ifBlank { null },
    category = category.trim().ifBlank { null }, quantity = quantity.toIntOrNull(),
    minStock = minStock.toIntOrNull(), unitPrice = unitPrice.toMoneyOrNull(), salePrice = salePrice.toMoneyOrNull(),
    description = description.trim().ifBlank { null }, unit = unit.trim().ifBlank { null },
    supplier = supplier.trim().ifBlank { null }
)
