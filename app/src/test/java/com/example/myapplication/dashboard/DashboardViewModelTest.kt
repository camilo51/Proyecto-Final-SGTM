package com.example.myapplication.dashboard

import com.example.myapplication.ui.viewmodel.GlobalSearchModule
import com.example.myapplication.ui.viewmodel.GlobalSearchResult
import com.example.myapplication.ui.viewmodel.DashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardViewModelTest {

    @Test
    fun globalSearchResults_matchRecordsFromDifferentModules() {
        val state = DashboardUiState(
            searchIndex = listOf(
                GlobalSearchResult(GlobalSearchModule.CLIENTS, "1", "Ana Gómez", "Doc. 12345678"),
                GlobalSearchResult(GlobalSearchModule.MOTORCYCLES, "2", "ABC123", "Yamaha FZ"),
                GlobalSearchResult(GlobalSearchModule.INVENTORY, "3", "Filtro de aceite", "Inventario")
            ),
            searchQuery = "yamaha"
        )

        assertEquals(listOf(GlobalSearchModule.MOTORCYCLES), state.searchResults.map { it.module })
    }
}
