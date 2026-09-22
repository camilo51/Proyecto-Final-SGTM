package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.ui.navigation.AppRoutes
import com.example.myapplication.ui.viewmodel.DashboardViewModel
import com.example.myapplication.ui.viewmodel.GlobalSearchResult
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    userName: String?,
    navController: NavController,
    isAdmin: Boolean,
    onLogout: () -> Unit,
    onSelectSearchResult: (GlobalSearchResult) -> Unit,
    currentUser: UserDto? = null,
    onOpenProfile: () -> Unit = {},
    viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AppScaffold(
        title = "Dashboard",
        navController = navController,
        isAdmin = isAdmin,
        userName = userName,
        currentUser = currentUser,
        onOpenProfile = onOpenProfile,
        onLogout = onLogout,
        appBar = { onOpenDrawer ->
            DashboardSearchBar(
                searchQuery = uiState.searchQuery,
                suggestions = uiState.searchResults,
                isLoading = uiState.isSearchLoading,
                onOpenDrawer = onOpenDrawer,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearch = { uiState.searchResults.firstOrNull()?.let(onSelectSearchResult) },
                onSelectResult = onSelectSearchResult
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    DashboardHeader(userName, navController)

                    MetricsSection(uiState)

                    FinancialSummarySection(uiState.totalSales)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardSearchBar(
    searchQuery: String,
    suggestions: List<GlobalSearchResult>,
    isLoading: Boolean,
    onOpenDrawer: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectResult: (GlobalSearchResult) -> Unit
) {
    var suggestionsExpanded by rememberSaveable {
        mutableStateOf(searchQuery.isNotBlank() && suggestions.isNotEmpty())
    }
    val submitSearch: () -> Unit = {
        onSearch()
        suggestionsExpanded = false
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onOpenDrawer) {
            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = MaterialTheme.colorScheme.primary)
        }

        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { value ->
                    onQueryChange(value)
                    suggestionsExpanded = value.isNotBlank()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("global-search"),
                placeholder = { Text("Buscar en todos los módulos...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = {
                    IconButton(onClick = submitSearch) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar en todos los módulos",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                trailingIcon = {
                    if (isLoading && searchQuery.isNotBlank()) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            onQueryChange("")
                            suggestionsExpanded = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )

            DropdownMenu(
                expanded = suggestionsExpanded && searchQuery.isNotBlank(),
                onDismissRequest = { suggestionsExpanded = false },
                modifier = Modifier.fillMaxWidth(),
                properties = PopupProperties(focusable = false)
            ) {
                when {
                    isLoading -> DropdownMenuItem(
                        text = { Text("Cargando información…") },
                        onClick = { suggestionsExpanded = false },
                        enabled = false
                    )
                    suggestions.isEmpty() -> DropdownMenuItem(
                        text = { Text("No se encontraron resultados") },
                        onClick = { suggestionsExpanded = false },
                        enabled = false
                    )
                    else -> suggestions.forEach { result ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = result.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = listOf(result.module.label, result.subtitle.orEmpty())
                                            .filter(String::isNotBlank)
                                            .joinToString(" · "),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                suggestionsExpanded = false
                                onSelectResult(result)
                            }
                        )
                    }
                }
            }
        }

        IconButton(onClick = { /* Notificaciones */ }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DashboardHeader(userName: String?, navController: NavController) {
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
                color = MaterialTheme.colorScheme.onBackground
            )

        }
        
        Button(
            onClick = { navController.navigate(AppRoutes.CreateOrder) },
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                    Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun FinancialSummarySection(totalSales: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Resumen financiero", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ventas totales acumuladas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
