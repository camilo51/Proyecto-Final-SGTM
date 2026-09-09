package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.AdminScreen
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.PlaceholderScreen
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val uiState by loginViewModel.uiState.collectAsState()

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
                isAdmin = uiState.isAdmin
            )
        }
        composable("home") {
            HomeScreen(
                userName = uiState.user?.name,
                navController = navController,
                isAdmin = uiState.isAdmin
            )
        }

        val placeholderRoutes = listOf(
            "clients" to "Clientes",
            "employees" to "Empleados",
            "appointments" to "Citas",
            "motorcycles" to "Motocicletas",
            "brands" to "Marcas",
            "inventory" to "Inventario",
            "orders" to "Órdenes",
            "invoices" to "Facturas",
            "reminders" to "Recordatorios",
            "reports" to "Reportes",
            "users" to "Usuarios"
        )

        placeholderRoutes.forEach { (route, label) ->
            composable(route) {
                AppScaffold(
                    title = label,
                    navController = navController,
                    isAdmin = uiState.isAdmin,
                    userName = uiState.user?.name
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        PlaceholderScreen(label)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    userName: String?,
    navController: NavController,
    isAdmin: Boolean
) {
    AppScaffold(
        title = "Inicio",
        navController = navController,
        isAdmin = isAdmin,
        userName = userName
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Bienvenido!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = userName ?: "Usuario",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
