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
import androidx.navigation.NavController
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.screens.BackLink
import com.example.myapplication.ui.viewmodel.UserViewModel
import com.example.myapplication.data.model.User

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
    var role by remember { mutableStateOf(user?.avatar ?: "Administrador") }
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BackLink(onClick = { navController.popBackStack() })

            Text(
                text = "Registrar usuario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Defina las credenciales y el rol de acceso al sistema.",
                color = Color.White.copy(alpha = 0.6f)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Usuario *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Correo electrónico *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirmar contraseña *") },
                modifier = Modifier.fillMaxWidth()
            )

            if (password.isNotEmpty() && password.length < 6) {
                Text("Mínimo 6 caracteres", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else if (password.isNotEmpty() && password != confirmPassword) {
                Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Rol dropdown (visual, imitando la imagen con botón naranja si se desea, pero mantendré dropdown por usabilidad, aunque añadiré el botón si es lo que se pide)
            // La imagen muestra un botón naranja "Seleccionar categoría" debajo del campo "Categoría".
            
            OutlinedTextField(
                value = role,
                onValueChange = {},
                placeholder = { Text("Rol *") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            var expandedRole by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { expandedRole = true },
                    colors = ButtonDefaults.buttonColors(containerColor = orange),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Seleccionar rol", color = Color.White)
                }
                DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                    roles.forEach { r -> 
                        DropdownMenuItem(
                            text = { Text(r) }, 
                            onClick = { 
                                role = r
                                expandedRole = false 
                            }
                        ) 
                    }
                }
            }

            OutlinedTextField(
                value = estado,
                onValueChange = {},
                placeholder = { Text("Estado") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (user == null) {
                            viewModel.createUser(username.trim(), email.trim(), role.takeIf { it.isNotBlank() })
                        } else {
                            viewModel.updateUser(user.copy(name = username.trim(), email = email.trim(), avatar = role.takeIf { it.isNotBlank() }))
                        }
                        navController.popBackStack()
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
