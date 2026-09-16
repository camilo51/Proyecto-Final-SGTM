package com.example.myapplication.ui.screens.clients

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.viewmodel.ClientViewModel

data class MockClient(
    val name: String,
    val phone: String,
    val email: String,
    val motosCount: Int
)

val mockClients = listOf(
    MockClient("Andrés Pérez", "3001234567", "andres@example.com", 2),
    MockClient("Carlos Ruiz", "3109876543", "carlos@example.com", 1),
    MockClient("Felipe Gómez", "3201112233", "felipe@example.com", 3)
)

@Composable
fun ClientsScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: ClientViewModel = viewModel()
) {
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
                        text = "Clientes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Gestión de clientes registrados en el taller.",
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
                        onClick = { /* logic */ },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = orange),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("+ Nuevo cliente", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = { /* logic */ },
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
                    item { ClientStatCard("Registrados", "95") }
                    item { ClientStatCard("Activos", "80") }
                    item { ClientStatCard("Nuevos (Mes)", "12") }
                }

                // Search Bar
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar clientes...", color = Color.White.copy(alpha = 0.4f)) },
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

                // Client Cards List
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    mockClients.forEach { client ->
                        ClientItemCard(client)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ClientStatCard(label: String, value: String) {
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
private fun ClientItemCard(client: MockClient) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = client.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${client.motosCount} Motocicletas registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = orange,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(client.phone, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(client.email, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { /* logic */ }) {
                    Text("Ver Historial", color = orange, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { /* logic */ }) {
                    Text("Editar", color = orange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
