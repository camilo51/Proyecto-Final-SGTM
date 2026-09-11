package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LoginTheme
import com.example.myapplication.ui.viewmodel.DashboardViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    userName: String?,
    viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LoginTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                StaticBackground(modifier = Modifier.matchParentSize())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    TopSearchBar()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        DashboardHeader(userName)

                        MetricsSection(uiState)

                        FinancialSummarySection(uiState.totalSales)

                        ModulesSection(colorScheme)

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopSearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = { /* Menú */ }) {
            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = MaterialTheme.colorScheme.primary)
        }

        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            placeholder = { Text("Buscar clientes, motos, órdenes...", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White.copy(alpha = 0.5f)) },
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        IconButton(onClick = { /* Notificaciones */ }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
        }
    }
}

@Composable
private fun DashboardHeader(userName: String?) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    
    val greeting = when (hour) {
        in 6..12 -> "Buenos días"
        in 13..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    val dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale("es", "ES")) ?: ""
    val dayNum = calendar.get(Calendar.DAY_OF_MONTH)
    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "ES")) ?: ""

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "DASHBOARD EJECUTIVO • ${dayName.uppercase()}, $dayNum DE ${monthName.uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    append("$greeting, ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(userName ?: "Administrador")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("PDF")
                ActionButton("Excel")
            }
        }
        
        Button(
            onClick = { /* Nueva orden */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("NUEVA ORDEN DE TRABAJO", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ActionButton(text: String) {
    OutlinedButton(
        onClick = { /* Acción */ },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricsSection(uiState: com.example.myapplication.ui.viewmodel.DashboardUiState) {
    val orange = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardMetricCard(
                value = uiState.totalClients.toString(),
                label = "Total clientes",
                icon = Icons.Default.Person,
                topBorderColor = orange,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                value = uiState.totalMotos.toString(),
                label = "Total motos",
                icon = Icons.Default.Settings,
                topBorderColor = orange,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardMetricCard(
                value = uiState.activeOrders.toString(),
                label = "Órdenes activas",
                icon = Icons.Default.Edit,
                topBorderColor = orange,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                value = uiState.deliveredOrders.toString(),
                label = "Entregadas",
                icon = Icons.Default.CheckCircle,
                topBorderColor = orange,
                modifier = Modifier.weight(1f)
            )
        }
        DashboardMetricCard(
            value = uiState.lowStockCount.toString(),
            label = "Stock bajo",
            icon = Icons.Default.Warning,
            topBorderColor = orange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DashboardMetricCard(
    value: String,
    label: String,
    icon: ImageVector,
    topBorderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(topBorderColor)
                    .align(Alignment.TopStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(topBorderColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = topBorderColor, modifier = Modifier.size(22.dp))
                }

                Column {
                    Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun FinancialSummarySection(totalSales: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Resumen financiero", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Text(
                        "Histórico total",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$" + String.format(Locale.getDefault(), "%,.2f", totalSales),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Ventas totales acumuladas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ModulesSection(colorScheme: ColorScheme) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Operaciones del Taller",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        val modules = listOf(
            ModuleItem("Inventario", Icons.Default.ShoppingCart, "Piezas y stock", colorScheme.primary),
            ModuleItem("Clientes", Icons.Default.AccountBox, "Directorio JDS", colorScheme.primary),
            ModuleItem("Motos", Icons.Default.Settings, "Historial clínico", colorScheme.primary),
            ModuleItem("Facturación", Icons.Default.Edit, "Ventas y cobros", colorScheme.primary),
            ModuleItem("Personal", Icons.Default.Person, "Equipos técnicos", colorScheme.primary),
            ModuleItem("Reportes", Icons.Default.Star, "Rendimiento", colorScheme.primary)
        )

        // Usamos Column + Rows en lugar de LazyVerticalGrid fijo para evitar cortes en el scroll principal
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            modules.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    pair.forEach { module ->
                        PremiumModuleCard(module, Modifier.weight(1f))
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PremiumModuleCard(module: ModuleItem, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { /* Navegación */ },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(module.accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = null,
                    tint = module.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = module.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    maxLines = 1
                )
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

data class ModuleItem(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val accentColor: Color
)
