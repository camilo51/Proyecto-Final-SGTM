package com.example.myapplication.ui.screens.invoices

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.screens.BackNavigationLink
import com.example.myapplication.ui.screens.OperationMessage
import com.example.myapplication.data.model.Invoice
import com.example.myapplication.ui.screens.inventory.asReadableDate
import com.example.myapplication.ui.screens.orders.formatOrderMoney
import com.example.myapplication.ui.viewmodel.InvoiceViewModel

@Composable
fun InvoiceDetailScreen(
    contentPadding: PaddingValues,
    invoiceId: String,
    onBack: () -> Unit,
    viewModel: InvoiceViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var confirmCancel by remember { mutableStateOf(false) }
    val invoice = state.selectedInvoice
    val openedUpdateVersion = remember(invoiceId) { state.invoiceUpdatedVersion }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null && invoice != null) writeInvoicePdf(context, uri, invoice)
    }

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
        viewModel.loadReferences()
    }
    LaunchedEffect(state.invoiceUpdatedVersion) {
        if (state.invoiceUpdatedVersion > openedUpdateVersion) onBack()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackNavigationLink(onClick = onBack)
        when {
            state.isLoadingDetail || invoice == null && state.errorMessage == null -> CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
            invoice == null -> DetailMessage(state.errorMessage ?: "No fue posible cargar la factura", viewModel::loadInvoice, invoiceId)
            else -> {
                val client = state.clients.firstOrNull { it.id == invoice.clientId }
                val order = state.orders.firstOrNull { it.id == invoice.orderId }
                Text(invoice.displayNumber(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(invoice.date.asReadableDate() ?: "Fecha no registrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
                InvoiceDetailCard("Cliente", client?.name?.takeIf(String::isNotBlank) ?: invoice.clientId)
                InvoiceDetailCard("Orden de trabajo", order?.orderNumber?.takeIf(String::isNotBlank) ?: invoice.orderId)
                InvoiceDetailCard("Modo de pago", invoice.paymentMethod.ifBlank { "No registrado" })
                InvoiceDetailCard("Estado", invoice.paymentStatus.ifBlank { invoice.status }.ifBlank { "Pendiente" })
                invoice.notes?.takeIf(String::isNotBlank)?.let { InvoiceDetailCard("Notas", it) }
                Text(formatOrderMoney(invoice.total), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Button(onClick = { pdfLauncher.launch("${invoice.displayNumber()}.pdf") }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Descargar PDF")
                }
                if (!invoice.isPaid() && !invoice.isCancelled()) {
                    Button(onClick = viewModel::payInvoice, modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving) { Text("Marcar como pagada") }
                }
                if (!invoice.isCancelled()) {
                    OutlinedButton(onClick = { confirmCancel = true }, modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving) { Text("Anular factura") }
                }
                state.operationMessage?.let { message ->
                    OperationMessage(message)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }

    if (confirmCancel && invoice != null) {
        AlertDialog(
            onDismissRequest = { if (!state.isSaving) confirmCancel = false },
            title = { Text("Anular factura") },
            text = { Text("Esta acción cambiará el estado de ${invoice.displayNumber()} a anulada.") },
            confirmButton = { Button(onClick = { confirmCancel = false; viewModel.cancelInvoice() }, enabled = !state.isSaving) { Text("Anular") } },
            dismissButton = { TextButton(onClick = { confirmCancel = false }, enabled = !state.isSaving) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun InvoiceDetailCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DetailMessage(message: String, onRetry: (String) -> Unit, invoiceId: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            androidx.compose.material3.Icon(Icons.Filled.Info, contentDescription = null)
            Text(message)
            OutlinedButton(onClick = { onRetry(invoiceId) }) { Text("Reintentar") }
        }
    }
}

private fun Invoice.isPaid(): Boolean = listOf(paymentStatus, status).any { value ->
    value.lowercase().contains("pagad") || value.lowercase().contains("paid") || value.lowercase().contains("complet")
}

private fun Invoice.isCancelled(): Boolean = listOf(paymentStatus, status).any { it.lowercase().contains("anulad") || it.lowercase().contains("cancel") }

private fun writeInvoicePdf(context: Context, uri: Uri, invoice: Invoice) {
    val document = PdfDocument()
    try {
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = 20f
        paint.isFakeBoldText = true
        page.canvas.drawText(invoice.displayNumber(), 48f, 64f, paint)
        paint.isFakeBoldText = false
        paint.textSize = 12f
        val rows = listOf(
            "Fecha: ${invoice.date.asReadableDate() ?: "No registrada"}",
            "Orden de trabajo: ${invoice.orderId}",
            "Modo de pago: ${invoice.paymentMethod.ifBlank { "No registrado" }}",
            "Estado: ${invoice.paymentStatus.ifBlank { invoice.status }.ifBlank { "Pendiente" }}",
            "Notas: ${invoice.notes ?: "Sin notas"}",
            "Total: ${formatOrderMoney(invoice.total)}"
        )
        rows.forEachIndexed { index, row -> page.canvas.drawText(row, 48f, 120f + index * 32f, paint) }
        document.finishPage(page)
        context.contentResolver.openOutputStream(uri)?.use(document::writeTo)
        Toast.makeText(context, "Factura PDF guardada", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        Toast.makeText(context, "No se pudo generar el PDF", Toast.LENGTH_SHORT).show()
    } finally {
        document.close()
    }
}
