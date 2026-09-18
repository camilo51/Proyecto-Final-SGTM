package com.example.myapplication.ui.screens.orders

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors

data class OrderDropdownOption(
    val id: String,
    val label: String,
    val searchText: String = label
)

@Composable
fun OrderReferenceDropdown(
    label: String,
    selectedId: String,
    options: List<OrderDropdownOption>,
    enabled: Boolean = true,
    searchable: Boolean = false,
    searchPlaceholder: String = "Buscar",
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedLabel = options.firstOrNull { it.id == selectedId }?.label
        ?: if (selectedId.isBlank()) "Seleccionar $label" else selectedId
    val visibleOptions = options.filter { option ->
        searchQuery.isBlank() || option.searchText.contains(searchQuery, ignoreCase = true)
    }.take(30)

    Box {
        OutlinedButton(
            onClick = { searchQuery = ""; expanded = true },
            enabled = enabled && options.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text("$label: $selectedLabel")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            if (searchable) {
                Column(Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(searchPlaceholder) },
                        singleLine = true,
                        colors = AppOutlinedTextFieldColors()
                    )
                }
            }
            visibleOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option.id)
                        expanded = false
                    }
                )
            }
            if (visibleOptions.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No se encontraron coincidencias") },
                    onClick = {}
                )
            }
        }
    }
}
