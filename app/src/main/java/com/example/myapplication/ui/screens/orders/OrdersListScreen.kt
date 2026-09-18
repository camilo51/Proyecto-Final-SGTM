package com.example.myapplication.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.Order
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.OrderStatus
import com.example.myapplication.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrdersListScreen(
    contentPadding: PaddingValues,
    onOpenOrder: (String) -> Unit,
    onCreateOrder: () -> Unit,
    onEditOrder: (String) -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val availableOrderStatuses = (state.statuses + OrderStatus.changeableValues).distinct()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
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
        OrdersHeader(
            onCreateOrder = onCreateOrder,
            isSaving = state.isSaving,
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refreshOrders
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Registradas",
                value = state.orders.size.toString(),
                icon = Icons.AutoMirrored.Filled.List
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Activas",
                value = state.orders.count(::isActiveOrder).toString(),
                icon = Icons.Filled.Build
            )
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar órdenes") },
            placeholder = { Text("OT, cliente, placa o motocicleta") },
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

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth >= 600.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusFilter(
                        modifier = Modifier.weight(1f),
                        selectedStatus = state.selectedStatus,
                        statuses = state.statuses,
                        onSelect = viewModel::onStatusFilterChange
                    )
                    TechnicianFilter(modifier = Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusFilter(
                        selectedStatus = state.selectedStatus,
                        statuses = state.statuses,
                        onSelect = viewModel::onStatusFilterChange
                    )
                    TechnicianFilter()
                }
            }
        }

        if (state.errorMessage != null && state.orders.isNotEmpty()) {
            InlineErrorMessage(
                message = state.errorMessage.orEmpty(),
                onRetry = viewModel::loadOrders
            )
        }

        when {
            state.isLoading && state.orders.isEmpty() -> LoadingMessage(Modifier)
            state.errorMessage != null && state.orders.isEmpty() -> ErrorMessage(
                message = state.errorMessage.orEmpty(),
                onRetry = viewModel::loadOrders
            )
            state.orders.isEmpty() -> EmptyMessage(
                message = "No hay órdenes registradas",
                actionLabel = "Crear nueva orden",
                onAction = onCreateOrder
            )
            state.filteredOrders.isEmpty() -> EmptyMessage(
                message = "No se encontraron órdenes con estos filtros",
                actionLabel = "Limpiar filtros",
                onAction = {
                    viewModel.onSearchQueryChange("")
                    viewModel.onStatusFilterChange(null)
                }
            )
            else -> state.filteredOrders.forEach { order ->
                OrderCard(
                    order = order,
                    client = state.clients.firstOrNull { it.id == order.clientId },
                    motorcycle = state.motorcycles.firstOrNull { it.id == order.motorcycleId },
                    onClick = { order.id?.let(onOpenOrder) },
                    onEdit = { order.id?.let(onEditOrder) },
                    statuses = (availableOrderStatuses + order.status).filter(String::isNotBlank).distinct(),
                    isUpdatingStatus = state.updatingOrderId == order.id,
                    onStatusChange = { status -> order.id?.let { viewModel.updateOrderStatus(it, status) } }
                )
            }
        }
    }
}

@Composable
private fun OrdersHeader(
    onCreateOrder: () -> Unit,
    isSaving: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = "MÓDULO ADMINISTRATIVO",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing
        )
        Text(
            text = "Órdenes de Trabajo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Control de órdenes de reparación y mantenimiento de motocicletas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onCreateOrder,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Nueva orden")
            }
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.weight(1f),
                enabled = !isRefreshing && !isSaving
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(if (isRefreshing) "Actualizando…" else "Actualizar")
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatusFilter(
    modifier: Modifier = Modifier,
    selectedStatus: String?,
    statuses: List<String>,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedStatus ?: "Todos los estados",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Todos los estados") },
                onClick = { onSelect(null); expanded = false }
            )
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = { onSelect(status); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun TechnicianFilter(modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.fillMaxWidth(),
        enabled = false
    ) {
        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(6.dp))
        Text("Técnicos no disponibles", maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun OrderCard(
    order: Order,
    client: Client?,
    motorcycle: Motorcycle?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    statuses: List<String>,
    isUpdatingStatus: Boolean,
    onStatusChange: (String) -> Unit
) {
    val clientLabel = client?.name?.takeIf(String::isNotBlank) ?: "Cliente ${order.clientId}"
    val motorcycleLabel = motorcycle?.let { "${it.brand} ${it.model} · ${it.plate}" }
        ?: "Motocicleta ${order.motorcycleId}"
    val orderLabel = order.orderNumber?.takeIf(String::isNotBlank) ?: "OT #${order.id ?: "—"}"
    val entryDateLabel = formatOrderEntryDate(order.entryDate)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = orderLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entryDateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                var statusMenuExpanded by remember(order.id) { mutableStateOf(false) }
                Box {
                    StatusBadge(
                        status = order.status,
                        isLoading = isUpdatingStatus,
                        enabled = !isUpdatingStatus,
                        onClick = { statusMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    statusMenuExpanded = false
                                    onStatusChange(status)
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = clientLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ReferenceLine(Icons.Filled.Build, motorcycleLabel)
            ReferenceLine(Icons.Filled.Person, "Técnico no disponible en el modelo actual")
            Text(
                text = order.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatOrderMoney(order.total),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClick) {
                        Icon(Icons.Filled.Info, contentDescription = "Ver orden")
                    }
                    IconButton(onClick = onEdit, enabled = !isUpdatingStatus) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar orden")
                    }
                }
            }
        }
    }
}

private fun formatOrderEntryDate(value: String?): String {
    val sourceDate = value
        ?.trim()
        ?.substringBefore('T')
        ?.substringBefore(' ')
        ?.takeIf(String::isNotBlank)
        ?: return "Fecha no disponible"
    val parsed = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }.parse(sourceDate)
    }.getOrNull() ?: return "Fecha no disponible"

    return SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"))
        .format(parsed)
}

@Composable
private fun StatusBadge(
    status: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = orderStatusColor(status)
    Surface(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status.ifBlank { "Sin estado" },
                color = color,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(13.dp), strokeWidth = 2.dp, color = color)
            } else {
                Text("▼", color = color, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ReferenceLine(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LoadingMessage(modifier: Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator()
            Text("Cargando órdenes…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InlineErrorMessage(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Verifica tu sesión o inténtalo nuevamente.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = onRetry) { Text("Reintentar") }
            }
        }
    }
}

@Composable
fun EmptyMessage(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Text(message, style = MaterialTheme.typography.bodyLarge)
                if (actionLabel != null && onAction != null) {
                    OutlinedButton(onClick = onAction) { Text(actionLabel) }
                }
            }
        }
    }
}

private fun isActiveOrder(order: Order): Boolean {
    val normalizedStatus = order.status.trim().lowercase(Locale.ROOT)
    return normalizedStatus.isNotBlank() &&
        normalizedStatus != "entregada" &&
        !normalizedStatus.contains("finaliz") &&
        !normalizedStatus.contains("complet")
}
