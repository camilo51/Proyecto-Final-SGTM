package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.navigation.AppRoutes
import com.example.myapplication.ui.theme.AppTheme
import kotlinx.coroutines.launch

data class AppMenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

val appMenuItems = listOf(
    AppMenuItem(AppRoutes.Admin, "Dashboard", Icons.Filled.Home),
    AppMenuItem(AppRoutes.Clients, "Clientes", Icons.Filled.Person, adminOnly = true),
    AppMenuItem(AppRoutes.Motorcycles, "Motocicletas", Icons.Filled.Build, adminOnly = true),
    AppMenuItem(AppRoutes.Orders, "Órdenes de trabajo", Icons.AutoMirrored.Filled.List, adminOnly = true),
    AppMenuItem(AppRoutes.Inventory, "Inventario", Icons.Filled.ShoppingCart, adminOnly = true),
    AppMenuItem(AppRoutes.Employees, "Empleados", Icons.Filled.AccountBox, adminOnly = true),
    AppMenuItem(AppRoutes.Users, "Usuarios", Icons.Filled.AccountCircle, adminOnly = true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    navController: NavController,
    isAdmin: Boolean,
    userName: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    onLogout: () -> Unit = {},
    appBar: (@Composable (onOpenDrawer: () -> Unit) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute by navController.currentBackStackEntryAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = MaterialTheme.colorScheme.onBackground,
                drawerTonalElevation = 0.dp
            ) {
                DrawerHeader(userName)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))

                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "NAVEGACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )

                    appMenuItems
                        .filter { !it.adminOnly || isAdmin }
                        .forEach { item ->
                            val isSelected = currentRoute?.destination?.route == item.route
                            NavigationDrawerItem(
                                label = { 
                                    Text(
                                        text = item.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ) 
                                },
                                icon = { 
                                    Icon(
                                        imageVector = item.icon, 
                                        contentDescription = item.label,
                                        modifier = Modifier.size(22.dp)
                                    ) 
                                },
                                selected = isSelected,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (currentRoute?.destination?.route != item.route) {
                                        navController.navigate(item.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    unselectedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                            )
                        }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión", fontWeight = FontWeight.Medium) },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedIconColor = MaterialTheme.colorScheme.error,
                            unselectedTextColor = MaterialTheme.colorScheme.error,
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (appBar != null) {
                    appBar { scope.launch { drawerState.open() } }
                } else {
                    TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Gestión de taller",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        MenuButton(onClick = { scope.launch { drawerState.open() } })
                    },
                    actions = {
                        NotificationButton()
                        actions()
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                    )
                }
            },
            content = content
        )
    }
}

@Composable
private fun MenuButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Abrir menú",
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
private fun NotificationButton() {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Box {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Ver notificaciones"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            text = "Notificaciones",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "No tienes novedades por ahora",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                leadingIcon = {
                    Icon(Icons.Filled.Notifications, contentDescription = null)
                },
                onClick = { expanded = false }
            )
        }
    }
}

@Composable
private fun DrawerHeader(userName: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = "SGTM",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Gestión de taller",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        if (!userName.isNullOrBlank()) {
            Text(
                text = "Sesión iniciada como $userName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Esta sección está en construcción",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppScaffoldPreview() {
    AppTheme {
        AppScaffold(
            title = "Inicio",
            navController = rememberNavController(),
            isAdmin = true,
            userName = "Admin"
        ) {
            PlaceholderScreen("Inicio")
        }
    }
}
