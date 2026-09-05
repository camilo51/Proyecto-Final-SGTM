package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(userName: String?) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "GESTIÓN JDS", 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                actions = {
                    IconButton(onClick = { /* Perfil */ }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera de Bienvenida
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            colorScheme.primary,
                                            colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName?.take(1)?.uppercase() ?: "A",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Panel Administrativo",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.primary
                            )
                            Text(
                                text = userName ?: "Administrador",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Sección de Estadísticas
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBadge("Órdenes", "12", Icons.Default.Build, colorScheme.primaryContainer, Modifier.weight(1f))
                    StatBadge("Citas", "05", Icons.Default.DateRange, colorScheme.secondaryContainer, Modifier.weight(1f))
                    StatBadge("Alertas", "03", Icons.Default.Warning, colorScheme.errorContainer, Modifier.weight(1f))
                }
            }

            // Título de Módulos
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Operaciones del Taller",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Módulos
            val modules = listOf(
                ModuleItem("Inventario", Icons.Default.ShoppingCart, "Piezas y stock", colorScheme.primary),
                ModuleItem("Clientes", Icons.Default.AccountBox, "Directorio JDS", colorScheme.secondary),
                ModuleItem("Motos", Icons.Default.Settings, "Historial clínico", colorScheme.tertiary),
                ModuleItem("Facturación", Icons.Default.Edit, "Ventas y cobros", Color(0xFF4CAF50)),
                ModuleItem("Personal", Icons.Default.Person, "Equipos técnicos", Color(0xFFFF9800)),
                ModuleItem("Reportes", Icons.Default.Star, "Rendimiento", Color(0xFF9C27B0))
            )

            items(modules) { module ->
                PremiumModuleCard(module)
            }
            
            // Espacio final
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StatBadge(label: String, value: String, icon: ImageVector, containerColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
        }
    }
}

@Composable
fun PremiumModuleCard(module: ModuleItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { /* Navegación */ },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(module.accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = null,
                    tint = module.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column {
                Text(
                    text = module.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

data class ModuleItem(
    val name: String, 
    val icon: ImageVector, 
    val description: String,
    val accentColor: Color
)
