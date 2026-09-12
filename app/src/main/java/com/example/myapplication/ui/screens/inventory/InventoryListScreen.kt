package com.example.myapplication.ui.screens.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.inventory.InventorySort
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.viewmodel.inventory.InventoryListUiState
import com.example.myapplication.ui.viewmodel.inventory.InventoryListViewModel
import kotlinx.coroutines.delay

@Composable
fun InventoryListScreen(
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    viewModel: InventoryListViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.query.search) {
        delay(350)
        viewModel.search()
    }

    AppScaffold(
        title = "Inventario",
        navController = navController,
        isAdmin = true,
        userName = userName,
        onLogout = onLogout
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Repuestos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Gestión administrativa de existencias", color = MaterialTheme.colorScheme.onSurfaceVariant)
                InventoryListActions(
                    onCreate = { navController.navigate("inventory/create") },
                    onRefresh = viewModel::refresh
                )
            }

            OutlinedTextField(
                value = state.query.search,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar por nombre o código") },
                singleLine = true
            )
            InventoryFilters(state, viewModel)
            InventoryAlertsSummary(
                lowStock = state.alerts.lowStockCount,
                outOfStock = state.alerts.outOfStockCount
            )

            when {
                state.isLoading && state.items.isEmpty() -> CircularProgressIndicator()
                state.errorMessage != null && state.items.isEmpty() -> InventoryMessage(
                    title = "No fue posible cargar el inventario",
                    detail = state.errorMessage ?: "",
                    actionLabel = "Reintentar",
                    onAction = viewModel::refresh
                )
                state.items.isEmpty() -> InventoryMessage(
                    title = "No hay repuestos",
                    detail = "Ajusta los filtros o registra el primer repuesto.",
                    actionLabel = "Crear repuesto",
                    onAction = { navController.navigate("inventory/create") }
                )
                else -> state.items.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("inventory/detail/${item.id}") },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(Modifier.padding(18.dp)) { InventorySummary(item) }
                    }
                }
            }
            state.errorMessage?.takeIf { state.items.isNotEmpty() }?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            InventoryPagination(
                page = state.pagination.page,
                totalPages = state.pagination.totalPages,
                onPrevious = { viewModel.goToPage(state.pagination.page - 1) },
                onNext = { viewModel.goToPage(state.pagination.page + 1) }
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InventoryFilters(state: InventoryListUiState, viewModel: InventoryListViewModel) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DropdownFilter("Categoría", state.query.category, state.categories, viewModel::setCategory)
        DropdownFilter("Marca", state.query.brand, state.brands, viewModel::setBrand)
        DropdownFilter("Estado", state.query.status, listOf("Disponible", "Stock bajo", "Agotado"), viewModel::setStatus)
        DropdownFilter(
            "Ordenar", state.query.sort.label, InventorySort.entries.map { it.label },
            onSelected = { label -> InventorySort.entries.firstOrNull { it.label == label }?.let(viewModel::setSort) },
            allowClear = false
        )
    }
}

@Composable
private fun DropdownFilter(
    label: String,
    selected: String?,
    options: List<String>,
    onSelected: (String?) -> Unit,
    allowClear: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { Text("$label: ${selected ?: "Todos"}") }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allowClear) {
                DropdownMenuItem(text = { Text("Todos") }, onClick = {
                    onSelected(null)
                    expanded = false
                })
            }
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun InventoryAlertsSummary(lowStock: Int, outOfStock: Int) {
    Text("Alertas: $lowStock con stock bajo · $outOfStock agotados", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun InventoryListActions(onCreate: () -> Unit, onRefresh: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (maxWidth >= 220.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCreate) { Text("Nuevo") }
                OutlinedButton(onClick = onRefresh) { Text("Actualizar") }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Text("Nuevo") }
                OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("Actualizar")
                }
            }
        }
    }
}

@Composable
private fun InventoryPagination(page: Int, totalPages: Int, onPrevious: () -> Unit, onNext: () -> Unit) {
    if (totalPages > 1) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            OutlinedButton(onClick = onPrevious, enabled = page > 1) { Text("Anterior") }
            Spacer(Modifier.width(12.dp))
            Text("Página $page de $totalPages", modifier = Modifier.padding(top = 12.dp))
            Spacer(Modifier.width(12.dp))
            OutlinedButton(onClick = onNext, enabled = page < totalPages) { Text("Siguiente") }
        }
    }
}
