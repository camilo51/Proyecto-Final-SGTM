package com.example.myapplication

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.data.model.Client
import com.example.myapplication.ui.screens.DashboardSearchBar
import com.example.myapplication.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardSearchBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchBar_submitsTypedClientQuery() {
        var submittedQuery = ""
        var query by mutableStateOf("")

        composeRule.setContent {
            AppTheme {
                DashboardSearchBar(
                    searchQuery = query,
                    suggestions = emptyList(),
                    onOpenDrawer = {},
                    onQueryChange = { query = it },
                    onSearchClients = { submittedQuery = it },
                    onSelectClient = {}
                )
            }
        }

        composeRule.onNodeWithTag("dashboard-client-search")
            .performTextInput("Ana")
        composeRule.onNodeWithContentDescription("Buscar clientes").performClick()

        assertEquals("Ana", submittedQuery)
    }

    @Test
    fun searchBar_showsAndSelectsClientSuggestion() {
        var selectedClientId = ""
        val client = Client(
            id = "1",
            name = "Ana",
            lastName = "Gómez",
            document = "12345678"
        )

        composeRule.setContent {
            AppTheme {
                DashboardSearchBar(
                    searchQuery = "Ana",
                    suggestions = listOf(client),
                    onOpenDrawer = {},
                    onQueryChange = {},
                    onSearchClients = {},
                    onSelectClient = { selectedClientId = it.id.orEmpty() }
                )
            }
        }

        composeRule.onNodeWithText("Ana Gómez").assertIsDisplayed().performClick()

        assertEquals("1", selectedClientId)
    }
}
