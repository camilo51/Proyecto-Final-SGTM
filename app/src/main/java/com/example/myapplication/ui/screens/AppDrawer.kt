package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.example.myapplication.data.model.UserDto
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.util.Locale

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
    AppMenuItem(AppRoutes.Invoices, "Facturación", Icons.Filled.Info, adminOnly = true),
    AppMenuItem(AppRoutes.Reports, "Reportes", Icons.Filled.DateRange, adminOnly = true),
    AppMenuItem(AppRoutes.Audit, "Auditoría", Icons.Filled.Info, adminOnly = true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    navController: NavController,
    isAdmin: Boolean,
    userName: String? = null,
    currentUser: UserDto? = null,
    onOpenProfile: () -> Unit = {},
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
                DrawerHeader(
                    user = currentUser,
                    fallbackName = userName,
                    onOpenProfile = onOpenProfile
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))

                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "NAVEGACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )

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
                                shape = RoundedCornerShape(14.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

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
                            onLogout()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedIconColor = MaterialTheme.colorScheme.error,
                            unselectedTextColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
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
private fun DrawerHeader(
    user: UserDto?,
    fallbackName: String?,
    onOpenProfile: () -> Unit
) {
    val displayName = user?.name?.takeIf { it.isNotBlank() } ?: fallbackName ?: "Usuario SGTM"
    val role = user?.role?.let(::readableRole) ?: "Sesión activa"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenProfile)
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SgtmAvatar(
                user = user ?: UserDto(name = displayName),
                modifier = Modifier.size(52.dp)
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                user?.email?.takeIf { it.isNotBlank() }?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "SGTM · Gestión de taller",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SgtmAvatar(user: UserDto, modifier: Modifier = Modifier) {
    val initial = user.name
        ?.trim()
        ?.firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "?"

    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        if (!user.avatar.isNullOrBlank()) {
            AsyncImage(
                model = user.avatar,
                contentDescription = "Avatar de ${user.name.orEmpty()}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun readableRole(role: String): String {
    return when (role.trim().lowercase(Locale.ROOT)) {
        "admin", "administrador", "1" -> "Administrador"
        "recepcionista", "2" -> "Recepcionista"
        "tecnico", "técnico", "3" -> "Técnico"
        else -> role
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
