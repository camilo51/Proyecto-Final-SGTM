package com.example.myapplication.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.viewmodel.OrderViewModel

@Composable
fun CreateOrderScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onOrderCreated: (String) -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadReferences() }
    var clientId by rememberSaveable { mutableStateOf("") }
    var motorcycleId by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("") }
    var total by rememberSaveable { mutableStateOf("") }

    val motorcycles = state.motorcycles.filter { it.clientId == clientId }
    val clientOptions = state.clients.mapNotNull { client ->
        client.id?.let { OrderDropdownOption(it, client.name) }
    }
    val motorcycleOptions = motorcycles.mapNotNull { motorcycle ->
        motorcycle.id?.let { OrderDropdownOption(it, "${motorcycle.brand} ${motorcycle.model} · ${motorcycle.plate}") }
    }

    LaunchedEffect(state.savedOrderId) {
        state.savedOrderId?.let { id ->
            viewModel.consumeOperationEvent()
            onOrderCreated(id)
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nueva orden", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Los campos disponibles corresponden al modelo Order actual.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OrderReferenceDropdown(
            label = "Cliente",
            selectedId = clientId,
            options = clientOptions,
            enabled = !state.isSaving,
            onSelected = {
                clientId = it
                if (motorcycles.none { motorcycle -> motorcycle.id == motorcycleId && motorcycle.clientId == it }) {
                    motorcycleId = ""
                }
            }
        )
        OrderReferenceDropdown(
            label = "Motocicleta",
            selectedId = motorcycleId,
            options = motorcycleOptions,
            enabled = !state.isSaving && clientId.isNotBlank(),
            onSelected = { motorcycleId = it }
        )
        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 2000) description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción") },
            supportingText = { Text("${description.length}/2000") },
            minLines = 4,
            enabled = !state.isSaving
        )
        OutlinedTextField(
            value = status,
            onValueChange = { status = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Estado") },
            supportingText = { Text("Usa un estado aceptado por la API") },
            singleLine = true,
            enabled = !state.isSaving
        )
        OutlinedTextField(
            value = total,
            onValueChange = { total = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Total") },
            singleLine = true,
            enabled = !state.isSaving
        )

        state.operationMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { viewModel.createOrder(clientId, motorcycleId, description, status, total) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) CircularProgressIndicator() else Text("Crear orden")
        }
        Button(onClick = onBack, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}
