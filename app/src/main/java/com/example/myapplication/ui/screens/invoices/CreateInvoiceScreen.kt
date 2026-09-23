package com.example.myapplication.ui.screens.invoices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.screens.FormHeader
import com.example.myapplication.ui.screens.OperationMessage
import com.example.myapplication.ui.screens.FormPrimaryButton
import com.example.myapplication.ui.screens.orders.OrderDropdownOption
import com.example.myapplication.ui.screens.orders.OrderReferenceDropdown
import com.example.myapplication.ui.screens.orders.formatOrderMoney
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.InvoiceViewModel

@Composable
fun CreateInvoiceScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    viewModel: InvoiceViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val initialCreationVersion = remember { state.creationVersion }
    var orderId by rememberSaveable { mutableStateOf("") }
    var paymentMethod by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val orderOptions = remember(state.orders, state.clients) {
        state.orders.mapNotNull { order ->
            order.id?.let { id ->
                val client = state.clients.firstOrNull { it.id == order.clientId }
                OrderDropdownOption(
                    id = id,
                    label = "${order.orderNumber ?: "Orden $id"} · ${client?.name.orEmpty()}",
                    searchText = listOf(id, order.orderNumber, client?.name, client?.document)
                        .filterNotNull()
                        .joinToString(" ")
                )
            }
        }
    }
    val selectedOrder = state.orders.firstOrNull { it.id == orderId }

    LaunchedEffect(Unit) {
        viewModel.clearOperationMessage()
        viewModel.loadReferences()
    }
    LaunchedEffect(state.creationVersion) {
        if (state.creationVersion > initialCreationVersion) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FormHeader(
            title = "Nueva factura",
            subtitle = "El cliente y el total se toman de la orden de trabajo seleccionada.",
            onBack = onBack
        )

        if (state.isLoadingReferences) {
            Text("Cargando órdenes de trabajo…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OrderReferenceDropdown(
            label = "Orden de trabajo",
            selectedId = orderId,
            options = orderOptions,
            enabled = !state.isSaving && !state.isLoadingReferences,
            autocomplete = true,
            searchPlaceholder = "Busca por OT o cliente",
            onSelected = { orderId = it }
        )
        selectedOrder?.let { order ->
            Text(
                "Total de la orden: ${formatOrderMoney(order.total)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        OrderReferenceDropdown(
            label = "Modo de pago",
            selectedId = paymentMethod,
            options = paymentMethods,
            enabled = !state.isSaving,
            onSelected = { paymentMethod = it }
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notas (opcional)") },
            minLines = 3,
            enabled = !state.isSaving,
            colors = AppOutlinedTextFieldColors()
        )
        state.operationMessage?.let { message ->
            OperationMessage(message)
        }
        FormPrimaryButton(
            text = "Emitir factura",
            onClick = { viewModel.createInvoice(orderId, paymentMethod, notes) },
            enabled = !state.isSaving && !state.isLoadingReferences
        )
        Spacer(Modifier.size(8.dp))
    }
}

private val paymentMethods = listOf(
    OrderDropdownOption("Efectivo", "Efectivo"),
    OrderDropdownOption("Tarjeta", "Tarjeta"),
    OrderDropdownOption("Transferencia", "Transferencia")
)
