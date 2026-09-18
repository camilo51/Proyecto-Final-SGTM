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
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.viewmodel.EmployeeViewModel
import com.example.myapplication.data.model.Employee

@Composable
fun EmployeeFormScreen(
    employee: Employee? = null,
    onBack: () -> Unit = {},
    viewModel: EmployeeViewModel = viewModel()
) {
    val orange = MaterialTheme.colorScheme.primary

    var tipoDocumento by remember { mutableStateOf("CC") }
    var documento by remember { mutableStateOf("80123456") }
    var nombre by remember { mutableStateOf(employee?.name?.split(" ")?.firstOrNull() ?: "Jorge") }
    var apellido by remember { mutableStateOf(employee?.name?.split(" ")?.getOrNull(1) ?: "Pérez") }
    var telefono by remember { mutableStateOf(employee?.phone ?: "3001112233") }
    var correo by remember { mutableStateOf(employee?.email ?: "empleado@sgtm.test") }
    var especialidad by remember { mutableStateOf(employee?.role ?: "Mecánica general") }
    var descripcion by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("unidad") }
    var cantidadInicial by remember { mutableStateOf("0") }
    var stockMinimo by remember { mutableStateOf("5") }
    var precioCompra by remember { mutableStateOf("") }

    val tipos = listOf("CC", "CE", "TI")
    val specialties = listOf("Mecánica general", "Mecánica básica", "Mecánica eléctrica")

    AppTheme(darkTheme = true) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text("Nuevo empleado", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Gestión de taller", style = MaterialTheme.typography.labelSmall, color = orange)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("El estado se calculará en el servidor según el stock.", color = Color.White.copy(alpha = 0.6f))

                OutlinedTextField(
                    value = documento,
                    onValueChange = { documento = it },
                    label = { Text("Código") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Marca") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Botón naranja para seleccionar categoría (visual)
                Button(
                    onClick = { /* abrir selección */ },
                    colors = ButtonDefaults.buttonColors(containerColor = orange),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Seleccionar categoría", color = Color.White)
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = unidad,
                    onValueChange = { unidad = it },
                    label = { Text("Unidad") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cantidadInicial,
                    onValueChange = { cantidadInicial = it },
                    label = { Text("Cantidad inicial") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stockMinimo,
                    onValueChange = { stockMinimo = it },
                    label = { Text("Stock mínimo") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = precioCompra,
                    onValueChange = { precioCompra = it },
                    label = { Text("Precio de compra") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            // Reusar ViewModel para crear empleado con campos mínimos que espera el modelo
                            val fullName = "${nombre.trim()} ${apellido.trim()}".trim()
                            viewModel.createEmployee(fullName, correo.trim(), telefono.trim(), especialidad)
                            onBack()
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
}
