package com.example.myapplication.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.myapplication.data.model.inventory.StockMovementAction
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.screens.BackLink
import com.example.myapplication.ui.viewmodel.inventory.InventoryDetailViewModel

@Composable
fun InventoryDetailScreen(
    id: Long,
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    viewModel: InventoryDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    LaunchedEffect(id) { viewModel.load(id) }
    LaunchedEffect(state.deletionCompleted) {
        if (state.deletionCompleted) {
            viewModel.consumeDeletion()
            navController.popBackStack()
        }
    }

    AppScaffold("Detalle de repuesto", navController, isAdmin = true, userName = userName, onLogout = onLogout) { padding ->
        when {
            state.isLoading && state.item == null -> CircularProgressIndicator(modifier = Modifier.padding(padding))
            state.item == null -> InventoryMessage(
                title = "Repuesto no disponible",
                detail = state.errorMessage ?: "No se encontró el repuesto solicitado.",
                actionLabel = "Volver",
                onAction = { navController.popBackStack() },
                modifier = Modifier.padding(padding).padding(16.dp)
            )
            else -> {
                val item = state.item ?: return@AppScaffold
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BackLink(onClick = { navController.popBackStack() })
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) { InventorySummary(item) }
                    }
                    DetailSection("Información general") {
                        DetailLine("Proveedor", item.supplier)
                        DetailLine("Descripción", item.description)
                        DetailLine("Stock m\u00ednimo", item.minStock.toString())
                        DetailLine("Creado", item.createdAt.asReadableDate())
                        DetailLine("Actualizado", item.updatedAt.asReadableDate())
                    }
                    DetailSection("Acciones administrativas") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("inventory/edit/${item.id}") }
                            ) { Text("Editar") }
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("inventory/movements/${item.id}/history") }
                            ) { Text("Historial") }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Registrar movimiento", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navController.navigate("inventory/movements/${item.id}/${StockMovementAction.ENTRY.apiValue}") }
                            ) { Text("Registrar entrada") }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { navController.navigate("inventory/movements/${item.id}/${StockMovementAction.OUTPUT.apiValue}") }
                                ) { Text("Registrar salida") }
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { navController.navigate("inventory/movements/${item.id}/${StockMovementAction.ADJUSTMENT.apiValue}") }
                                ) { Text("Ajustar stock") }
                            }
                        }
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showDeleteDialog = true },
                            enabled = !state.isDeleting
                        ) {
                            Text(if (state.isDeleting) "Eliminando..." else "Eliminar repuesto", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    DetailSection("Movimientos recientes") {
                        if (state.recentMovements.isEmpty()) Text("No hay movimientos registrados.")
                        state.recentMovements.forEach { movement ->
                            Text("${movement.movementType ?: "Movimiento"}: ${movement.quantity} · ${movement.notes ?: "Sin motivo"}")
                        }
                    }
                    if (state.alerts.alerts.isNotEmpty()) {
                        DetailSection("Alertas") {
                            state.alerts.alerts.take(3).forEach { alert -> Text(alert.message ?: alert.status ?: "Alerta de inventario") }
                        }
                    }
                    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
    if (showDeleteDialog) {
        DeleteInventoryDialog(
            isDeleting = state.isDeleting,
            onDismiss = { showDeleteDialog = false },
            onConfirm = viewModel::delete
        )
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun DeleteInventoryDialog(isDeleting: Boolean, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar repuesto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Esta acción administrativa se enviará al servidor. Puede ser rechazada si existe historial.")
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo obligatorio") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(reason) }, enabled = !isDeleting && reason.trim().length >= 3) {
                Text("Confirmar eliminación")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss, enabled = !isDeleting) { Text("Cancelar") } }
    )
}
