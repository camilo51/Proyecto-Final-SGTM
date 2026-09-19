package com.example.myapplication.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Order
import com.example.myapplication.ui.screens.BackNavigationLink
import com.example.myapplication.ui.viewmodel.OrderStatus
import com.example.myapplication.ui.viewmodel.OrderViewModel

@Composable
fun OrderDetailScreen(
    orderId: String,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
        viewModel.loadReferences()
    }
    LaunchedEffect(state.deletedOrderId) {
        if (state.deletedOrderId == orderId) {
            viewModel.consumeOperationEvent()
            onBack()
        }
    }

    val order = state.selectedOrder
    if (state.isLoadingDetail && (order == null || order.id != orderId)) {
        Column(Modifier.padding(contentPadding), verticalArrangement = Arrangement.Center) { CircularProgressIndicator() }
    } else if (order == null || order.id != orderId) {
        Column(Modifier.padding(contentPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(state.detailErrorMessage ?: "No se encontró la orden", color = MaterialTheme.colorScheme.error)
            BackNavigationLink(onClick = onBack)
        }
    } else {
        OrderDetailContent(
            order = order,
            contentPadding = contentPadding,
            clientName = state.clients.firstOrNull { it.id == order.clientId }?.name,
            motorcycleLabel = state.motorcycles.firstOrNull { it.id == order.motorcycleId }?.let {
                "${it.brand} ${it.model} · ${it.plate}"
            },
            employeeLabel = state.employees.firstOrNull { it.id == order.assignedEmployeeId }
                ?.let { employee ->
                    listOf(employee.name.orEmpty(), employee.lastName.orEmpty())
                        .filter(String::isNotBlank)
                        .joinToString(" ")
                        .ifBlank { "Técnico sin nombre" }
                },
            statuses = (state.statuses + OrderStatus.changeableValues + order.status)
                .filter(String::isNotBlank)
                .distinct(),
            isSaving = state.isSaving,
            isUpdatingStatus = state.updatingOrderId == order.id,
            isDeleting = state.isDeleting,
            operationMessage = state.operationMessage,
            onStatusChange = viewModel::changeStatus,
            onEdit = { onEdit(orderId) },
            onDelete = { showDeleteConfirmation = true },
            onBack = onBack
        )
    }

    if (showDeleteConfirmation && order != null) {
        AlertDialog(
            onDismissRequest = { if (!state.isDeleting) showDeleteConfirmation = false },
            title = { Text("Eliminar orden") },
            text = { Text("Esta acción eliminará la orden mediante la API actual. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false; viewModel.deleteOrder(orderId) },
                    enabled = !state.isDeleting
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }, enabled = !state.isDeleting) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OrderDetailContent(
    order: Order,
    contentPadding: PaddingValues,
    clientName: String?,
    motorcycleLabel: String?,
    employeeLabel: String?,
    statuses: List<String>,
    isSaving: Boolean,
    isUpdatingStatus: Boolean,
    isDeleting: Boolean,
    operationMessage: String?,
    onStatusChange: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(contentPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BackNavigationLink(
            onClick = onBack,
            enabled = !isSaving && !isUpdatingStatus && !isDeleting
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Orden #${order.id}", style = MaterialTheme.typography.headlineSmall)
            FilterChip(
                selected = false,
                onClick = { statusMenuExpanded = true },
                enabled = !isSaving && !isDeleting && !isUpdatingStatus,
                label = {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(order.status.ifBlank { "Sin estado" })
                        if (isUpdatingStatus) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Text("▼", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            )
        }
        DropdownMenu(
            expanded = statusMenuExpanded,
            onDismissRequest = { statusMenuExpanded = false }
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = { statusMenuExpanded = false; onStatusChange(status) }
                )
            }
        }

        DetailLine("Cliente", clientName ?: order.clientId)
        DetailLine("Motocicleta", motorcycleLabel ?: order.motorcycleId)
        DetailLine("Técnico", employeeLabel ?: "Sin asignar")
        DetailLine("Descripción", order.description)
        OrderTotalsDetail(
            laborCost = order.laborCost ?: 0.0,
            servicesCost = order.servicesCost ?: 0.0,
            partsCost = order.partsCost ?: 0.0,
            discount = order.discount ?: 0.0,
            total = order.total
        )
        operationMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        Button(onClick = onEdit, enabled = !isSaving && !isUpdatingStatus && !isDeleting, modifier = Modifier.fillMaxWidth()) {
            Text("Editar orden")
        }
        Button(onClick = onDelete, enabled = !isSaving && !isUpdatingStatus && !isDeleting, modifier = Modifier.fillMaxWidth()) {
            Text("Eliminar orden")
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun OrderTotalsDetail(
    laborCost: Double,
    servicesCost: Double,
    partsCost: Double,
    discount: Double,
    total: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Totales", style = MaterialTheme.typography.titleMedium)
        DetailLine("Mano de obra", formatOrderMoney(laborCost))
        DetailLine("Servicios", formatOrderMoney(servicesCost))
        DetailLine("Repuestos", formatOrderMoney(partsCost))
        DetailLine("Descuento", "− ${formatOrderMoney(discount)}")
        DetailLine("Total", formatOrderMoney(total))
    }
}
