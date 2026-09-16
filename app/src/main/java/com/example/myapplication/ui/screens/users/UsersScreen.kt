package com.example.myapplication.ui.screens.users

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppTheme

data class UserAccount(
    val usuario: String,
    val correo: String,
    val rol: String,
    val estado: String,
    val ultimoAcceso: String,
    val fechaCreacion: String
)

val mockUsers = listOf(
    UserAccount("JOSÉ", "jose@example.com", "Administrador", "Activo", "15 de sept, 08:05 pm", "03 de ago de 2026"),
    UserAccount("Danilo", "danilo@example.com", "Administrador", "Activo", "16 de sept, 06:27 am", "03 de ago de 2026"),
    UserAccount("Sgtm Admin", "admin@enginesjds.com", "Administrador", "Activo", "Hoy, 10:57 pm", "02 de ago de 2026")
)

@Composable
fun UsersScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
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
                        onClick = { /* Nuevo */ },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = orange),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("+ Nuevo usuario", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
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
                    item { UserStatMiniCard("Total", "3") }
                    item { UserStatMiniCard("Activos", "3") }
                    item { UserStatMiniCard("Admins", "3") }
                }

                // Search Bar
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar usuarios...", color = Color.White.copy(alpha = 0.4f)) },
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

                // User Cards List
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    mockUsers.forEach { user ->
                        UserCard(user)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
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
private fun UserCard(user: UserAccount) {
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
                        text = user.usuario,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = user.correo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                // Role Badge
                Surface(
                    color = Color(0xFFF3E8FF).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.2f))
                ) {
                    Text(
                        text = user.rol,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFFD8B4FE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Acceso: ${user.ultimoAcceso}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Creado: ${user.fechaCreacion}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { /* logic */ }) {
                    Text("Editar", color = orange, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { /* logic */ }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
