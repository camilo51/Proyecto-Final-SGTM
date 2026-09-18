package com.example.myapplication.ui.screens.employees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.example.myapplication.data.model.Employee
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.viewmodel.EmployeeViewModel

@Composable
fun EmployeesScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: EmployeeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val orange = MaterialTheme.colorScheme.primary

    var showDialog by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }

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
                        text = "Empleados",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Gestión del personal técnico y administrativo del taller.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { 
                            selectedEmployee = null
                            showDialog = true 
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = orange),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("+ Nuevo empleado", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = { viewModel.refreshEmployees() },
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
                    item { StatMiniCard("Registrados", state.employees.size.toString()) }
                    item { StatMiniCard("Filtrados", state.filteredEmployees.size.toString()) }
                }

                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar empleados...", color = Color.White.copy(alpha = 0.4f)) },
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

                // Employee Cards List
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = orange)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        state.visibleEmployees.forEach { employee ->
                            EmployeeCard(
                                employee = employee,
                                onEdit = {
                                    selectedEmployee = employee
                                    showDialog = true
                                },
                                onDelete = { employee.id?.let { viewModel.deleteEmployee(it) } }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showDialog) {
            EmployeeFormDialog(
                employee = selectedEmployee,
                onDismiss = { 
                    showDialog = false
                    viewModel.clearOperationMessage()
                },
                onSave = { name, email, phone, role ->
                    if (selectedEmployee == null) {
                        viewModel.createEmployee(name, email, phone, role)
                    } else {
                        viewModel.updateEmployee(selectedEmployee!!.copy(name = name, email = email, phone = phone, role = role))
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
private fun EmployeeFormDialog(
    employee: Employee?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    isSaving: Boolean,
    operationMessage: String?
) {
    // Map UI fields into existing model: name = "$nombre $apellido", role = especialidad
    var tipoDocumento by remember { mutableStateOf("CC") }
    var documento by remember { mutableStateOf("80123456") }
    var nombre by remember { mutableStateOf(employee?.name?.split(" ")?.firstOrNull() ?: "Jorge") }
    var apellido by remember { mutableStateOf(employee?.name?.split(" ")?.getOrNull(1) ?: "Pérez") }
    var email by remember { mutableStateOf(employee?.email ?: "empleado@sgtm.test") }
    var phone by remember { mutableStateOf(employee?.phone ?: "3001112233") }
    var especialidad by remember { mutableStateOf(employee?.role ?: "Mecánica general") }
    var tarifaDiaria by remember { mutableStateOf("80000") }
    var comision by remember { mutableStateOf("60") }
    var estado by remember { mutableStateOf("Activo") }
    var fechaContratacion by remember { mutableStateOf("18/09/2026") }

    val tipos = listOf("CC", "CE", "TI")
    val specialties = listOf("Mecánica general", "Mecánica básica", "Mecánica eléctrica")
    val estados = listOf("Activo", "Inactivo")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employee == null) "Nuevo empleado" else "Editar empleado") },
        text = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Documento
                    var expandedTipo by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = tipoDocumento,
                            onValueChange = {},
                            label = { Text("Tipo de documento *") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expandedTipo = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                            }
                        )
                        DropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
                            tipos.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { tipoDocumento = t; expandedTipo = false }) }
                        }
                    }
                    OutlinedTextField(value = documento, onValueChange = { documento = it }, label = { Text("Documento *") }, modifier = Modifier.fillMaxWidth())

                    // Nombre / Apellido
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido *") }, modifier = Modifier.fillMaxWidth())

                    // Teléfono / Correo
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth())

                    // Especialidad
                    var expandedSpec by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = especialidad,
                            onValueChange = {},
                            label = { Text("Especialidad *") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { expandedSpec = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } }
                        )
                        DropdownMenu(expanded = expandedSpec, onDismissRequest = { expandedSpec = false }) {
                            specialties.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { especialidad = s; expandedSpec = false }) }
                        }
                    }

                    // Tarifa diaria / Comisión
                    OutlinedTextField(value = tarifaDiaria, onValueChange = { tarifaDiaria = it }, label = { Text("Tarifa diaria (COP)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = comision, onValueChange = { comision = it }, label = { Text("Comisión (%)") }, modifier = Modifier.fillMaxWidth())

                    // Estado / Fecha de contratación
                    var expandedEstado by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = estado,
                            onValueChange = {},
                            label = { Text("Estado") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { expandedEstado = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } }
                        )
                        DropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }) {
                            estados.forEach { e -> DropdownMenuItem(text = { Text(e) }, onClick = { estado = e; expandedEstado = false }) }
                        }
                    }
                    OutlinedTextField(value = fechaContratacion, onValueChange = { fechaContratacion = it }, label = { Text("Fecha de contratación *") }, modifier = Modifier.fillMaxWidth())

                    operationMessage?.let {
                        Text(it, color = if (it.contains("correctamente")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },

        confirmButton = {
            Button(
                onClick = { onSave("${nombre.trim()} ${apellido.trim()}".trim(), email.trim(), phone.trim(), especialidad) },
                enabled = !isSaving && nombre.isNotBlank() && apellido.isNotBlank()
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Guardar empleado")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}

@Composable
private fun StatMiniCard(label: String, value: String) {
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
private fun EmployeeCard(
    employee: Employee,
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
                        text = employee.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = employee.role,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "ID: ${employee.id ?: "—"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.width(6.dp))
                Text(employee.phone, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
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
