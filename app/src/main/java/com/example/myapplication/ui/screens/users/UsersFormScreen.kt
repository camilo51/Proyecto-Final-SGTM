package com.example.myapplication.ui.screens.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.User
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.screens.FormHeader
import com.example.myapplication.ui.screens.FormPrimaryButton
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.UserViewModel

@Composable
fun UsersFormScreen(
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    user: User? = null,
    viewModel: UserViewModel = viewModel()
) {
    val orange = MaterialTheme.colorScheme.primary

    var username by remember { mutableStateOf(user?.name ?: "nombre_usuario") }
    var email by remember { mutableStateOf(user?.email ?: "usuario@sgtm.test") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(user?.role ?: "Administrador") }
    var estado by remember { mutableStateOf("Activo") }

    val roles = listOf("Administrador", "Técnico", "Recepción")
    val estados = listOf("Activo", "Inactivo")

    AppScaffold(
        title = if (user == null) "Nuevo usuario" else "Editar usuario",
        navController = navController,
        isAdmin = true,
        userName = userName,
        onLogout = onLogout
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormHeader(
                title = if (user == null) "Registrar usuario" else "Editar usuario",
                subtitle = "Defina las credenciales y el rol de acceso al sistema.",
                onBack = { navController.popBackStack() }
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Usuario *") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppOutlinedTextFieldColors()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Correo electrónico *") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppOutlinedTextFieldColors()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña *") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppOutlinedTextFieldColors()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirmar contraseña *") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppOutlinedTextFieldColors()
            )

            if (password.isNotEmpty() && password.length < 6) {
                Text("Mínimo 6 caracteres", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else if (password.isNotEmpty() && password != confirmPassword) {
                Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            var expandedRole by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = role,
                    onValueChange = {},
                    placeholder = { Text("Rol *") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expandedRole = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar rol")
                        }
                    },
                    colors = AppOutlinedTextFieldColors()
                )
                DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                    roles.forEach { r ->
                        DropdownMenuItem(text = { Text(r) }, onClick = { role = r; expandedRole = false })
                    }
                }
            }

            var expandedEstado by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    placeholder = { Text("Estado") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expandedEstado = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar estado")
                        }
                    },
                    colors = AppOutlinedTextFieldColors()
                )
                DropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }) {
                    estados.forEach { e ->
                        DropdownMenuItem(text = { Text(e) }, onClick = { estado = e; expandedEstado = false })
                    }
                }
            }

            FormPrimaryButton(
                text = "Guardar usuario",
                onClick = {
                    if (user == null) {
                        viewModel.createUser(username.trim(), email.trim(), role.takeIf { it.isNotBlank() })
                    } else {
                        viewModel.updateUser(user.copy(name = username.trim(), email = email.trim(), role = role.takeIf { it.isNotBlank() }))
                    }
                    navController.popBackStack()
                }
            )

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cancelar", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
