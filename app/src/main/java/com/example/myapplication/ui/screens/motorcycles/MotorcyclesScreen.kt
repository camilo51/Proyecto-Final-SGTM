package com.example.myapplication.ui.screens.motorcycles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.MotorcycleStatus
import com.example.myapplication.ui.viewmodel.MotorcycleUiState
import com.example.myapplication.ui.viewmodel.MotorcycleViewModel

@Composable
fun MotorcyclesScreen(
    contentPadding: PaddingValues,
    onOpenMotorcycle: (String) -> Unit,
    onCreateMotorcycle: () -> Unit,
    onEditMotorcycle: (String) -> Unit,
    viewModel: MotorcycleViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMotorcycles() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MotorcycleHeader(
            isSaving = state.isSaving,
            isRefreshing = state.isRefreshing,
            onCreate = onCreateMotorcycle,
            onRefresh = viewModel::refreshMotorcycles
        )
        MotorcycleSummary(state)

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar motocicletas") },
            placeholder = { Text("Placa, marca, modelo o propietario") },
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

        MotorcycleFilters(state, viewModel)

        state.errorMessage?.let { message ->
            if (state.motorcycles.isNotEmpty()) {
                InlineMotorcycleMessage(message, onRetry = viewModel::refreshMotorcycles)
            }
        }

        when {
            state.isLoading && state.motorcycles.isEmpty() -> MotorcycleLoading()
            state.errorMessage != null && state.motorcycles.isEmpty() -> InlineMotorcycleMessage(
                message = state.errorMessage.orEmpty(),
                onRetry = viewModel::loadMotorcycles
            )
            state.motorcycles.isEmpty() -> MotorcycleEmpty(
                message = "No hay motocicletas registradas",
                actionLabel = "Nueva motocicleta",
                onAction = onCreateMotorcycle
            )
            state.filteredMotorcycles.isEmpty() -> MotorcycleEmpty(
                message = "No se encontraron motocicletas",
                actionLabel = "Limpiar filtros",
                onAction = {
                    viewModel.onSearchQueryChange("")
                    viewModel.onStatusFilterChange(null)
                    viewModel.onBrandFilterChange(null)
                }
            )
            else -> state.filteredMotorcycles.forEach { motorcycle ->
                MotorcycleCard(
                    motorcycle = motorcycle,
                    owner = state.clients.firstOrNull { it.id == motorcycle.clientId },
                    onOpen = { motorcycle.id?.let(onOpenMotorcycle) },
                    onEdit = { motorcycle.id?.let(onEditMotorcycle) }
                )
            }
        }
    }
}

@Composable
private fun MotorcycleHeader(
    isSaving: Boolean,
    isRefreshing: Boolean,
    onCreate: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = "MÓDULO ADMINISTRATIVO",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text("Motocicletas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Gestión de motocicletas registradas en el taller.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(onClick = onCreate, modifier = Modifier.weight(1.2f), enabled = !isSaving) {
                Text("+ Nueva motocicleta", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.weight(1f),
                enabled = !isRefreshing && !isSaving
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(if (isRefreshing) "Actualizando…" else "Actualizar", modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

@Composable
private fun MotorcycleSummary(state: MotorcycleUiState) {
    val summaries = listOf(
        "Registradas" to state.motorcycles.size,
        "En servicio" to state.motorcycles.count { it.status == MotorcycleStatus.IN_SERVICE },
        "En reparación" to state.motorcycles.count { it.status == MotorcycleStatus.IN_REPAIR },
        "Listas para entrega" to state.motorcycles.count { it.status == MotorcycleStatus.READY_FOR_DELIVERY },
        "Entregadas" to state.motorcycles.count { it.status == MotorcycleStatus.DELIVERED }
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(summaries) { (label, value) ->
            Card(
                modifier = Modifier.size(width = 132.dp, height = 72.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MotorcycleFilters(state: MotorcycleUiState, viewModel: MotorcycleViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MotorcycleFilterMenu(
            label = "Estado",
            selectedLabel = state.selectedStatus ?: "Todos",
            options = listOf("Todos") + MotorcycleStatus.values,
            modifier = Modifier.weight(1f),
            onSelected = { viewModel.onStatusFilterChange(it.takeIf { value -> value != "Todos" }) }
        )
        if (state.availableBrands.size > 1) {
            MotorcycleFilterMenu(
                label = "Marca",
                selectedLabel = state.selectedBrand ?: "Todas",
                options = listOf("Todas") + state.availableBrands,
                modifier = Modifier.weight(1f),
                onSelected = { viewModel.onBrandFilterChange(it.takeIf { value -> value != "Todas" }) }
            )
        }
    }
}

@Composable
private fun MotorcycleFilterMenu(
    label: String,
    selectedLabel: String,
    options: List<String>,
    modifier: Modifier,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selectedLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { expanded = false; onSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun MotorcycleCard(
    motorcycle: Motorcycle,
    owner: Client?,
    onOpen: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(motorcycle.plate ?: "Sin placa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        listOfNotNull(motorcycle.brand, motorcycle.model).joinToString(" ").ifBlank { "Motocicleta sin descripción" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(motorcycle.year?.toString() ?: "Año no registrado", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                MotorcycleStatusChip(motorcycle.status)
            }
            Text(
                "Propietario: ${owner?.name?.takeIf(String::isNotBlank) ?: "Sin propietario"}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onOpen, enabled = motorcycle.id != null) { Text("Ver") }
                TextButton(onClick = onEdit, enabled = motorcycle.id != null) { Text("Editar") }
            }
        }
    }
}

@Composable
fun MotorcycleStatusChip(status: String?) {
    FilterChip(
        selected = true,
        onClick = {},
        enabled = false,
        label = { Text(status ?: "No registrado", maxLines = 1, overflow = TextOverflow.Ellipsis) }
    )
}

@Composable
private fun MotorcycleLoading() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MotorcycleEmpty(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(message, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
private fun InlineMotorcycleMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    }
}
