package com.example.myapplication.ui.screens.motorcycles

import androidx.compose.foundation.BorderStroke
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.viewmodel.MotorcycleViewModel

data class MockMotorcycle(
    val plate: String,
    val model: String,
    val year: String,
    val owner: String,
    val status: String
)

val mockMotorcycles = listOf(
    MockMotorcycle("ABC123", "Honda CB190R", "2021", "Andrés", "En servicio"),
    MockMotorcycle("DEF456", "Yamaha FZ25", "2022", "Carlos", "En servicio"),
    MockMotorcycle("GHI789", "Kawasaki Ninja 400", "2023", "Felipe", "En reparación")
)

@Composable
fun MotorcyclesScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onOpenMotorcycle: (String) -> Unit = {},
    onCreateMotorcycle: () -> Unit = {},
    onEditMotorcycle: (String) -> Unit = {},
    viewModel: MotorcycleViewModel = viewModel()
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
                        text = "Motocicletas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Gestión de motocicletas registradas en el taller.",
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
                        onClick = onCreateMotorcycle,
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = orange),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("+ Nueva motocicle..", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
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
                    item { MotorcycleStatCard("Registradas", "5") }
                    item { MotorcycleStatCard("En servicio", "4") }
                    item { MotorcycleStatCard("En reparaci..", "0") }
                }

                // Search Bar
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar motocicletas...", color = Color.White.copy(alpha = 0.4f)) },
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
                    MotorcycleFilterChip("Estado: Todos")
                    MotorcycleFilterChip("Marca: Todas")
                }

                // Motorcycle Cards List
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    mockMotorcycles.forEach { moto ->
                        MotorcycleItemCard(moto)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun MotorcycleStatCard(label: String, value: String) {
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
private fun MotorcycleFilterChip(text: String) {
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
fun MotorcycleStatusChip(status: String?) {
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = status ?: "No registrado",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MotorcycleItemCard(moto: MockMotorcycle) {
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
                        text = moto.plate,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = moto.model,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = moto.year,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                // Status Badge
                MotorcycleStatusChip(moto.status)
            }

            Text(
                text = "Propietario: ${moto.owner}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver",
                    modifier = Modifier
                        .clickable { /* logic */ }
                        .padding(8.dp),
                    color = orange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
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
