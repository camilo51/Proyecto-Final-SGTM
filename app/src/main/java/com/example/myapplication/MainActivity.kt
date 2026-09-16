package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.ui.screens.AdminScreen
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.inventory.CreateInventoryScreen
import com.example.myapplication.ui.screens.inventory.EditInventoryScreen
import com.example.myapplication.ui.screens.inventory.InventoryDetailScreen
import com.example.myapplication.ui.screens.inventory.InventoryListScreen
import com.example.myapplication.ui.screens.inventory.InventoryMovementsScreen
import com.example.myapplication.ui.screens.invoices.InvoicesScreen
import com.example.myapplication.ui.screens.invoices.InvoiceDetailScreen
import com.example.myapplication.ui.screens.PlaceholderScreen
import com.example.myapplication.ui.screens.clients.ClientsScreen
import com.example.myapplication.ui.screens.motorcycles.CreateMotorcycleScreen
import com.example.myapplication.ui.screens.motorcycles.EditMotorcycleScreen
import com.example.myapplication.ui.screens.motorcycles.MotorcycleDetailScreen
import com.example.myapplication.ui.screens.motorcycles.MotorcyclesScreen
import com.example.myapplication.ui.screens.orders.CreateOrderScreen
import com.example.myapplication.ui.screens.orders.EditOrderScreen
import com.example.myapplication.ui.screens.orders.OrderDetailScreen
import com.example.myapplication.ui.screens.orders.OrdersListScreen
import com.example.myapplication.ui.screens.reports.ReportsScreen
import com.example.myapplication.ui.navigation.AppRoutes
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.viewmodel.ClientViewModel
import com.example.myapplication.ui.viewmodel.LoginViewModel
import com.example.myapplication.ui.viewmodel.MotorcycleViewModel
import com.example.myapplication.ui.viewmodel.OrderViewModel
import com.example.myapplication.ui.viewmodel.InvoiceViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            AppTheme(darkTheme = true) {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val clientViewModel: ClientViewModel = viewModel()
    val motorcycleViewModel: MotorcycleViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val invoiceViewModel: InvoiceViewModel = viewModel()
    val uiState by loginViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.accessToken) {
        RetrofitClient.setAuthorizationToken(uiState.accessToken)
    }

    val onLogout: () -> Unit = {
        loginViewModel.logout {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    if (loginViewModel.uiState.value.isAdmin) {
                        navController.navigate("admin") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("admin") {
            AdminScreen(
                userName = uiState.user?.name,
                navController = navController,
                isAdmin = uiState.isAdmin,
                onLogout = onLogout
            )
        }
        composable("inventory") {
            AdminOnlyRoute(uiState.isAdmin, navController) {
                InventoryListScreen(navController, uiState.user?.name, onLogout)
            }
        }
        composable("inventory/create") {
            AdminOnlyRoute(uiState.isAdmin, navController) {
                CreateInventoryScreen(navController, uiState.user?.name, onLogout)
            }
        }
        composable("inventory/detail/{id}") { entry ->
            entry.arguments?.getString("id")?.toLongOrNull()?.let { id ->
                AdminOnlyRoute(uiState.isAdmin, navController) {
                    InventoryDetailScreen(id, navController, uiState.user?.name, onLogout)
                }
            }
        }
        composable("inventory/edit/{id}") { entry ->
            entry.arguments?.getString("id")?.toLongOrNull()?.let { id ->
                AdminOnlyRoute(uiState.isAdmin, navController) {
                    EditInventoryScreen(id, navController, uiState.user?.name, onLogout)
                }
            }
        }
        composable("inventory/movements/{id}/{action}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull()
            val action = entry.arguments?.getString("action") ?: "history"
            if (id != null) {
                AdminOnlyRoute(uiState.isAdmin, navController) {
                    InventoryMovementsScreen(id, action, navController, uiState.user?.name, onLogout)
                }
            }
        }
        composable("home") {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido Usuario Estándar", 
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Rol detectado: ${uiState.user?.role ?: "No definido"}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        composable(AppRoutes.Clients) {
            if (uiState.isAdmin) {
                AppScaffold(
                    title = "Clientes",
                    navController = navController,
                    isAdmin = true,
                    userName = uiState.user?.name,
                    onLogout = {
                        loginViewModel.logout {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                ) { padding ->
                    ClientsScreen(
                        contentPadding = padding,
                        viewModel = clientViewModel
                    )
                }
            } else {
                PlaceholderScreen("No tienes permisos para acceder a clientes")
            }
        }
        composable(AppRoutes.Orders) {
            if (uiState.isAdmin) {
                AppScaffold(
                    title = "Órdenes de trabajo",
                    navController = navController,
                    isAdmin = true,
                    userName = uiState.user?.name,
                    onLogout = {
                        loginViewModel.logout {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                ) { padding ->
                    OrdersListScreen(
                        contentPadding = padding,
                        onOpenOrder = { id -> navController.navigate(AppRoutes.orderDetail(id)) },
                        onCreateOrder = { navController.navigate(AppRoutes.CreateOrder) },
                        onEditOrder = { id -> navController.navigate(AppRoutes.editOrder(id)) },
                        viewModel = orderViewModel
                    )
                }
            } else {
                PlaceholderScreen("No tienes permisos para acceder a órdenes")
            }
        }
        composable(AppRoutes.Reports) {
            if (uiState.isAdmin) {
                AppScaffold(
                    title = "Reportes",
                    navController = navController,
                    isAdmin = true,
                    userName = uiState.user?.name,
                    onLogout = onLogout
                ) { padding ->
                    ReportsScreen(
                        contentPadding = padding,
                        onOpenInventory = { id -> navController.navigate("inventory/detail/$id") }
                    )
                }
            } else {
                PlaceholderScreen("No tienes permisos para acceder a reportes")
            }
        }
        composable(AppRoutes.Invoices) {
            if (uiState.isAdmin) {
                AppScaffold(
                    title = "Facturación",
                    navController = navController,
                    isAdmin = true,
                    userName = uiState.user?.name,
                    onLogout = onLogout
                ) { padding ->
                    InvoicesScreen(
                        contentPadding = padding,
                        onOpenInvoice = { id -> navController.navigate(AppRoutes.invoiceDetail(id)) },
                        viewModel = invoiceViewModel
                    )
                }
            } else {
                PlaceholderScreen("No tienes permisos para acceder a facturación")
            }
        }
        composable(AppRoutes.InvoiceDetail) { entry ->
            val invoiceId = entry.arguments?.getString("invoiceId")
            if (uiState.isAdmin && invoiceId != null) {
                AppScaffold(
                    title = "Factura",
                    navController = navController,
                    isAdmin = true,
                    userName = uiState.user?.name,
                    onLogout = onLogout
                ) { padding ->
                    InvoiceDetailScreen(
                        contentPadding = padding,
                        invoiceId = invoiceId,
                        onBack = navController::popBackStack,
                        viewModel = invoiceViewModel
                    )
                }
            } else {
                PlaceholderScreen("No tienes permisos para acceder a esta factura")
            }
        }
        composable(AppRoutes.Motorcycles) {
            if (uiState.isAdmin) {
                AppScaffold(
                    title = "Motocicletas",
                    navController = navController,
                    isAdmin = true,
                    userName = uiState.user?.name,
                    onLogout = onLogout
                ) { padding ->
                    MotorcyclesScreen(
                        contentPadding = padding,
                        onOpenMotorcycle = { id -> navController.navigate(AppRoutes.motorcycleDetail(id)) },
                        onCreateMotorcycle = { navController.navigate(AppRoutes.CreateMotorcycle) },
                        onEditMotorcycle = { id -> navController.navigate(AppRoutes.editMotorcycle(id)) },
                        viewModel = motorcycleViewModel
                    )
                }
            } else {
                PlaceholderScreen("No tienes permisos para acceder a motocicletas")
            }
        }
        composable(AppRoutes.CreateMotorcycle) {
            if (uiState.isAdmin) {
                CreateMotorcycleScreen(
                    navController = navController,
                    userName = uiState.user?.name,
                    onLogout = onLogout,
                    onCreated = { id ->
                        navController.navigate(AppRoutes.motorcycleDetail(id)) {
                            popUpTo(AppRoutes.CreateMotorcycle) { inclusive = true }
                        }
                    },
                    viewModel = motorcycleViewModel
                )
            } else {
                PlaceholderScreen("No tienes permisos para crear motocicletas")
            }
        }
        composable(
            route = AppRoutes.MotorcycleDetail,
            arguments = listOf(navArgument("motorcycleId") { type = NavType.StringType })
        ) { entry ->
            val motorcycleId = entry.arguments?.getString("motorcycleId") ?: return@composable
            if (uiState.isAdmin) {
                MotorcycleDetailScreen(
                    id = motorcycleId,
                    navController = navController,
                    userName = uiState.user?.name,
                    onLogout = onLogout,
                    onEdit = { id -> navController.navigate(AppRoutes.editMotorcycle(id)) },
                    viewModel = motorcycleViewModel
                )
            } else {
                PlaceholderScreen("No tienes permisos para ver motocicletas")
            }
        }
        composable(
            route = AppRoutes.EditMotorcycle,
            arguments = listOf(navArgument("motorcycleId") { type = NavType.StringType })
        ) { entry ->
            val motorcycleId = entry.arguments?.getString("motorcycleId") ?: return@composable
            if (uiState.isAdmin) {
                EditMotorcycleScreen(
                    id = motorcycleId,
                    navController = navController,
                    userName = uiState.user?.name,
                    onLogout = onLogout,
                    viewModel = motorcycleViewModel
                )
            } else {
                PlaceholderScreen("No tienes permisos para editar motocicletas")
            }
        }
        composable(AppRoutes.CreateOrder) {
            if (uiState.isAdmin) {
                AppScaffold(
                    title = "Nueva orden",
                    navController = navController,
                    isAdmin = true,
                    userName = uiState.user?.name,
                    onLogout = {
                        loginViewModel.logout {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                ) { padding ->
                    CreateOrderScreen(
                        contentPadding = padding,
                        onBack = { navController.popBackStack() },
                        onOrderCreated = { id ->
                            navController.navigate(AppRoutes.orderDetail(id)) {
                                popUpTo(AppRoutes.CreateOrder) { inclusive = true }
                            }
                        },
                        viewModel = orderViewModel
                    )
                }
            } else {
                PlaceholderScreen("No tienes permisos para crear órdenes")
            }
        }
        composable(
            route = AppRoutes.OrderDetail,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            val orderId = entry.arguments?.getString("orderId") ?: return@composable
            if (uiState.isAdmin) {
                AppScaffold(
                    title = "Detalle de orden",
                    navController = navController,
                    isAdmin = true,
                    userName = uiState.user?.name,
                    onLogout = {
                        loginViewModel.logout {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                ) { padding ->
                    OrderDetailScreen(
                        orderId = orderId,
                        contentPadding = padding,
                        onBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate(AppRoutes.editOrder(id)) },
                        viewModel = orderViewModel
                    )
                }
            } else {
                PlaceholderScreen("No tienes permisos para ver órdenes")
            }
        }
        composable(
            route = AppRoutes.EditOrder,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            val orderId = entry.arguments?.getString("orderId") ?: return@composable
            if (uiState.isAdmin) {
                AppScaffold(
                    title = "Editar orden",
                    navController = navController,
                    isAdmin = true,
                    userName = uiState.user?.name,
                    onLogout = {
                        loginViewModel.logout {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                ) { padding ->
                    EditOrderScreen(
                        orderId = orderId,
                        contentPadding = padding,
                        onBack = { navController.popBackStack() },
                        viewModel = orderViewModel
                    )
                }
            } else {
                PlaceholderScreen("No tienes permisos para editar órdenes")
            }
        }
    }
}

@Composable
private fun AdminOnlyRoute(
    isAdmin: Boolean,
    navController: androidx.navigation.NavHostController,
    content: @Composable () -> Unit
) {
    if (isAdmin) {
        content()
    } else {
        LaunchedEffect(Unit) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
