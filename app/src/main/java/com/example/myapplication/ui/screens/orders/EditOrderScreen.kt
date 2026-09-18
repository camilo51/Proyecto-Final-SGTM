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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Order
import com.example.myapplication.ui.screens.BackNavigationLink
import com.example.myapplication.ui.viewmodel.OrderViewModel

@Composable
fun EditOrderScreen(
    orderId: String,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) { viewModel.loadOrder(orderId) }
    LaunchedEffect(state.savedOrderId) {
        if (state.savedOrderId != null) {
            viewModel.consumeOperationEvent()
            onBack()
        }
    }

    val order = state.selectedOrder
    if (state.isLoadingDetail && order == null) {
        Column(Modifier.padding(contentPadding), verticalArrangement = Arrangement.Center) { CircularProgressIndicator() }
    } else if (order == null) {
        Column(Modifier.padding(contentPadding).padding(16.dp)) {
            Text(state.detailErrorMessage ?: "No se encontró la orden", color = MaterialTheme.colorScheme.error)
            BackNavigationLink(onClick = onBack)
        }
    } else {
        key(order.id) {
            EditOrderForm(order, contentPadding, state.isSaving, state.operationMessage, viewModel, onBack)
        }
    }
}

@Composable
private fun EditOrderForm(
    order: Order,
    contentPadding: PaddingValues,
    isSaving: Boolean,
    operationMessage: String?,
    viewModel: OrderViewModel,
    onBack: () -> Unit
) {
    var clientId by rememberSaveable(order.id) { mutableStateOf(order.clientId) }
    var motorcycleId by rememberSaveable(order.id) { mutableStateOf(order.motorcycleId) }
    var description by rememberSaveable(order.id) { mutableStateOf(order.description) }
    var total by rememberSaveable(order.id) { mutableStateOf(order.total.toString()) }

    val state by viewModel.uiState.collectAsState()
    val clientOptions = state.clients.mapNotNull { client -> client.id?.let { OrderDropdownOption(it, client.name.orEmpty()) } }
    val motorcycleOptions = state.motorcycles.filter { it.clientId == clientId }.mapNotNull { motorcycle ->
        motorcycle.id?.let { OrderDropdownOption(it, "${motorcycle.brand} ${motorcycle.model} · ${motorcycle.plate}") }
    }

    Column(
        modifier = Modifier.padding(contentPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BackNavigationLink(onClick = onBack, enabled = !isSaving)
        Text("Editar orden #${order.id}", style = MaterialTheme.typography.headlineSmall)
        OrderReferenceDropdown(
            label = "Cliente",
            selectedId = clientId,
            options = clientOptions,
            enabled = !isSaving,
            autocomplete = true,
            onSelected = { clientId = it }
        )
        OrderReferenceDropdown("Motocicleta", motorcycleId, motorcycleOptions, !isSaving) { motorcycleId = it }
        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 2000) description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción") },
            supportingText = { Text("${description.length}/2000") },
            minLines = 4,
            enabled = !isSaving
        )
        OutlinedTextField(
            value = total,
            onValueChange = { total = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Total") },
            singleLine = true,
            enabled = !isSaving
        )
        operationMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                viewModel.updateOrder(
                    order.copy(
                        clientId = clientId,
                        motorcycleId = motorcycleId,
                        description = description,
                        total = total.toDoubleOrNull() ?: -1.0
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) CircularProgressIndicator() else Text("Guardar cambios")
        }
    }
}
