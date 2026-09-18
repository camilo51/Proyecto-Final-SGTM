package com.example.myapplication.ui.screens.employees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.myapplication.ui.viewmodel.EmployeeViewModel
import com.example.myapplication.data.model.Employee

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BackLink(onClick = { navController.popBackStack() })

            Text(
                text = "Registrar empleado",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "El estado se calculará en el servidor según el desempeño.",
                color = Color.White.copy(alpha = 0.6f)
            )

            OutlinedTextField(
                value = documento,
                onValueChange = { documento = it },
                placeholder = { Text("Documento / Código") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                placeholder = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = especialidad,
                onValueChange = { especialidad = it },
                placeholder = { Text("Especialidad") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            // Botón naranja para seleccionar (visual, imitando la imagen)
            Button(
                onClick = { /* abrir selección */ },
                colors = ButtonDefaults.buttonColors(containerColor = orange),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Seleccionar especialidad", color = Color.White)
            }

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                placeholder = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = { Text("Descripción / Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                        val fullName = "${nombre.trim()} ${apellido.trim()}".trim()
                        if (employee == null) {
                            viewModel.createEmployee(fullName, correo.trim(), telefono.trim(), especialidad)
                        } else {
                            viewModel.updateEmployee(employee.copy(name = fullName, email = correo.trim(), phone = telefono.trim(), role = especialidad))
                        }
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = orange),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar empleado", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
