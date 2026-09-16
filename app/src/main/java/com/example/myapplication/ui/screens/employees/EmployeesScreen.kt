package com.example.myapplication.ui.screens.employees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppTheme

data class Employee(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val correo: String,
    val especialidad: String,
    val comision: String,
    val estado: String
)

val mockEmployees = listOf(
    Employee("77195816", "José", "Aparicio", "3008909195", "jogeapa@hotmail.com", "Mecánica General", "0%", "Activo"),
    Employee("1076890876", "Isaac", "Vento", "3147929430", "—", "Mecánica general", "50%", "Activo"),
    Employee("13345139", "Chunt", "Vento", "3045624769", "—", "Mecánica básica", "40%", "Activo")
)

@Composable
fun EmployeesScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val orange = MaterialTheme.colorScheme.primary

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
                        onClick = { /* Nuevo */ },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = orange),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("+ Nuevo empleado", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = { /* Actualizar */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Actualizar", fontSize = 13.sp, color = Color.White)
                    }
                }

                // Stats Cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { StatMiniCard("Registrados", "3") }
                    item { StatMiniCard("Activos", "3") }
                    item { StatMiniCard("En turno", "2") }
                }

                // Search Bar
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar empleados...", color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.4f)) },
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

                // Filters
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SimpleFilterChip("Especialidad: Todas")
                    SimpleFilterChip("Estado: Todos")
                }

                // Employee Cards List
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    mockEmployees.forEach { employee ->
                        EmployeeCard(employee)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
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
private fun SimpleFilterChip(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun EmployeeCard(employee: Employee) {
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
                        text = "${employee.nombre} ${employee.apellido}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = employee.especialidad,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "Doc: ${employee.documento}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                // Status Badge
                Surface(
                    color = Color(0xFFDCFCE7).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.2f))
                ) {
                    Text(
                        text = employee.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.width(6.dp))
                Text(employee.telefono, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ganancias",
                    modifier = Modifier
                        .clickable { /* logic */ }
                        .padding(8.dp),
                    color = orange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Editar",
                    modifier = Modifier
                        .clickable { /* logic */ }
                        .padding(8.dp),
                    color = orange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
