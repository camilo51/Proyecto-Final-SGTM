package com.example.myapplication

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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

        composeRule.setContent {
            AppTheme {
                DashboardSearchBar(
                    onOpenDrawer = {},
                    onSearchClients = { submittedQuery = it }
                )
            }
        }

        composeRule.onNodeWithTag("dashboard-client-search")
            .performTextInput("Ana")
        composeRule.onNodeWithContentDescription("Buscar clientes").performClick()

        assertEquals("Ana", submittedQuery)
    }
}
