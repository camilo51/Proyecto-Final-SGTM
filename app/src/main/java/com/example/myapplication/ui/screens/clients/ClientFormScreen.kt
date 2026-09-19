package com.example.myapplication.ui.screens.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.screens.FormHeader
import com.example.myapplication.ui.screens.FormPrimaryButton
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.ClientFormState
import com.example.myapplication.ui.viewmodel.ClientViewModel

private val clientDocumentTypes = listOf("CC", "CE", "NIT", "Pasaporte")

@Composable
fun ClientFormScreen(
    clientId: String?,
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    currentUser: UserDto? = null,
    onOpenProfile: () -> Unit = {},
    viewModel: ClientViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val isEditing = clientId != null
    var formStarted by remember(clientId) { mutableStateOf(false) }

    LaunchedEffect(clientId) {
        if (clientId == null) {
            viewModel.startCreateForm()
        } else {
            viewModel.startEditForm(clientId)
        }
        formStarted = true
    }

    LaunchedEffect(state.form.completionVersion) {
        if (formStarted && state.form.completionVersion > 0) {
            viewModel.consumeFormCompletion()
            navController.popBackStack()
        }
    }

    AppScaffold(
        title = if (isEditing) "Editar cliente" else "Nuevo cliente",
        navController = navController,
        isAdmin = true,
        userName = userName,
        currentUser = currentUser,
        onOpenProfile = onOpenProfile,
        onLogout = onLogout
    ) { padding ->
        ClientFormContent(
            contentPadding = padding,
            form = state.form,
            isSaving = state.isSaving,
            title = if (isEditing) "Editar cliente" else "Registrar cliente",
            submitLabel = if (isEditing) "Guardar cambios" else "Guardar cliente",
            onBack = navController::popBackStack,
            onSave = if (isEditing) viewModel::updateClient else viewModel::createClient,
            onDocumentTypeChange = viewModel::onDocumentTypeChange,
            onDocumentChange = viewModel::onDocumentChange,
            onNameChange = viewModel::onNameChange,
            onLastNameChange = viewModel::onLastNameChange,
            onPhoneChange = viewModel::onPhoneChange
        )
    }
}

@Composable
private fun ClientFormContent(
    contentPadding: PaddingValues,
    form: ClientFormState,
    isSaving: Boolean,
    title: String,
    submitLabel: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDocumentTypeChange: (String) -> Unit,
    onDocumentChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormHeader(
                title = title,
                subtitle = "Completa la información del cliente. Los campos que diligencies se validan según las reglas del servicio.",
                onBack = onBack
            )

            ClientDocumentTypeField(
                value = form.documentType,
                enabled = !isSaving,
                onValueChange = onDocumentTypeChange
            )
            ClientTextField(
                label = "Documento",
                value = form.document,
                onValueChange = onDocumentChange,
                enabled = !isSaving,
                keyboardType = KeyboardType.Text
            )
            ClientTextField(
                label = "Nombre",
                value = form.name,
                onValueChange = onNameChange,
                enabled = !isSaving
            )
            ClientTextField(
                label = "Apellido",
                value = form.lastName,
                onValueChange = onLastNameChange,
                enabled = !isSaving
            )
            ClientTextField(
                label = "Teléfono",
                value = form.phone,
                onValueChange = onPhoneChange,
                enabled = !isSaving,
                keyboardType = KeyboardType.Phone
            )

            form.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            FormPrimaryButton(
                text = submitLabel,
                onClick = onSave,
                enabled = !isSaving && !form.isLoading
            )
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isSaving
            ) {
                Text("Cancelar", color = MaterialTheme.colorScheme.primary)
            }
        }

        if (form.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ClientDocumentTypeField(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tipo de documento") },
            readOnly = true,
            enabled = enabled,
            colors = AppOutlinedTextFieldColors()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(Modifier)
        ) {
            TextButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxSize()
            ) { Text("") }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            clientDocumentTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onValueChange(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ClientTextField(
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
        label = { Text(label) },
        enabled = enabled,
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = AppOutlinedTextFieldColors()
    )
}
