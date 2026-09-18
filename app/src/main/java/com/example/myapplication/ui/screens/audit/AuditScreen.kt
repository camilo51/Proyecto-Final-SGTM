package com.example.myapplication.ui.screens.audit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.AuditLog
import com.example.myapplication.data.model.User
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.AuditUiState
import com.example.myapplication.ui.viewmodel.AuditViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun AuditScreen(
    contentPadding: PaddingValues,
    viewModel: AuditViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val shouldLoadMore by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: 0
            layout.totalItemsCount > 0 && lastVisible >= layout.totalItemsCount - 2
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }
    LaunchedEffect(shouldLoadMore, state.hasMore) {
        if (shouldLoadMore && state.hasMore) viewModel.loadNextPage()
    }

    if (showFilters) {
        AuditFiltersSheet(
            state = state,
            onDismiss = { showFilters = false },
            onFromDateChange = viewModel::onFromDateChange,
            onToDateChange = viewModel::onToDateChange,
            onUserChange = viewModel::onUserFilterChange,
            onActionChange = viewModel::onActionFilterChange,
            onTableChange = viewModel::onTableFilterChange,
            onApply = {
                if (viewModel.applyFilters()) showFilters = false
            },
            onClear = {
                viewModel.clearFilters()
                showFilters = false
            }
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AuditHeader(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh
            )
        }
        item {
            AuditSummary(totalRecords = state.totalRecords, isLoading = state.isLoading)
        }
        item {
            AuditSearch(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearch = viewModel::applyFilters,
                onClear = { viewModel.onSearchQueryChange("") }
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showFilters = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (state.hasActiveFilters) "Filtros aplicados" else "Filtros",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (state.hasActiveFilters) {
                    TextButton(onClick = viewModel::clearFilters) {
                        Text("Limpiar")
                    }
                }
            }
        }

        when {
            state.isLoading && state.auditLogs.isEmpty() -> item { AuditLoading() }
            state.errorMessage != null && state.auditLogs.isEmpty() -> item {
                AuditMessage(
                    message = state.errorMessage.orEmpty(),
                    actionLabel = "Reintentar",
                    onAction = viewModel::refresh
                )
            }
            state.auditLogs.isEmpty() -> item {
                AuditMessage(
                    message = if (state.hasActiveFilters) {
                        "No se encontraron registros con estos filtros."
                    } else {
                        "No hay registros de auditoría."
                    },
                    actionLabel = if (state.hasActiveFilters) "Limpiar filtros" else null,
                    onAction = if (state.hasActiveFilters) viewModel::clearFilters else null
                )
            }
            else -> {
                state.errorMessage?.let { message ->
                    item {
                        AuditMessage(
                            message = message,
                            actionLabel = "Reintentar",
                            onAction = viewModel::refresh,
                            compact = true
                        )
                    }
                }
                items(
                    items = state.auditLogs,
                    key = { log -> log.id ?: "${log.createdAt}-${log.action}-${log.description}" }
                ) { log ->
                    AuditLogCard(log)
                }
                if (state.isLoadingMore) {
                    item { AuditLoading(compact = true) }
                } else if (!state.hasMore) {
                    item {
                        Text(
                            "No hay más registros.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditHeader(isRefreshing: Boolean, onRefresh: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = "MÓDULO ADMINISTRATIVO",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Auditoría",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Consulta las acciones registradas en el sistema.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRefreshing
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                if (isRefreshing) "Actualizando…" else "Actualizar",
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun AuditSummary(totalRecords: Int, isLoading: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("REGISTROS DE AUDITORÍA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(
                if (isLoading) "Cargando…" else "$totalRecords registros",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AuditSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Boolean,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Buscar auditoría") },
        placeholder = { Text("Usuario, descripción, acción o módulo") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotBlank()) {
                Row {
                    IconButton(onClick = { onSearch() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            }
        },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSearch() }),
        colors = AppOutlinedTextFieldColors()
    )
}

@Composable
private fun AuditLogCard(log: AuditLog) {
    val action = log.action.orEmpty()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                text = formatAuditDate(log.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(log.userName?.takeIf { it.isNotBlank() } ?: "Usuario no registrado", fontWeight = FontWeight.SemiBold)
                    log.role?.takeIf { it.isNotBlank() }?.let { role ->
                        Text(role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                AuditActionChip(action)
            }
            log.tableName?.takeIf { it.isNotBlank() }?.let { table ->
                AuditDetail("Módulo", moduleLabel(table, log.recordId))
            }
            log.description?.takeIf { it.isNotBlank() }?.let { description ->
                AuditDetail("Descripción", description)
            }
            log.ipAddress?.takeIf { it.isNotBlank() }?.let { address ->
                AuditDetail("IP", address)
            }
        }
    }
}

@Composable
private fun AuditActionChip(action: String) {
    val colors = when {
        action.startsWith("ELIMINAR") || action.endsWith("FALLIDO") || action == "IP_BLOQUEADA" -> {
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                labelColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        action.startsWith("CREAR") || action.endsWith("EXITOSO") -> {
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        else -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(
                actionLabel(action),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = colors
    )
}

@Composable
private fun AuditDetail(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$label:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AuditLoading(compact: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 8.dp else 36.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(if (compact) 28.dp else 40.dp))
    }
}

@Composable
private fun AuditMessage(
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 14.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (actionLabel != null && onAction != null) {
                OutlinedButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuditFiltersSheet(
    state: AuditUiState,
    onDismiss: () -> Unit,
    onFromDateChange: (String?) -> Unit,
    onToDateChange: (String?) -> Unit,
    onUserChange: (String?) -> Unit,
    onActionChange: (String?) -> Unit,
    onTableChange: (String?) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Filtros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Refina los registros por fecha, usuario, acción o módulo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            AuditDateFilter("Desde", state.fromDate, onFromDateChange)
            AuditDateFilter("Hasta", state.toDate, onToDateChange)
            AuditFilterMenu(
                label = "Usuario",
                selectedLabel = state.users.firstOrNull { it.id == state.selectedUserId }?.let(::userLabel) ?: "Todos",
                options = listOf(AuditFilterOption(null, "Todos")) + state.users.map { user ->
                    AuditFilterOption(user.id, userLabel(user))
                },
                onSelected = onUserChange
            )
            AuditFilterMenu(
                label = "Acción",
                selectedLabel = state.selectedAction?.let(::actionLabel) ?: "Todas",
                options = listOf(AuditFilterOption(null, "Todas")) + state.actions.map { action ->
                    AuditFilterOption(action, actionLabel(action))
                },
                onSelected = onActionChange
            )
            AuditFilterMenu(
                label = "Módulo",
                selectedLabel = state.selectedTable?.let { table -> moduleLabel(table, null) } ?: "Todos",
                options = listOf(AuditFilterOption(null, "Todos")) + state.tables.map { table ->
                    AuditFilterOption(table, moduleLabel(table, null))
                },
                onSelected = onTableChange
            )
            Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                Text("Aplicar filtros")
            }
            if (state.hasActiveFilters) {
                TextButton(onClick = onClear, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Limpiar filtros")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuditDateFilter(label: String, value: String?, onDateChange: (String?) -> Unit) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    if (showPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = value?.toUtcMillisOrNull())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis -> onDateChange(millis.toIsoDate()) }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(value ?: "Seleccionar fecha", modifier = Modifier.padding(start = 6.dp))
            }
            if (value != null) {
                IconButton(onClick = { onDateChange(null) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Quitar fecha $label")
                }
            }
        }
    }
}

@Composable
private fun AuditFilterMenu(
    label: String,
    selectedLabel: String,
    options: List<AuditFilterOption>,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedLabel, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            expanded = false
                            onSelected(option.value)
                        }
                    )
                }
            }
        }
    }
}

private data class AuditFilterOption(val value: String?, val label: String)

private fun userLabel(user: User): String = buildString {
    append(user.name)
    user.role?.takeIf { it.isNotBlank() }?.let { role -> append(" · ").append(role) }
}

private fun actionLabel(action: String): String = when (action) {
    "CREAR_CLIENTE" -> "Crear cliente"
    "EDITAR_CLIENTE" -> "Editar cliente"
    "ELIMINAR_CLIENTE" -> "Eliminar cliente"
    "CREAR_MOTO" -> "Crear motocicleta"
    "EDITAR_MOTO" -> "Editar motocicleta"
    "ELIMINAR_MOTO" -> "Eliminar motocicleta"
    "CREAR_ORDEN" -> "Crear orden"
    "CAMBIAR_ESTADO" -> "Cambiar estado"
    "EDITAR_INVENTARIO" -> "Editar inventario"
    "ELIMINAR_REPUESTO" -> "Eliminar repuesto"
    "CREAR_EMPLEADO" -> "Crear empleado"
    "CREAR_MARCA" -> "Crear marca"
    "EDITAR_MARCA" -> "Editar marca"
    "ELIMINAR_MARCA" -> "Eliminar marca"
    "LOGIN_EXITOSO" -> "Login exitoso"
    "LOGIN_FALLIDO" -> "Login fallido"
    "IP_BLOQUEADA" -> "IP bloqueada"
    "LOGOUT" -> "Cierre de sesión"
    "CAMBIO_CONTRASENA" -> "Cambio de contraseña"
    "RECUPERACION_CONTRASENA" -> "Recuperar clave"
    else -> action.replace('_', ' ').lowercase(SPANISH_COLOMBIA).replaceFirstChar { it.titlecase(SPANISH_COLOMBIA) }
}

private fun moduleLabel(table: String, recordId: String?): String {
    val label = when (table) {
        "clients" -> "Clientes"
        "motorcycles" -> "Motocicletas"
        "orders" -> "Órdenes"
        "inventory" -> "Inventario"
        "employees" -> "Empleados"
        "brands" -> "Marcas"
        else -> table
    }
    return recordId?.takeIf { it.isNotBlank() }?.let { "$label #$it" } ?: label
}

private fun formatAuditDate(value: String?): String {
    if (value.isNullOrBlank()) return "Fecha no registrada"
    val date = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(value)
            }.getOrNull()
        }
        ?: return value
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SPANISH_COLOMBIA).format(date)
}

private fun String.toUtcMillisOrNull(): Long? = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }.parse(this)?.time
}.getOrNull()

private fun Long.toIsoDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}.format(Date(this))

private val SPANISH_COLOMBIA: Locale = Locale.forLanguageTag("es-CO")
