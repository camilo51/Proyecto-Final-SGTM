package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.AppTheme
import kotlinx.coroutines.launch

data class AppMenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

val appMenuItems = listOf(
    AppMenuItem("home", "Inicio", Icons.Filled.Home),
    AppMenuItem("appointments", "Citas", Icons.Filled.DateRange),
    AppMenuItem("motorcycles", "Motocicletas", Icons.Filled.Build),
    AppMenuItem("reminders", "Recordatorios", Icons.Filled.Notifications),
    AppMenuItem("clients", "Clientes", Icons.Filled.Person, adminOnly = true),
    AppMenuItem("employees", "Empleados", Icons.Filled.Face, adminOnly = true),
    AppMenuItem("brands", "Marcas", Icons.Filled.Star, adminOnly = true),
    AppMenuItem("inventory", "Inventario", Icons.AutoMirrored.Filled.List, adminOnly = true),
    AppMenuItem("orders", "Órdenes", Icons.Filled.ShoppingCart, adminOnly = true),
    AppMenuItem("invoices", "Facturas", Icons.Filled.Email, adminOnly = true),
    AppMenuItem("reports", "Reportes", Icons.Filled.Info, adminOnly = true),
    AppMenuItem("users", "Usuarios", Icons.Filled.AccountCircle, adminOnly = true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    navController: NavController,
    isAdmin: Boolean,
    userName: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute by navController.currentBackStackEntryAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(userName)
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Column(Modifier.verticalScroll(rememberScrollState())) {
                    appMenuItems
                        .filter { !it.adminOnly || isAdmin }
                        .forEach { item ->
                            NavigationDrawerItem(
                                label = { Text(item.label) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                selected = currentRoute?.destination?.route == item.route,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (currentRoute?.destination?.route != item.route) {
                                        navController.navigate(item.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión"
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    actions = {
                        actions()
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    }
                )
            },
            content = content
        )
    }
}

@Composable
private fun DrawerHeader(userName: String?) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "SGTM",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (!userName.isNullOrBlank()) {
            Text(
                text = userName,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Esta sección está en construcción",
            fontSize = 14.sp,
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
