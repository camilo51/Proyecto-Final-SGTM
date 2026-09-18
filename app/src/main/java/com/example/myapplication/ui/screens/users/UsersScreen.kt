package com.example.myapplication.ui.screens.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.User
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.viewmodel.UserViewModel

@Composable
fun UsersScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: UserViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val orange = MaterialTheme.colorScheme.primary

    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    AppTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "MÓDULO ADMINISTRATIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = orange,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Usuarios",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Gestión de cuentas de acceso al sistema.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Main Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { 
                            selectedUser = null
                            showDialog = true 
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = orange),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("+ Nuevo usuario", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = { viewModel.refreshUsers() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Actualizar", fontSize = 13.sp, color = Color.White)
                    }
                }

                // Stats Cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { UserStatMiniCard("Total", state.users.size.toString()) }
                    item { UserStatMiniCard("Filtrados", state.filteredUsers.size.toString()) }
                }

                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar usuarios...", color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.4f)) },
                    trailingIcon = if (state.searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { viewModel.clearSearch() }) { Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.4f)) } }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = orange.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Error Message
                state.errorMessage?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                // User Cards List
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = orange)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        state.visibleUsers.forEach { user ->
                            UserCard(
                                user = user,
                                onEdit = {
                                    selectedUser = user
                                    showDialog = true
                                },
                                onDelete = { user.id?.let { viewModel.deleteUser(it) } }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showDialog) {
            UserFormDialog(
                user = selectedUser,
                onDismiss = { 
                    showDialog = false
                    viewModel.clearOperationMessage()
                },
                onSave = { name, email, avatar ->
                    if (selectedUser == null) {
                        viewModel.createUser(name, email, avatar)
                    } else {
                        viewModel.updateUser(selectedUser!!.copy(name = name, email = email, avatar = avatar))
                    }
                },
                isSaving = state.isSaving,
                operationMessage = state.operationMessage
            )
        }
    }

    // Reset dialog when saving completes successfully
    LaunchedEffect(state.creationVersion, state.updateVersion) {
        if (showDialog && !state.isSaving && state.operationMessage?.contains("correctamente") == true) {
            showDialog = false
            viewModel.clearOperationMessage()
        }
    }
}

@Composable
private fun UserFormDialog(
    user: User?,
    onDismiss: () -> Unit,
    onSave: (String, String, String?) -> Unit,
    isSaving: Boolean,
    operationMessage: String?
) {
    // Keep mapping compatible with existing viewModel: name <- username, avatar <- role
    var username by remember { mutableStateOf(user?.name ?: "nombre_usuario") }
    var email by remember { mutableStateOf(user?.email ?: "usuario@sgtm.test") }
    var password by remember { mutableStateOf("secret1") }
    var confirmPassword by remember { mutableStateOf("secret1") }
    var role by remember { mutableStateOf(user?.avatar ?: "Administrador") }
    var estado by remember { mutableStateOf("Activo") }

    val roles = listOf("Administrador", "Técnico", "Recepción")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Nuevo usuario" else "Editar usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Usuario *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirmar contraseña *") }, modifier = Modifier.fillMaxWidth())
                if (password.length < 6) {
                    Text("Mínimo 6 caracteres", color = MaterialTheme.colorScheme.error)
                } else if (password != confirmPassword) {
                    Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error)
                }

                // Role dropdown
                var expandedRole by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        label = { Text("Rol *") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedRole = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar rol")
                            }
                        }
                    )
                    DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { role = r; expandedRole = false })
                        }
                    }
                }

                // Estado dropdown
                var expandedEstado by remember { mutableStateOf(false) }
                val estados = listOf("Activo", "Inactivo")
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        label = { Text("Estado") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedEstado = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar estado")
                            }
                        }
                    )
                    DropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }) {
                        estados.forEach { e ->
                            DropdownMenuItem(text = { Text(e) }, onClick = { estado = e; expandedEstado = false })
                        }
                    }
                }

                operationMessage?.let {
                    Text(it, color = if (it.contains("correctamente")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(username.trim(), email.trim(), role.takeIf { it.isNotBlank() }) },
                enabled = !isSaving && username.isNotBlank() && email.isNotBlank() && password.length >= 6 && password == confirmPassword
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Guardar usuario")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}

@Composable
private fun UserStatMiniCard(label: String, value: String) {
    Card(
        modifier = Modifier.size(width = 110.dp, height = 70.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val orange = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "ID: ${user.id ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Text("Editar", color = orange, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
