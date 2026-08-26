package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.ItemListScreen
import com.example.myapplication.ui.screens.ItemUpsertScreen
import com.example.myapplication.ui.screens.UserListScreen
import com.example.myapplication.ui.screens.UserUpsertScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.ItemViewModel
import com.example.myapplication.ui.viewmodel.UserViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}


@Composable
fun MainScreen() {

    val navController = rememberNavController()

    val itemViewModel: ItemViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    Scaffold(

        bottomBar = {

            NavigationBar {

                val navBackStackEntry by
                navController.currentBackStackEntryAsState()

                val currentDestination =
                    navBackStackEntry?.destination


                // ITEMS
                NavigationBarItem(

                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Items"
                        )
                    },

                    label = {
                        Text("Items")
                    },

                    selected =
                        currentDestination
                            ?.hierarchy
                            ?.any {
                                it.route?.startsWith("items") == true
                            } == true,

                    onClick = {

                        navController.navigate("items") {

                            popUpTo(
                                navController.graph.findStartDestination().id
                            ) {
                                saveState = true
                            }

                            launchSingleTop = true

                            restoreState = true
                        }
                    }
                )


                // USERS
                NavigationBarItem(

                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Users"
                        )
                    },

                    label = {
                        Text("Users")
                    },

                    selected =
                        currentDestination
                            ?.hierarchy
                            ?.any {
                                it.route?.startsWith("users") == true
                            } == true,

                    onClick = {

                        navController.navigate("users") {

                            popUpTo(
                                navController.graph.findStartDestination().id
                            ) {
                                saveState = true
                            }

                            launchSingleTop = true

                            restoreState = true
                        }
                    }
                )
            }
        }

    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "items",
            modifier = Modifier.padding(innerPadding)
        ) {


            // =========================================================
            // ITEMS
            // =========================================================

            navigation(
                startDestination = "items_list",
                route = "items"
            ) {

                // Lista de Items
                composable("items_list") {

                    ItemListScreen(

                        viewModel = itemViewModel,

                        onEditItem = { id ->
                            navController.navigate(
                                "items_upsert?id=$id"
                            )
                        },

                        onAddItem = {
                            navController.navigate(
                                "items_upsert"
                            )
                        }
                    )
                }


                // Crear / Editar Item
                composable(

                    route = "items_upsert?id={id}",

                    arguments = listOf(

                        navArgument("id") {

                            type = NavType.StringType

                            nullable = true

                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->

                    val id =
                        backStackEntry.arguments
                            ?.getString("id")

                    ItemUpsertScreen(

                        viewModel = itemViewModel,

                        itemId = id,

                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // =========================================================
            // USERS
            // =========================================================

            navigation(
                startDestination = "users_list",
                route = "users"
            ) {

                // Lista de usuarios
                composable("users_list") {

                    UserListScreen(

                        viewModel = userViewModel,

                        onEditUser = { id ->

                            navController.navigate(
                                "users_upsert?id=$id"
                            )
                        },

                        onAddUser = {

                            navController.navigate(
                                "users_upsert"
                            )
                        }
                    )
                }


                // Crear / Editar usuario
                composable(

                    route = "users_upsert?id={id}",

                    arguments = listOf(

                        navArgument("id") {

                            type = NavType.StringType

                            nullable = true

                            defaultValue = null
                        }
                    )

                ) { backStackEntry ->

                    val id =
                        backStackEntry.arguments
                            ?.getString("id")

                    UserUpsertScreen(

                        viewModel = userViewModel,

                        userId = id,

                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}