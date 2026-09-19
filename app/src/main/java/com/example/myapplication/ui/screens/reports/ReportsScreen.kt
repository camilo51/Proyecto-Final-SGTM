package com.example.myapplication.ui.screens.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Report
import com.example.myapplication.data.model.RevenuePeriod
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.ui.screens.inventory.InventoryStatusChip
import com.example.myapplication.ui.screens.inventory.asCurrency
import com.example.myapplication.ui.viewmodel.ReportViewModel

@Composable
fun ReportsScreen(
    contentPadding: PaddingValues,
    onOpenInventory: (Long) -> Unit,
    viewModel: ReportViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ReportsHeader(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh
        )

        when {
            state.isLoading && state.report == null -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
            }
            state.report == null -> {
                ReportsMessage(
                    title = "No fue posible cargar los reportes",
                    detail = state.errorMessage ?: "Inténtalo nuevamente.",
                    actionLabel = "Reintentar",
                    onAction = viewModel::refresh
                )
            }
            else -> {
                val report = state.report ?: return@Column
                RevenueSummary(report)
                ReportMetrics(report)
                InventoryValueSummary(report)
                TopSellingParts(report.topSellingParts, onOpenInventory)
                StockParts(
                    title = "Stock agotados",
                    emptyMessage = "No hay repuestos agotados.",
                    parts = report.outOfStockParts,
                    onOpenInventory = onOpenInventory
                )
                StockParts(
                    title = "Stock bajos",
                    emptyMessage = "No hay repuestos con stock bajo.",
                    parts = report.lowStockParts,
                    onOpenInventory = onOpenInventory
                )
                state.errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ReportsHeader(isRefreshing: Boolean, onRefresh: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = "Módulo administrativo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Reportes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ganancias, existencias, alertas y valor del inventario.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth(), enabled = !isRefreshing) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(if (isRefreshing) "Actualizando..." else "Actualizar reportes")
        }
    }
}

@Composable
private fun RevenueSummary(report: Report) {
    ReportSection("Ganancias por periodo") {
        RevenuePeriodCard("Diarias · Hoy", report.dailyRevenue)
        RevenuePeriodCard("Quincenales · Últimos 15 días", report.fortnightRevenue)
        RevenuePeriodCard("Mensuales · Mes actual", report.monthlyRevenue)
        report.financialError?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RevenuePeriodCard(title: String, revenue: RevenuePeriod) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            ReportAmount("Ventas", revenue.sales.asCurrency())
            ReportAmount("Órdenes de trabajo", revenue.workOrders.asCurrency())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ReportAmount("Total", revenue.total.asCurrency(), emphasize = true)
        }
    }
}

@Composable
private fun ReportMetrics(report: Report) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReportMetricCard(
                modifier = Modifier.weight(1f),
                value = report.totalReferences.toString(),
                label = "Repuestos",
                icon = Icons.Filled.Build
            )
            ReportMetricCard(
                modifier = Modifier.weight(1f),
                value = report.totalUnits.toString(),
                label = "Existencias",
                icon = Icons.Filled.ShoppingCart
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReportMetricCard(
                modifier = Modifier.weight(1f),
                value = report.outOfStockCount.toString(),
                label = "Stock agotados",
                icon = Icons.Filled.Warning
            )
            ReportMetricCard(
                modifier = Modifier.weight(1f),
                value = report.lowStockCount.toString(),
                label = "Stock bajos",
                icon = Icons.Filled.Warning
            )
        }
    }
}

@Composable
private fun ReportMetricCard(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InventoryValueSummary(report: Report) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Valor del inventario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            ReportAmount("Costo estimado", report.inventoryCost.asCurrency())
            ReportAmount("Venta potencial", report.potentialSales.asCurrency(), emphasize = true)
        }
    }
}

@Composable
private fun ReportAmount(label: String, amount: String, emphasize: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            amount,
            fontWeight = FontWeight.Bold,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TopSellingParts(parts: List<InventoryDto>, onOpenInventory: (Long) -> Unit) {
    ReportSection("Repuestos con mayor salida") {
        if (parts.isEmpty()) {
            Text("No hay repuestos para resumir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            parts.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenInventory(item.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name ?: "Repuesto sin nombre",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${item.soldCount} salidas registradas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        InventoryStatusChip(item.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun StockParts(
    title: String,
    emptyMessage: String,
    parts: List<InventoryDto>,
    onOpenInventory: (Long) -> Unit
) {
    ReportSection(title) {
        if (parts.isEmpty()) {
            Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            parts.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenInventory(item.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name ?: "Repuesto sin nombre",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Existencias: ${item.quantity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        InventoryStatusChip(item.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun ReportsMessage(title: String, detail: String, actionLabel: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
