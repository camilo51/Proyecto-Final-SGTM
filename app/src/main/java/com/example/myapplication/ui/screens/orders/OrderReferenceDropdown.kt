package com.example.myapplication.ui.screens.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.PopupProperties
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
    autocomplete: Boolean = false,
    searchPlaceholder: String = "Buscar",
    onSelected: (String) -> Unit
) {
    if (autocomplete) {
        AutocompleteReferenceField(
            label = label,
            selectedId = selectedId,
            options = options,
            enabled = enabled,
            searchPlaceholder = searchPlaceholder,
            onSelected = onSelected
        )
    } else {
        SelectReferenceField(
            label = label,
            selectedId = selectedId,
            options = options,
            enabled = enabled,
            onSelected = onSelected
        )
    }
}

@Composable
private fun AutocompleteReferenceField(
    label: String,
    selectedId: String,
    options: List<OrderDropdownOption>,
    enabled: Boolean,
    searchPlaceholder: String,
    onSelected: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.id == selectedId }?.label.orEmpty()
    val visibleOptions = options.filter { option ->
        query.isBlank() || option.searchText.contains(query.trim(), ignoreCase = true)
    }.take(8)

    LaunchedEffect(selectedId, selectedLabel) {
        if (selectedId.isNotBlank()) query = selectedLabel
    }

    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { value ->
                query = value
                expanded = true
                if (value != selectedLabel) onSelected("")
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focus ->
                    if (focus.isFocused && enabled) expanded = true
                },
            label = { Text(label) },
            placeholder = { Text(searchPlaceholder) },
            singleLine = true,
            enabled = enabled,
            colors = AppOutlinedTextFieldColors()
        )
        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
            properties = PopupProperties(focusable = false)
        ) {
            if (visibleOptions.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No se encontraron coincidencias") },
                    onClick = { expanded = false }
                )
            } else {
                visibleOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option.label,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            query = option.label
                            onSelected(option.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectReferenceField(
    label: String,
    selectedId: String,
    options: List<OrderDropdownOption>,
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.id == selectedId }?.label.orEmpty()

    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Seleccionar $label") },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Mostrar opciones")
            },
            singleLine = true,
            enabled = enabled && options.isNotEmpty(),
            colors = AppOutlinedTextFieldColors()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled && options.isNotEmpty()) { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option.label,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onSelected(option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
