package com.example.myapplication.dashboard

import com.example.myapplication.data.model.Client
import com.example.myapplication.ui.viewmodel.DashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardViewModelTest {

    @Test
    fun clientSuggestions_matchNameDocumentAndPhone() {
        val state = DashboardUiState(
            clients = listOf(
                Client(id = "1", name = "Ana", lastName = "Gómez", document = "12345678"),
                Client(id = "2", name = "Carlos", phone = "3001112233")
            ),
            clientSearchQuery = "gomez"
        )

        assertEquals(listOf("1"), state.clientSuggestions.map { it.id })
    }
}
