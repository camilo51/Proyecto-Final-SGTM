package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.ui.viewmodel.ProfileUiState
import com.example.myapplication.ui.viewmodel.ProfileViewModel
import java.util.Locale

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    sessionUser: UserDto?,
    onUserUpdated: (UserDto) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionUser?.id) {
        viewModel.loadProfile(sessionUser)
    }

    LaunchedEffect(state.user) {
        state.user?.let(onUserUpdated)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BackNavigationLink(onClick = onBack)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "MI CUENTA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text("Mi Perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Administra tu información personal y seguridad de la cuenta.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        state.errorMessage?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
        state.successMessage?.let { success ->
            item { Text(success, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
        }

        if (state.isLoading && state.user == null) {
            item {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 24.dp))
            }
        } else {
            val user = state.user ?: sessionUser
            if (user != null) {
                item {
                    ProfileSummaryCard(user)
                }
                item {
                    key(user.id) {
                        ProfileInformationCard(
                            user = user,
                            isSaving = state.isSaving,
                            onSave = viewModel::updateProfile
                        )
                    }
                }
                item {
                    SecurityCard(
                        state = state,
                        isSaving = state.isSaving,
                        onChangePassword = viewModel::changePassword
                    )
                }
            } else {
                item {
                    Text("No se encontró información del usuario.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ProfileSummaryCard(user: UserDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SgtmAvatar(user, Modifier.size(72.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(user.name ?: "Usuario SGTM", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(user.email ?: "Correo no registrado", color = MaterialTheme.colorScheme.onSurfaceVariant)
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(readableProfileRole(user.role)) }
                )
            }
        }
    }
}

@Composable
private fun ProfileInformationCard(
    user: UserDto,
    isSaving: Boolean,
    onSave: (String, String) -> Unit
) {
    var name by remember(user.id, user.name) { mutableStateOf(user.name.orEmpty()) }
    var email by remember(user.id, user.email) { mutableStateOf(user.email.orEmpty()) }

    ProfileSectionCard(title = "Información personal") {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de usuario") },
            singleLine = true,
            enabled = !isSaving
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            singleLine = true,
            enabled = !isSaving
        )
        OutlinedTextField(
            value = readableProfileRole(user.role),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cargo / Rol") },
            readOnly = true,
            enabled = false,
        )
        Button(
            onClick = { onSave(name, email) },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSaving) "Guardando..." else "Guardar información")
        }
    }
}

@Composable
private fun SecurityCard(
    state: ProfileUiState,
    isSaving: Boolean,
    onChangePassword: (String, String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage == "Contraseña actualizada correctamente") {
            currentPassword = ""
            newPassword = ""
            confirmation = ""
        }
    }

    ProfileSectionCard(title = "Seguridad") {
        PasswordField(
            value = currentPassword,
            label = "Contraseña actual",
            visible = showCurrent,
            enabled = !isSaving,
            onValueChange = { currentPassword = it },
            onToggleVisibility = { showCurrent = !showCurrent }
        )
        PasswordField(
            value = newPassword,
            label = "Nueva contraseña",
            visible = showNew,
            enabled = !isSaving,
            onValueChange = { newPassword = it },
            onToggleVisibility = { showNew = !showNew }
        )
        PasswordField(
            value = confirmation,
            label = "Confirmar nueva contraseña",
            visible = showConfirmation,
            enabled = !isSaving,
            onValueChange = { confirmation = it },
            onToggleVisibility = { showConfirmation = !showConfirmation }
        )
        Button(
            onClick = { onChangePassword(currentPassword, newPassword, confirmation) },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSaving) "Actualizando..." else "Cambiar contraseña")
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    label: String,
    visible: Boolean,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = onToggleVisibility) {
                Text(if (visible) "Ocultar" else "Mostrar")
            }
        }
    )
}

@Composable
private fun ProfileSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                content()
            }
        )
    }
}

private fun readableProfileRole(role: String?): String {
    return when (role?.trim()?.lowercase(Locale.ROOT)) {
        "admin", "administrador", "1" -> "Administrador"
        "recepcionista", "2" -> "Recepcionista"
        "tecnico", "técnico", "3" -> "Técnico"
        null, "" -> "Rol no registrado"
        else -> role
    }
}
