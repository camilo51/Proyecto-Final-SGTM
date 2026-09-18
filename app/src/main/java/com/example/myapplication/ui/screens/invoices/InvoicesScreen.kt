package com.example.myapplication.ui.screens.invoices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Invoice
import com.example.myapplication.data.model.Order
import com.example.myapplication.ui.screens.inventory.asReadableDate
import com.example.myapplication.ui.screens.orders.formatOrderMoney
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.InvoiceUiState
import com.example.myapplication.ui.viewmodel.InvoiceViewModel

@Composable
fun InvoicesScreen(
    contentPadding: PaddingValues,
    onOpenInvoice: (String) -> Unit,
    onCreateInvoice: () -> Unit,
    viewModel: InvoiceViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInvoices()
        viewModel.loadReferences()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InvoicesHeader(
            isSaving = state.isSaving,
            isRefreshing = state.isRefreshing,
            onCreate = {
                viewModel.clearOperationMessage()
                onCreateInvoice()
            },
            onRefresh = viewModel::refreshInvoices
        )
        InvoiceSummary(state)
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar facturas") },
            placeholder = { Text("Factura, orden, cliente o estado") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true,
            colors = AppOutlinedTextFieldColors()
        )

        if (state.errorMessage != null && state.invoices.isNotEmpty()) {
            InvoiceInlineError(state.errorMessage.orEmpty(), viewModel::loadInvoices)
        }

        when {
            state.isLoading && state.invoices.isEmpty() -> InvoiceLoading()
            state.errorMessage != null && state.invoices.isEmpty() -> InvoiceMessage(
                title = "No fue posible cargar las facturas",
                detail = state.errorMessage.orEmpty(),
                actionLabel = "Reintentar",
                onAction = viewModel::loadInvoices
            )
            state.invoices.isEmpty() -> InvoiceMessage(
                title = "No hay facturas registradas",
                detail = "Emite una factura desde una orden de trabajo registrada.",
                actionLabel = "Nueva factura",
                onAction = onCreateInvoice
            )
            state.filteredInvoices.isEmpty() -> InvoiceMessage(
                title = "No se encontraron facturas",
                detail = "Prueba con otra búsqueda.",
                actionLabel = "Limpiar búsqueda",
                onAction = { viewModel.onSearchQueryChange("") }
            )
            else -> state.filteredInvoices.forEach { invoice ->
                InvoiceCard(
                    invoice = invoice,
                    client = state.clients.firstOrNull { it.id == invoice.clientId },
                    order = state.orders.firstOrNull { it.id == invoice.orderId },
                    onOpen = { invoice.id?.let(onOpenInvoice) }
                )
            }
        }
        Spacer(Modifier.size(8.dp))
    }
}

@Composable
private fun InvoicesHeader(
    isSaving: Boolean,
    isRefreshing: Boolean,
    onCreate: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("MÓDULO ADMINISTRATIVO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Facturación", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Emite y consulta las facturas asociadas a órdenes de trabajo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onCreate, modifier = Modifier.weight(1f), enabled = !isSaving) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Nueva factura", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f), enabled = !isSaving && !isRefreshing) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(if (isRefreshing) "Actualizando..." else "Actualizar", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun InvoiceSummary(state: InvoiceUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        InvoiceMetric(Modifier.weight(1f), "Total pagado hoy", formatOrderMoney(state.paidTodayTotal))
        InvoiceMetric(Modifier.weight(1f), "Pendientes", formatOrderMoney(state.pendingTotal))
    }
}

@Composable
private fun InvoiceMetric(modifier: Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun InvoiceCard(invoice: Invoice, client: Client?, order: Order?, onOpen: () -> Unit) {
    val orderLabel = order?.orderNumber?.takeIf(String::isNotBlank) ?: invoice.orderId
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.padding(9.dp).size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(invoice.displayNumber(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(invoice.date.asReadableDate() ?: "Fecha no registrada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilterChip(selected = true, onClick = {}, enabled = false, label = { Text(invoice.paymentStatus.ifBlank { invoice.status }.ifBlank { "Pendiente" }, maxLines = 1, overflow = TextOverflow.Ellipsis) })
            }
            Text(client?.name?.takeIf(String::isNotBlank) ?: "Cliente ${invoice.clientId}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text("Orden: $orderLabel", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Modo de pago: ${invoice.paymentMethod.ifBlank { "No registrado" }}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            invoice.notes?.takeIf(String::isNotBlank)?.let { notes ->
                Text("Notas: $notes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatOrderMoney(invoice.total), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

fun Invoice.displayNumber(): String {
    invoiceNumber?.takeIf { it.startsWith("FAC-") }?.let { return it }
    val datePart = date?.take(10)?.replace("-", "") ?: "00000000"
    val sequence = (invoiceNumber.orEmpty() + id.orEmpty()).filter(Char::isDigit).takeLast(3).padStart(3, '0')
    return "FAC-$datePart-$sequence"
}

@Composable
private fun InvoiceLoading() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun InvoiceMessage(title: String, detail: String, actionLabel: String, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun InvoiceInlineError(message: String, onRetry: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    }
}
