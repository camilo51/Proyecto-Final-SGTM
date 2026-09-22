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
import com.example.myapplication.ui.viewmodel.GlobalSearchModule
import com.example.myapplication.ui.viewmodel.GlobalSearchResult
import com.example.myapplication.ui.screens.DashboardSearchBar
import com.example.myapplication.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardSearchBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchBar_submitsTypedGlobalQuery() {
        var submittedQuery = ""
        var query by mutableStateOf("")

        composeRule.setContent {
            AppTheme {
                DashboardSearchBar(
                    searchQuery = query,
                    suggestions = emptyList(),
                    isLoading = false,
                    onOpenDrawer = {},
                    onQueryChange = { query = it },
                    onSearch = { submittedQuery = query },
                    onSelectResult = {}
                )
            }
        }

        composeRule.onNodeWithTag("global-search")
            .performTextInput("Ana")
        composeRule.onNodeWithContentDescription("Buscar en todos los módulos").performClick()

        assertEquals("Ana", submittedQuery)
    }

    @Test
    fun searchBar_showsAndSelectsGlobalSuggestion() {
        var selectedResultId = ""
        val result = GlobalSearchResult(
            module = GlobalSearchModule.CLIENTS,
            id = "1",
            title = "Ana Gómez",
            subtitle = "Doc. 12345678"
        )

        composeRule.setContent {
            AppTheme {
                DashboardSearchBar(
                    searchQuery = "Ana",
                    suggestions = listOf(result),
                    isLoading = false,
                    onOpenDrawer = {},
                    onQueryChange = {},
                    onSearch = {},
                    onSelectResult = { selectedResultId = it.id }
                )
            }
        }

        composeRule.onNodeWithText("Ana Gómez").assertIsDisplayed().performClick()

        assertEquals("1", selectedResultId)
    }
}
