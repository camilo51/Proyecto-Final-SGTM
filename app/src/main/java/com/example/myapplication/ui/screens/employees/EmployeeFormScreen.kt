package com.example.myapplication.ui.screens.employees

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
import com.example.myapplication.data.model.Employee
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.screens.BackNavigationLink
import com.example.myapplication.ui.screens.FormHeader
import com.example.myapplication.ui.screens.FormPrimaryButton
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.EmployeeViewModel

@Composable
fun EmployeeFormScreen(
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    employee: Employee? = null,
    viewModel: EmployeeViewModel = viewModel()
) {
    val orange = MaterialTheme.colorScheme.primary

    var documento by remember { mutableStateOf(employee?.id ?: "80123456") }
    var nombre by remember { mutableStateOf(employee?.name?.split(" ")?.firstOrNull() ?: "Jorge") }
    var apellido by remember { mutableStateOf(employee?.name?.split(" ")?.getOrNull(1) ?: "Pérez") }
    var telefono by remember { mutableStateOf(employee?.phone ?: "3001112233") }
    var correo by remember { mutableStateOf(employee?.email ?: "empleado@sgtm.test") }
    var especialidad by remember { mutableStateOf(employee?.role ?: "Mecánica general") }
    var descripcion by remember { mutableStateOf("") }

    val specialties = listOf("Mecánica general", "Mecánica básica", "Mecánica eléctrica")

    AppScaffold(
        title = if (employee == null) "Nuevo empleado" else "Editar empleado",
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
                title = if (employee == null) "Registrar empleado" else "Editar empleado",
                subtitle = "El estado se calculará en el servidor según el desempeño.",
                onBack = { navController.popBackStack() }
            )

            OutlinedTextField(
                value = documento,
                onValueChange = { documento = it },
                placeholder = { Text("Documento / Código") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppOutlinedTextFieldColors()
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppOutlinedTextFieldColors()
            )

            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                placeholder = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppOutlinedTextFieldColors()
            )

            var expandedEspecialidad by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = especialidad,
                    onValueChange = {},
                    placeholder = { Text("Especialidad") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expandedEspecialidad = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar especialidad")
                        }
                    },
                    colors = AppOutlinedTextFieldColors()
                )
                DropdownMenu(expanded = expandedEspecialidad, onDismissRequest = { expandedEspecialidad = false }) {
                    specialties.forEach { item ->
                        DropdownMenuItem(text = { Text(item) }, onClick = { especialidad = item; expandedEspecialidad = false })
                    }
                }
            }

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppOutlinedTextFieldColors()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                placeholder = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                colors = AppOutlinedTextFieldColors()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = { Text("Descripción / Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = AppOutlinedTextFieldColors()
            )

            FormPrimaryButton(
                text = "Guardar empleado",
                onClick = {
                    val fullName = "${nombre.trim()} ${apellido.trim()}".trim()
                    if (employee == null) {
                        viewModel.createEmployee(fullName, correo.trim(), telefono.trim(), especialidad)
                    } else {
                        viewModel.updateEmployee(employee.copy(name = fullName, email = correo.trim(), phone = telefono.trim(), role = especialidad))
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
