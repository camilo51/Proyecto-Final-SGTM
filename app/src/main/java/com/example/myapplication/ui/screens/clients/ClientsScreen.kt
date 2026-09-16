package com.example.myapplication.ui.screens.clients

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Client
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.ClientViewModel

@Composable
fun ClientsScreen(
    contentPadding: PaddingValues,
    viewModel: ClientViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingClient by remember { mutableStateOf<Client?>(null) }
    var deletingClient by remember { mutableStateOf<Client?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadClients()
    }

    LaunchedEffect(state.creationVersion) {
        if (state.creationVersion > 0) {
            showCreateDialog = false
        }
    }

    LaunchedEffect(state.updateVersion) {
        if (state.updateVersion > 0) {
            editingClient = null
        }
    }

    LaunchedEffect(state.deleteVersion) {
        if (state.deleteVersion > 0) {
            deletingClient = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ClientsHeader(
            isSaving = state.isSaving,
            isRefreshing = state.isRefreshing,
            onCreateClient = {
                viewModel.clearOperationMessage()
                showCreateDialog = true
            },
            onRefresh = viewModel::refreshClients
        )

        ClientSummaryCard(total = state.clients.size)

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar clientes") },
            placeholder = { Text("Nombre, cédula, teléfono o correo") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = viewModel::clearSearch) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true,
            colors = AppOutlinedTextFieldColors()
        )

        if (state.errorMessage != null && state.clients.isNotEmpty()) {
            InlineErrorMessage(
                message = state.errorMessage.orEmpty(),
                onRetry = viewModel::loadClients
            )
        }

        if (!showCreateDialog && editingClient == null && deletingClient == null &&
            (state.creationVersion > 0 || state.updateVersion > 0 || state.deleteVersion > 0) &&
            state.operationMessage != null
        ) {
            SuccessMessage(state.operationMessage.orEmpty())
        }

        when {
            state.isLoading && state.clients.isEmpty() -> LoadingMessage(Modifier)
            state.errorMessage != null && state.clients.isEmpty() -> ErrorMessage(
                message = state.errorMessage.orEmpty(),
                onRetry = viewModel::loadClients
            )
            state.clients.isEmpty() -> EmptyMessage(
                message = "No hay clientes registrados"
            )
            state.filteredClients.isEmpty() -> EmptyMessage(
                message = "No se encontraron clientes con esa búsqueda",
                actionLabel = "Limpiar búsqueda",
                onAction = viewModel::clearSearch
            )
            else -> state.visibleClients.forEach { client ->
                ClientCard(
                    client = client,
                    onEdit = {
                        viewModel.clearOperationMessage()
                        editingClient = client
                    },
                    onDelete = {
                        viewModel.clearOperationMessage()
                        deletingClient = client
                    },
                    isDeleting = state.deletingClientId == client.id
                )
            }
        }

        if (state.totalPages > 1) {
            ClientPagination(
                currentPage = state.currentPage,
                totalPages = state.totalPages,
                onPageChange = viewModel::onPageChange
            )
        }
    }

    if (showCreateDialog) {
        CreateClientDialog(
            isSaving = state.isSaving,
            errorMessage = state.operationMessage,
            onDismissRequest = {
                showCreateDialog = false
                viewModel.clearOperationMessage()
            },
            onSave = viewModel::createClient
        )
    }

    editingClient?.let { client ->
        EditClientDialog(
            client = client,
            isSaving = state.isSaving,
            errorMessage = state.operationMessage,
            onDismissRequest = {
                editingClient = null
                viewModel.clearOperationMessage()
            },
            onSave = viewModel::updateClient
        )
    }

    deletingClient?.let { client ->
        DeleteClientDialog(
            client = client,
            isDeleting = state.isDeleting,
            errorMessage = state.operationMessage,
            onDismissRequest = {
                if (!state.isDeleting) {
                    deletingClient = null
                    viewModel.clearOperationMessage()
                }
            },
            onConfirm = { client.id?.let(viewModel::deleteClient) }
        )
    }
}

@Composable
private fun ClientsHeader(
    isSaving: Boolean,
    isRefreshing: Boolean,
    onCreateClient: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = "GESTIÓN DEL TALLER",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Clientes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Consulta los contactos registrados para la atención del taller.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCreateClient,
                modifier = Modifier.weight(1.25f),
                enabled = !isSaving
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text("Nuevo cliente")
            }
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.weight(1f),
                enabled = !isRefreshing && !isSaving
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(if (isRefreshing) "Actualizando…" else "Actualizar")
            }
        }
    }
}

@Composable
private fun ClientSummaryCard(total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column {
                Text(
                    text = "Registrados",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = total.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ClientPagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 1
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Página anterior"
                )
            }
            Text(
                text = "Página $currentPage de $totalPages",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Página siguiente"
                )
            }
        }
    }
}

@Composable
private fun CreateClientDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismissRequest: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var cedula by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismissRequest() },
        title = { Text("Nuevo cliente") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Todos los campos son opcionales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ClientFormField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre"
                )
                ClientFormField(
                    value = cedula,
                    onValueChange = { cedula = it },
                    label = "Cédula"
                )
                ClientFormField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Número"
                )
                ClientFormField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo"
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, cedula, phone, email) },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isSaving
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EditClientDialog(
    client: Client,
    isSaving: Boolean,
    errorMessage: String?,
    onDismissRequest: () -> Unit,
    onSave: (Client) -> Unit
) {
    var name by rememberSaveable(client.id) { mutableStateOf(client.name) }
    var cedula by rememberSaveable(client.id) { mutableStateOf(client.cedula.orEmpty()) }
    var phone by rememberSaveable(client.id) { mutableStateOf(client.phone) }
    var email by rememberSaveable(client.id) { mutableStateOf(client.email) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismissRequest() },
        title = { Text("Editar cliente") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Actualiza la información disponible del cliente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ClientFormField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre"
                )
                ClientFormField(
                    value = cedula,
                    onValueChange = { cedula = it },
                    label = "Cédula"
                )
                ClientFormField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Número"
                )
                ClientFormField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo"
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        client.copy(
                            name = name,
                            cedula = cedula.trim().takeIf(String::isNotBlank),
                            phone = phone,
                            email = email
                        )
                    )
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar cambios")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isSaving
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ClientFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        colors = AppOutlinedTextFieldColors()
    )
}

@Composable
private fun ClientCard(
    client: Client,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDeleting: Boolean
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "CLIENTE",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = clientDisplayName(client),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    onClick = onEdit,
                    enabled = !client.id.isNullOrBlank() && !isDeleting
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar cliente"
                    )
                }
                IconButton(
                    onClick = onDelete,
                    enabled = !client.id.isNullOrBlank() && !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar cliente"
                        )
                    }
                }
            }

            client.id?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = "ID $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            client.cedula?.takeIf(String::isNotBlank)?.let {
                ClientInfoRow(Icons.Filled.Person, "Cédula", it)
            }
            ClientInfoRow(Icons.Filled.Phone, "Teléfono", client.phone.orEmpty())
            ClientInfoRow(Icons.Filled.Email, "Correo", client.email.orEmpty())
        }
    }
}

@Composable
private fun DeleteClientDialog(
    client: Client,
    isDeleting: Boolean,
    errorMessage: String?,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismissRequest() },
        title = { Text("Eliminar cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("¿Deseas eliminar a ${clientDisplayName(client)}? Esta acción se enviará a la API.")
                if (!errorMessage.isNullOrBlank()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isDeleting) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Eliminar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, enabled = !isDeleting) { Text("Cancelar") }
        }
    )
}

private fun clientDisplayName(client: Client): String {
    client.name.orEmpty().trim().takeIf(String::isNotEmpty)?.let { return it }
    client.cedula.orEmpty().trim().takeIf(String::isNotEmpty)?.let { return "Cliente $it" }
    client.phone.orEmpty().trim().takeIf(String::isNotEmpty)?.let { return it }
    client.email.orEmpty().trim().takeIf(String::isNotEmpty)?.let { return it }
    return "Cliente sin identificar"
}

@Composable
private fun SuccessMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ClientInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    if (value.isBlank()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadingMessage(modifier: Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Cargando clientes…",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        }
    }
}

@Composable
private fun InlineErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyMessage(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Text(message, style = MaterialTheme.typography.bodyLarge)
                if (actionLabel != null && onAction != null) {
                    OutlinedButton(onClick = onAction) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}
