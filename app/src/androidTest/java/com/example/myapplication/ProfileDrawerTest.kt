package com.example.myapplication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class ProfileDrawerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun drawerHeader_showsAuthenticatedUser() {
        composeRule.setContent {
            AppTheme {
                AppScaffold(
                    title = "Inicio",
                    navController = rememberNavController(),
                    isAdmin = true,
                    currentUser = UserDto(
                        name = "Danilo",
                        email = "danilo@sgtm.com",
                        role = "Administrador"
                    )
                ) { }
            }
        }

        composeRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeRule.onNodeWithText("Danilo").assertIsDisplayed()
        composeRule.onNodeWithText("Administrador").assertIsDisplayed()
    }
}
