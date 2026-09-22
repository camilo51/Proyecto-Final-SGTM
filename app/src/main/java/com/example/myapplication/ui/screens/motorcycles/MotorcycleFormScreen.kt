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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.screens.BackNavigationLink
import com.example.myapplication.ui.screens.OperationMessage
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.MotorcycleStatus
import com.example.myapplication.ui.viewmodel.MotorcycleInput
import com.example.myapplication.ui.viewmodel.MotorcycleViewModel

@Composable
fun CreateMotorcycleScreen(
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    currentUser: UserDto? = null,
    onOpenProfile: () -> Unit = {},
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

    AppScaffold(
        "Nueva motocicleta",
        navController,
        isAdmin = true,
        userName = userName,
        currentUser = currentUser,
        onOpenProfile = onOpenProfile,
        onLogout = onLogout
    ) { padding ->
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
    currentUser: UserDto? = null,
    onOpenProfile: () -> Unit = {},
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

    AppScaffold(
        "Editar motocicleta",
        navController,
        isAdmin = true,
        userName = userName,
        currentUser = currentUser,
        onOpenProfile = onOpenProfile,
        onLogout = onLogout
    ) { padding ->
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
                    BackNavigationLink(onClick = navController::popBackStack)
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
    LaunchedEffect(formKey, initial?.clientId) {
        viewModel.setFormClientId(initial?.clientId)
    }
    var plate by rememberSaveable(formKey) { mutableStateOf(initial?.plate.orEmpty()) }
    var brand by rememberSaveable(formKey) { mutableStateOf(initial?.brand.orEmpty()) }
    var model by rememberSaveable(formKey) { mutableStateOf(initial?.model.orEmpty()) }
    var year by rememberSaveable(formKey) { mutableStateOf(initial?.year?.toString().orEmpty()) }
    var color by rememberSaveable(formKey) { mutableStateOf(initial?.color.orEmpty()) }
    var engineCc by rememberSaveable(formKey) { mutableStateOf(initial?.engineCc?.toString().orEmpty()) }
    var notes by rememberSaveable(formKey) { mutableStateOf(initial?.notes.orEmpty()) }
    val status = initial?.status ?: MotorcycleStatus.IN_SERVICE
    val plateFocusRequester = remember { FocusRequester() }
    val brandFocusRequester = remember { FocusRequester() }
    val modelFocusRequester = remember { FocusRequester() }
    val yearFocusRequester = remember { FocusRequester() }
    val colorFocusRequester = remember { FocusRequester() }
    val engineCcFocusRequester = remember { FocusRequester() }
    val ownerFocusRequester = remember { FocusRequester() }
    val notesFocusRequester = remember { FocusRequester() }

    val clientId = state.selectedClientId
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
        BackNavigationLink(onClick = onBack, enabled = !state.isSaving)
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Los campos opcionales pueden dejarse vacíos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        MotorcycleTextField(
            label = "Placa",
            value = plate,
            onValueChange = { plate = it },
            enabled = !state.isSaving,
            focusRequester = plateFocusRequester,
            onNext = { brandFocusRequester.requestFocus() }
        )
        MotorcycleTextField(
            label = "Marca",
            value = brand,
            onValueChange = { brand = it },
            enabled = !state.isSaving,
            focusRequester = brandFocusRequester,
            onNext = { modelFocusRequester.requestFocus() }
        )
        MotorcycleTextField(
            label = "Modelo",
            value = model,
            onValueChange = { model = it },
            enabled = !state.isSaving,
            focusRequester = modelFocusRequester,
            onNext = { yearFocusRequester.requestFocus() }
        )
        MotorcycleTextField(
            label = "Año",
            value = year,
            onValueChange = { year = it.filter(Char::isDigit) },
            enabled = !state.isSaving,
            keyboardType = KeyboardType.Number,
            focusRequester = yearFocusRequester,
            onNext = { colorFocusRequester.requestFocus() }
        )
        MotorcycleTextField(
            label = "Color",
            value = color,
            onValueChange = { color = it },
            enabled = !state.isSaving,
            focusRequester = colorFocusRequester,
            onNext = { engineCcFocusRequester.requestFocus() }
        )
        MotorcycleTextField(
            label = "Cilindraje (cc)",
            value = engineCc,
            onValueChange = { engineCc = it.filter(Char::isDigit) },
            enabled = !state.isSaving,
            keyboardType = KeyboardType.Number,
            focusRequester = engineCcFocusRequester,
            onNext = { ownerFocusRequester.requestFocus() }
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Propietario", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedTextField(
                    value = selectedClient?.name ?: state.clientSearchQuery,
                    onValueChange = viewModel::onClientSearchQueryChange,
                    modifier = Modifier.fillMaxWidth().focusRequester(ownerFocusRequester),
                    label = { Text("Buscar cliente") },
                    placeholder = { Text("Nombre, documento o teléfono") },
                    enabled = !state.isSaving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { notesFocusRequester.requestFocus() }),
                    colors = AppOutlinedTextFieldColors()
                )
                DropdownMenu(
                    expanded = clientId == null && state.clientSearchQuery.isNotBlank() && clientResults.isNotEmpty(),
                    onDismissRequest = viewModel::clearClientSearchQuery,
                    modifier = Modifier.fillMaxWidth(0.92f),
                    properties = PopupProperties(focusable = false)
                ) {
                    clientResults.forEach { client ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(client.name.orEmpty().ifBlank { "Cliente sin nombre" })
                                    Text(
                                        listOfNotNull(client.document, client.phone?.takeIf(String::isNotBlank)).joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                client.id?.let(viewModel::selectClient)
                                viewModel.clearClientSearchQuery()
                            }
                        )
                    }
                }
            }
            if (state.isLoadingClients) {
                Text("Cargando propietarios…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            selectedClient?.let { MotorcycleClientSummary(it) }
            Text(
                when {
                    clientId == null -> "Sin propietario: se enviará client_id = null"
                    selectedClient != null -> "✓ ${selectedClient.name.orEmpty().ifBlank { "Nombre no registrado" }} seleccionado"
                    state.isLoadingClients -> "Cargando propietario…"
                    else -> "Propietario no encontrado"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (clientId != null && selectedClient != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { viewModel.clearSelectedClient(); viewModel.clearClientSearchQuery() },
                    enabled = !state.isSaving && clientId != null
                ) { Text("Sin propietario") }
            }
        }

        MotorcycleTextField(
            label = "Notas",
            value = notes,
            onValueChange = { notes = it },
            enabled = !state.isSaving,
            minLines = 3,
            focusRequester = notesFocusRequester,
            imeAction = ImeAction.Done
        )

        state.operationMessage?.let { message ->
            OperationMessage(message)
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
private fun MotorcycleClientSummary(client: Client) {
    val details = listOf(client.document, client.phone, client.email)
        .filterNotNull()
        .filter(String::isNotBlank)
        .joinToString(" · ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "Cliente seleccionado",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                client.name.orEmpty().ifBlank { "Nombre no registrado" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (details.isNotBlank()) {
                Text(
                    details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
    minLines: Int = 1,
    focusRequester: FocusRequester,
    imeAction: ImeAction = ImeAction.Next,
    onNext: () -> Unit = {},
    onDone: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        label = { Text(label) },
        enabled = enabled,
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onDone() }),
        colors = AppOutlinedTextFieldColors()
    )
}
