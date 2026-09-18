package com.example.myapplication.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.screens.BackLink
import com.example.myapplication.ui.viewmodel.inventory.EditInventoryViewModel

@Composable
fun EditInventoryScreen(
    id: Long,
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    viewModel: EditInventoryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(id) { viewModel.loadForEdit(id) }
    LaunchedEffect(state.savedItem) {
        if (state.savedItem != null) {
            viewModel.consumeSavedItem()
            navController.popBackStack()
        }
    }
    AppScaffold("Editar repuesto", navController, isAdmin = true, userName = userName, onLogout = onLogout) { padding ->
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BackLink(onClick = { navController.popBackStack() })
                Text("Editar repuesto", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("El estado no se edita: el backend lo recalcula.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                InventoryFormFields(
                    state = state,
                    submitLabel = "Guardar cambios",
                    onFieldChange = viewModel::updateForm,
                    onSubmit = { viewModel.update(id) }
                )
            }
        }
    }
}
