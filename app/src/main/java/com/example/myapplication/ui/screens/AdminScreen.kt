package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LoginTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(userName: String?) {
    LoginTheme {
        val colorScheme = MaterialTheme.colorScheme

        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo estático estrictamente Naranja y Blanco sobre Oscuro
            StaticBackground(modifier = Modifier.matchParentSize())

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StaticLogo(size = 32.dp)
                                Text(
                                    "SGTM JDS",
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* Perfil */ }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Perfil",
                                    tint = colorScheme.primary, // Naranja
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White // Blanco
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
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    colorScheme.primary,
                                                    colorScheme.primary.copy(alpha = 0.7f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userName?.take(1)?.uppercase() ?: "A",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Panel Administrativo",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colorScheme.primary // Naranja
                                    )
                                    Text(
                                        text = userName ?: "Administrador",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White // Blanco
                                    )
                                }
                            }
                        }
                    }

                    // Sección de Estadísticas - Unificada a Naranja/Gris
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatBadge("Órdenes", "12", Icons.Default.Build, colorScheme.primaryContainer, Modifier.weight(1f))
                            StatBadge("Citas", "05", Icons.Default.DateRange, colorScheme.primaryContainer.copy(alpha = 0.8f), Modifier.weight(1f))
                            StatBadge("Alertas", "03", Icons.Default.Warning, colorScheme.primaryContainer.copy(alpha = 0.6f), Modifier.weight(1f))
                        }
                    }

                    // Título de Módulos
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Operaciones del Taller",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Módulos - Todos con acento Naranja
                    val modules = listOf(
                        ModuleItem("Inventario", Icons.Default.ShoppingCart, "Piezas y stock", colorScheme.primary),
                        ModuleItem("Clientes", Icons.Default.AccountBox, "Directorio JDS", colorScheme.primary),
                        ModuleItem("Motos", Icons.Default.Settings, "Historial clínico", colorScheme.primary),
                        ModuleItem("Facturación", Icons.Default.Edit, "Ventas y cobros", colorScheme.primary),
                        ModuleItem("Personal", Icons.Default.Person, "Equipos técnicos", colorScheme.primary),
                        ModuleItem("Reportes", Icons.Default.Star, "Rendimiento", colorScheme.primary)
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
    }
}

@Composable
private fun StaticBackground(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val orange = colorScheme.primary

    Canvas(modifier = modifier) {
        val maxDimension = maxOf(size.width, size.height)

        fun drawGlow(
            color: Color,
            center: Offset,
            radius: Float,
            alpha: Float,
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        color.copy(alpha = alpha * 0.35f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = radius,
                ),
                center = center,
                radius = radius,
            )
        }

        // Destellos únicamente Naranjas para coincidir con el Login
        drawGlow(
            color = orange,
            center = Offset(size.width * 0.15f, size.height * 0.2f),
            radius = maxDimension * 0.5f,
            alpha = 0.18f,
        )
        drawGlow(
            color = orange,
            center = Offset(size.width * 0.85f, size.height * 0.8f),
            radius = maxDimension * 0.45f,
            alpha = 0.12f,
        )

        // Estrellas Blancas estáticas
        val stars = listOf(
            Offset(0.12f, 0.18f), Offset(0.35f, 0.12f), Offset(0.55f, 0.45f),
            Offset(0.75f, 0.25f), Offset(0.92f, 0.55f), Offset(0.25f, 0.75f),
            Offset(0.45f, 0.95f), Offset(0.65f, 0.65f), Offset(0.85f, 0.85f),
            Offset(0.08f, 0.88f), Offset(0.48f, 0.43f), Offset(0.88f, 0.08f)
        )
        stars.forEach { pos ->
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = 1.2.dp.toPx(),
                center = Offset(size.width * pos.x, size.height * pos.y)
            )
        }
    }
}

@Composable
private fun StaticLogo(size: Dp) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(8.dp),
        color = primary,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "S",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
fun StatBadge(label: String, value: String, icon: ImageVector, containerColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = containerColor.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PremiumModuleCard(module: ModuleItem) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { /* Navegación */ },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
                    .background(module.accentColor.copy(alpha = 0.15f)),
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
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = colorScheme.onSurface
                )
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
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
