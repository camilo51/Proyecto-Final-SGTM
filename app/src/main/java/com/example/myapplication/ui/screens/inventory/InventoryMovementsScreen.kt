package com.example.myapplication.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.inventory.InventoryMovementDto
import com.example.myapplication.data.model.inventory.StockMovementAction
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.viewmodel.inventory.InventoryMovementsViewModel
import java.util.Locale
import kotlin.math.abs

@Composable
fun InventoryMovementsScreen(
    id: Long,
    initialAction: String,
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    viewModel: InventoryMovementsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedAction by remember(initialAction) {
        mutableStateOf(StockMovementAction.entries.firstOrNull { it.apiValue == initialAction })
    }
    LaunchedEffect(id) { viewModel.load(id) }
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            selectedAction = null
            viewModel.consumeSuccess()
        }
    }

    AppScaffold("Movimientos de stock", navController, isAdmin = true, userName = userName, onLogout = onLogout) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { InventoryBackLink(onClick = navController::popBackStack) }
            state.item?.let { item ->
                item { InventorySummary(item) }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Registrar movimiento", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StockMovementAction.entries.forEach { action ->
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { selectedAction = action }
                            ) { Text(action.label, maxLines = 1) }
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Historial (${state.movements.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (state.isLoading && state.movements.isEmpty()) {
                item { CircularProgressIndicator() }
            } else if (state.movements.isEmpty()) {
                item {
                    Text(
                        text = "A\u00fan no hay movimientos registrados.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.movements, key = { it.id }) { movement ->
                    InventoryMovementCard(movement)
                }
            }
            state.errorMessage?.let { error ->
                item { Text(error, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
    selectedAction?.let { action ->
        StockMovementDialog(
            action = action,
            stock = state.item?.quantity ?: 0,
            isSubmitting = state.isSubmitting,
            onDismiss = { selectedAction = null },
            onConfirm = { quantity, notes -> viewModel.submit(action, quantity, notes, state.item?.quantity ?: 0) }
        )
    }
}

@Composable
private fun InventoryMovementCard(movement: InventoryMovementDto) {
    val type = movement.movementType?.ifBlank { null } ?: "Movimiento"
    val normalizedType = type.lowercase(Locale.ROOT)
    val isOutput = normalizedType.contains("salida")
    val isAdjustment = normalizedType.contains("ajuste")
    val amount = when {
        isAdjustment -> movement.quantity.toString()
        isOutput -> "-${abs(movement.quantity)}"
        else -> "+${abs(movement.quantity)}"
    }
    val amountColor = if (isOutput) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(type, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = movement.createdAt.asReadableDate() ?: "Fecha no informada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                movement.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Surface(
                color = amountColor.copy(alpha = 0.12f),
                contentColor = amountColor,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = amount,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StockMovementDialog(
    action: StockMovementAction,
    stock: Int,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var quantity by remember(action) { mutableStateOf("") }
    var notes by remember(action) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${action.label} de stock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (action == StockMovementAction.ADJUSTMENT) "Indica la nueva cantidad absoluta." else "Stock visible: $stock")
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Cantidad") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Motivo obligatorio") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(quantity, notes) }, enabled = !isSubmitting) {
                Text(if (isSubmitting) "Procesando..." else "Registrar")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Cancelar") } }
    )
}
