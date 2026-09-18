package com.example.myapplication.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.ui.viewmodel.OrderValidator
import com.example.myapplication.ui.viewmodel.OrderViewModel
import com.example.myapplication.ui.viewmodel.OrderStatus

@Composable
fun CreateOrderScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onOrderCreated: (String) -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.resetOrderDraft()
        viewModel.loadReferences()
    }
    var description by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf(OrderStatus.PENDING) }
    var laborCost by rememberSaveable { mutableStateOf("") }
    var discount by rememberSaveable { mutableStateOf("0") }
    var assignedEmployeeId by rememberSaveable { mutableStateOf("") }

    val clientId = state.selectedClientId.orEmpty()
    val motorcycleId = state.selectedMotorcycleId.orEmpty()
    val motorcycles = state.selectedClientMotorcycles
    val selectedClient = state.selectedClient
    val selectedMotorcycle = state.selectedMotorcycle
    val statusOptions = OrderStatus.changeableValues
    val employeeOptions = listOf(OrderDropdownOption("", "Sin asignar")) + state.employees.mapNotNull { employee ->
        employee.id?.let { id ->
            val fullName = listOf(employee.name, employee.lastName)
                .filter(String::isNotBlank)
                .joinToString(" ")
                .ifBlank { "Técnico sin nombre" }
            OrderDropdownOption(
                id = id,
                label = listOf(fullName, employee.specialty)
                    .filter(String::isNotBlank)
                    .joinToString(" · "),
                searchText = listOf(fullName, employee.specialty, employee.phone, employee.email.orEmpty())
                    .joinToString(" ")
            )
        }
    }
    val clientOptions = state.clients.mapNotNull { client ->
        client.id?.let {
            OrderDropdownOption(
                id = it,
                label = client.name.ifBlank { "Cliente sin nombre" },
                searchText = listOf(client.name, client.cedula.orEmpty(), client.phone, client.email)
                    .joinToString(" ")
            )
        }
    }
    val motorcycleOptions = motorcycles.mapNotNull { motorcycle ->
        motorcycle.id?.let { OrderDropdownOption(it, "${motorcycle.brand} ${motorcycle.model} · ${motorcycle.plate}") }
    }

    LaunchedEffect(state.savedOrderId) {
        state.savedOrderId?.let { id ->
            viewModel.consumeOperationEvent()
            onOrderCreated(id)
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nueva orden", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Los campos disponibles corresponden al modelo Order actual.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OrderReferenceDropdown(
            label = "Cliente",
            selectedId = clientId,
            options = clientOptions,
            enabled = !state.isSaving,
            searchable = true,
            searchPlaceholder = "Nombre, documento, teléfono o correo",
            onSelected = viewModel::selectClient
        )
        selectedClient?.let { ClientSelectionSummary(it) }
        if (state.isLoadingClientMotorcycles && clientId.isNotBlank()) {
            Text(
                "Cargando motocicletas del cliente…",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (state.referencesLoaded && clientId.isNotBlank() && motorcycles.isEmpty()) {
            Text(
                "Este cliente no tiene motocicletas registradas.",
                color = MaterialTheme.colorScheme.error
            )
        }
        OrderReferenceDropdown(
            label = "Motocicleta",
            selectedId = motorcycleId,
            options = motorcycleOptions,
            enabled = !state.isSaving && clientId.isNotBlank() && !state.isLoadingClientMotorcycles,
            onSelected = viewModel::selectMotorcycle
        )
        selectedMotorcycle?.let { MotorcycleSelectionSummary(it) }
        OrderReferenceDropdown(
            label = "Técnico asignado",
            selectedId = assignedEmployeeId,
            options = employeeOptions,
            enabled = !state.isSaving,
            searchable = true,
            searchPlaceholder = "Nombre, especialidad o teléfono",
            onSelected = { assignedEmployeeId = it }
        )
        OutlinedTextField(
            value = laborCost,
            onValueChange = { laborCost = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mano de obra ($)") },
            singleLine = true,
            enabled = !state.isSaving,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = discount,
            onValueChange = { discount = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descuento ($)") },
            singleLine = true,
            enabled = !state.isSaving,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 2000) description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción del problema") },
            supportingText = { Text("${description.length}/2000") },
            minLines = 4,
            enabled = !state.isSaving
        )
        OrderReferenceDropdown(
            label = "Estado",
            selectedId = status,
            options = statusOptions.map { OrderDropdownOption(it, it) },
            enabled = !state.isSaving,
            onSelected = { status = it }
        )

        OrderTotalsPreview(
            laborCost = OrderValidator.parseMoney(laborCost) ?: 0.0,
            discount = OrderValidator.parseMoney(discount) ?: 0.0
        )

        state.operationMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                viewModel.createOrder(
                    clientId = clientId,
                    motorcycleId = motorcycleId,
                    description = description,
                    status = status,
                    laborCostText = laborCost,
                    assignedEmployeeId = assignedEmployeeId,
                    discountText = discount
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) CircularProgressIndicator() else Text("Crear orden")
        }
        Button(onClick = onBack, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}

@Composable
private fun ClientSelectionSummary(client: Client) {
    SelectionSummaryCard(
        title = "Cliente seleccionado",
        primary = client.name.ifBlank { "Nombre no registrado" },
        secondary = listOf(client.cedula, client.phone.takeIf(String::isNotBlank))
            .filterNotNull()
            .filter(String::isNotBlank)
            .joinToString(" · ")
    )
}

@Composable
private fun MotorcycleSelectionSummary(motorcycle: Motorcycle) {
    SelectionSummaryCard(
        title = "Motocicleta seleccionada",
        primary = motorcycle.plate ?: "Placa no registrada",
        secondary = listOfNotNull(
            listOfNotNull(motorcycle.brand, motorcycle.model).joinToString(" ").takeIf(String::isNotBlank),
            motorcycle.year?.toString()
        ).joinToString(" · ")
    )
}

@Composable
private fun SelectionSummaryCard(title: String, primary: String, secondary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(primary, style = MaterialTheme.typography.titleSmall)
            if (secondary.isNotBlank()) {
                Text(secondary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun OrderTotalsPreview(laborCost: Double, discount: Double) {
    val services = 0.0
    val parts = 0.0
    val subtotal = laborCost + services + parts
    val total = (subtotal - discount).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Resumen calculado", style = MaterialTheme.typography.titleSmall)
            OrderTotalLine("Mano de obra", laborCost)
            OrderTotalLine("Servicios", services)
            OrderTotalLine("Repuestos", parts)
            OrderTotalLine("Descuento", -discount)
            OrderTotalLine("Total", total, emphasized = true)
        }
    }
}

@Composable
private fun OrderTotalLine(label: String, amount: Double, emphasized: Boolean = false) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = if (emphasized) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium)
        Text(formatOrderMoney(amount), style = if (emphasized) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium)
    }
}
