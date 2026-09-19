package com.example.myapplication.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.viewmodel.inventory.InventoryForm
import com.example.myapplication.ui.viewmodel.inventory.InventoryFormUiState

@Composable
internal fun InventoryFormFields(
    state: InventoryFormUiState,
    submitLabel: String,
    onFieldChange: (String, (InventoryForm) -> InventoryForm) -> Unit,
    onSubmit: () -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InventoryTextField("Código", state.form.code, state.fieldErrors["code"]) {
            onFieldChange("code") { form -> form.copy(code = it) }
        }
        InventoryTextField("Nombre", state.form.name, state.fieldErrors["name"]) {
            onFieldChange("name") { form -> form.copy(name = it) }
        }
        InventoryTextField("Marca", state.form.brand, state.fieldErrors["brand"]) {
            onFieldChange("brand") { form -> form.copy(brand = it) }
        }
        Column {
            OutlinedTextField(
                value = state.form.category,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Categoría") },
                readOnly = true,
                isError = state.fieldErrors.containsKey("category")
            )
            DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                state.categories.forEach { category ->
                    DropdownMenuItem(text = { Text(category) }, onClick = {
                        onFieldChange("category") { form -> form.copy(category = category) }
                        categoryExpanded = false
                    })
                }
            }
            Button(onClick = { categoryExpanded = true }, enabled = state.categories.isNotEmpty()) {
                Text(if (state.categories.isEmpty()) "Cargando categorías" else "Seleccionar categoría")
            }
            FieldError(state.fieldErrors["category"])
        }
        InventoryTextField("Descripción", state.form.description, state.fieldErrors["description"], singleLine = false) {
            onFieldChange("description") { form -> form.copy(description = it) }
        }
        InventoryTextField("Unidad", state.form.unit, state.fieldErrors["unit"]) {
            onFieldChange("unit") { form -> form.copy(unit = it) }
        }
        InventoryTextField("Cantidad inicial", state.form.quantity, state.fieldErrors["quantity"], KeyboardType.Number) {
            onFieldChange("quantity") { form -> form.copy(quantity = it) }
        }
        InventoryTextField("Stock mínimo", state.form.minStock, state.fieldErrors["minStock"], KeyboardType.Number) {
            onFieldChange("minStock") { form -> form.copy(minStock = it) }
        }
        InventoryTextField("Precio de compra", state.form.unitPrice, state.fieldErrors["unitPrice"], KeyboardType.Decimal) {
            onFieldChange("unitPrice") { form -> form.copy(unitPrice = it) }
        }
        InventoryTextField("Precio de venta", state.form.salePrice, state.fieldErrors["salePrice"], KeyboardType.Decimal) {
            onFieldChange("salePrice") { form -> form.copy(salePrice = it) }
        }
        InventoryTextField("Proveedor", state.form.supplier, state.fieldErrors["supplier"]) {
            onFieldChange("supplier") { form -> form.copy(supplier = it) }
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = onSubmit,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) CircularProgressIndicator(modifier = Modifier.padding(2.dp))
            else Text(submitLabel)
        }
    }
}

@Composable
private fun InventoryTextField(
    label: String,
    value: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(label) },
            isError = error != null,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
        FieldError(error)
    }
}

@Composable
private fun FieldError(error: String?) {
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
}
