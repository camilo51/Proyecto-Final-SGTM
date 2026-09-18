package com.example.myapplication.ui.screens.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.viewmodel.UserViewModel
import com.example.myapplication.data.model.User

@Composable
fun UsersFormScreen(
    user: User? = null,
    onBack: () -> Unit = {},
    viewModel: UserViewModel = viewModel()
) {
    val orange = MaterialTheme.colorScheme.primary

    var username by remember { mutableStateOf(user?.name ?: "nombre_usuario") }
    var email by remember { mutableStateOf(user?.email ?: "usuario@sgtm.test") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(user?.avatar ?: "Administrador") }
    var estado by remember { mutableStateOf("Activo") }

    val roles = listOf("Administrador", "Técnico", "Recepción")
    val estados = listOf("Activo", "Inactivo")

    AppTheme(darkTheme = true) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(if (user == null) "Nuevo usuario" else "Editar usuario", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Gestión de cuentas", style = MaterialTheme.typography.labelSmall, color = orange)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Los campos opcionales pueden dejarse vacíos.", color = Color.White.copy(alpha = 0.6f))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña *") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (password.isNotEmpty() && password.length < 6) {
                    Text("Mínimo 6 caracteres", color = MaterialTheme.colorScheme.error)
                } else if (password.isNotEmpty() && password != confirmPassword) {
                    Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error)
                }

                // Rol dropdown
                var expandedRole by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        label = { Text("Rol *") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedRole = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar rol") }
                        }
                    )
                    DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                        roles.forEach { r -> DropdownMenuItem(text = { Text(r) }, onClick = { role = r; expandedRole = false }) }
                    }
                }

                // Estado dropdown
                var expandedEstado by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        label = { Text("Estado") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { expandedEstado = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar estado") } }
                    )
                    DropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }) {
                        estados.forEach { e -> DropdownMenuItem(text = { Text(e) }, onClick = { estado = e; expandedEstado = false }) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(
                        onClick = onBack,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (user == null) {
                                viewModel.createUser(username.trim(), email.trim(), role.takeIf { it.isNotBlank() })
                            } else {
                                viewModel.updateUser(user.copy(name = username.trim(), email = email.trim(), avatar = role.takeIf { it.isNotBlank() }))
                            }
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = orange),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (user == null) "Crear usuario" else "Guardar usuario", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
