package com.example.myapplication.ui.screens.motorcycles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.MotorcycleStatus
import com.example.myapplication.ui.viewmodel.MotorcycleInput
import com.example.myapplication.ui.viewmodel.MotorcycleViewModel

@Composable
fun CreateMotorcycleScreen(
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: MotorcycleViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadClients() }
    LaunchedEffect(state.savedMotorcycleId) {
        state.savedMotorcycleId?.let { id ->
            viewModel.consumeSavedMotorcycle()
            onCreated(id)
        }
    }

    AppScaffold("Nueva motocicleta", navController, isAdmin = true, userName = userName, onLogout = onLogout) { padding ->
        MotorcycleForm(
            initial = null,
            state = state,
            contentPadding = padding,
            title = "Registrar motocicleta",
            submitLabel = "Crear motocicleta",
            onBack = navController::popBackStack,
            onSave = { input -> viewModel.createMotorcycleInput(input) },
            viewModel = viewModel
        )
    }
}

@Composable
fun EditMotorcycleScreen(
    id: String,
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    viewModel: MotorcycleViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(id) {
        viewModel.loadMotorcycle(id)
        viewModel.loadClients()
    }
    LaunchedEffect(state.savedMotorcycleId) {
        if (state.savedMotorcycleId == id) {
            viewModel.consumeSavedMotorcycle()
            navController.popBackStack()
        }
    }

    AppScaffold("Editar motocicleta", navController, isAdmin = true, userName = userName, onLogout = onLogout) { padding ->
        val motorcycle = state.selectedMotorcycle
        when {
            state.isLoadingDetail && (motorcycle == null || motorcycle.id != id) ->
                CircularProgressIndicator(modifier = Modifier.padding(padding).padding(16.dp))
            motorcycle == null || motorcycle.id != id ->
                Column(
                    modifier = Modifier.padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(state.detailErrorMessage ?: "La motocicleta no existe.", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = navController::popBackStack) { Text("← Volver") }
                }
            else -> key(motorcycle.id) {
                MotorcycleForm(
                    initial = motorcycle,
                    state = state,
                    contentPadding = padding,
                    title = "Editar motocicleta",
                    submitLabel = "Guardar cambios",
                    onBack = navController::popBackStack,
                    onSave = { input -> viewModel.updateMotorcycle(input.toMotorcycle(motorcycle.id)) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun MotorcycleForm(
    initial: Motorcycle?,
    state: com.example.myapplication.ui.viewmodel.MotorcycleUiState,
    contentPadding: PaddingValues,
    title: String,
    submitLabel: String,
    onBack: () -> Unit,
    onSave: (MotorcycleInput) -> Unit,
    viewModel: MotorcycleViewModel
) {
    val formKey = initial?.id ?: "new"
    var plate by rememberSaveable(formKey) { mutableStateOf(initial?.plate.orEmpty()) }
    var brand by rememberSaveable(formKey) { mutableStateOf(initial?.brand.orEmpty()) }
    var model by rememberSaveable(formKey) { mutableStateOf(initial?.model.orEmpty()) }
    var year by rememberSaveable(formKey) { mutableStateOf(initial?.year?.toString().orEmpty()) }
    var color by rememberSaveable(formKey) { mutableStateOf(initial?.color.orEmpty()) }
    var engineCc by rememberSaveable(formKey) { mutableStateOf(initial?.engineCc?.toString().orEmpty()) }
    var clientId by rememberSaveable(formKey) { mutableStateOf(initial?.clientId) }
    var status by rememberSaveable(formKey) { mutableStateOf(initial?.status ?: MotorcycleStatus.IN_SERVICE) }
    var notes by rememberSaveable(formKey) { mutableStateOf(initial?.notes.orEmpty()) }
    var statusExpanded by rememberSaveable(formKey) { mutableStateOf(false) }

    val selectedClient = state.clients.firstOrNull { it.id == clientId }
    val clientResults = state.filteredClients

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack, enabled = !state.isSaving) { Text("← Volver") }
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Los campos opcionales pueden dejarse vacíos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        MotorcycleTextField("Placa", plate, { plate = it }, state.isSaving)
        MotorcycleTextField("Marca", brand, { brand = it }, state.isSaving)
        MotorcycleTextField("Modelo", model, { model = it }, state.isSaving)
        MotorcycleTextField(
            label = "Año",
            value = year,
            onValueChange = { year = it.filter(Char::isDigit) },
            enabled = !state.isSaving,
            keyboardType = KeyboardType.Number
        )
        MotorcycleTextField("Color", color, { color = it }, state.isSaving)
        MotorcycleTextField(
            label = "Cilindraje (cc)",
            value = engineCc,
            onValueChange = { engineCc = it.filter(Char::isDigit) },
            enabled = !state.isSaving,
            keyboardType = KeyboardType.Number
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Propietario", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedTextField(
                    value = state.clientSearchQuery,
                    onValueChange = viewModel::onClientSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nombre, documento o teléfono") },
                    enabled = !state.isSaving,
                    singleLine = true,
                    colors = AppOutlinedTextFieldColors()
                )
                DropdownMenu(
                    expanded = state.clientSearchQuery.isNotBlank() && clientResults.isNotEmpty(),
                    onDismissRequest = viewModel::clearClientSearchQuery,
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    clientResults.forEach { client ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(client.name.ifBlank { "Cliente sin nombre" })
                                    Text(
                                        listOfNotNull(client.cedula, client.phone.takeIf(String::isNotBlank)).joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                clientId = client.id
                                viewModel.clearClientSearchQuery()
                            }
                        )
                    }
                }
            }
            if (state.isLoadingClients) {
                Text("Cargando propietarios…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                selectedClient?.name?.let { "Seleccionado: $it" } ?: "Sin propietario",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { clientId = null; viewModel.clearClientSearchQuery() },
                    enabled = !state.isSaving && clientId != null
                ) { Text("Sin propietario") }
            }
        }

        Box {
            OutlinedButton(
                onClick = { statusExpanded = true },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Estado: $status") }
            DropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false }
            ) {
                MotorcycleStatus.values.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { status = option; statusExpanded = false }
                    )
                }
            }
        }

        MotorcycleTextField(
            label = "Notas",
            value = notes,
            onValueChange = { notes = it },
            enabled = !state.isSaving,
            minLines = 3
        )

        state.operationMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                onSave(
                    MotorcycleInput(
                        plate = plate,
                        brand = brand,
                        model = model,
                        yearText = year,
                        color = color,
                        engineCcText = engineCc,
                        clientId = clientId,
                        status = status,
                        notes = notes
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) CircularProgressIndicator(strokeWidth = 2.dp) else Text(submitLabel)
        }
        TextButton(onClick = onBack, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}

@Composable
private fun MotorcycleTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(label) },
        enabled = enabled,
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = AppOutlinedTextFieldColors()
    )
}
