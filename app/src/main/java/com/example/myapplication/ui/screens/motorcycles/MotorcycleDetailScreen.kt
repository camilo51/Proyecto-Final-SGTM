package com.example.myapplication.ui.screens.motorcycles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.ui.screens.AppScaffold
import com.example.myapplication.ui.viewmodel.MotorcycleViewModel

@Composable
fun MotorcycleDetailScreen(
    id: String,
    navController: NavController,
    userName: String?,
    onLogout: () -> Unit,
    currentUser: UserDto? = null,
    onOpenProfile: () -> Unit = {},
    onEdit: (String) -> Unit,
    viewModel: MotorcycleViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(id) { viewModel.loadMotorcycle(id) }

    AppScaffold(
        title = "Detalle de motocicleta",
        navController = navController,
        isAdmin = true,
        userName = userName,
        currentUser = currentUser,
        onOpenProfile = onOpenProfile,
        onLogout = onLogout
    ) { padding ->
        val motorcycle = state.selectedMotorcycle
        when {
            state.isLoadingDetail && (motorcycle == null || motorcycle.id != id) ->
                CircularProgressIndicator(modifier = Modifier.padding(padding).padding(16.dp))
            motorcycle == null || motorcycle.id != id ->
                Column(
                    modifier = Modifier.padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(state.detailErrorMessage ?: "La motocicleta no existe.", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = navController::popBackStack) { Text("← Volver") }
                }
            else -> MotorcycleDetailContent(
                motorcycle = motorcycle,
                owner = state.clients.firstOrNull { it.id == motorcycle.clientId },
                isSaving = state.isSaving,
                onEdit = { onEdit(id) },
                onBack = navController::popBackStack,
                contentPadding = padding
            )
        }
    }
}

@Composable
private fun MotorcycleDetailContent(
    motorcycle: Motorcycle,
    owner: Client?,
    isSaving: Boolean,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack, enabled = !isSaving) { Text("← Volver") }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(motorcycle.plate ?: "Sin placa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    listOfNotNull(motorcycle.brand, motorcycle.model).joinToString(" ").ifBlank { "Motocicleta" },
                    style = MaterialTheme.typography.titleMedium
                )
                MotorcycleStatusChip(motorcycle.status)
            }
        }

        MotorcycleDetailSection("Información de la motocicleta") {
            MotorcycleDetailLine("Placa", motorcycle.plate)
            MotorcycleDetailLine("Marca", motorcycle.brand)
            MotorcycleDetailLine("Modelo", motorcycle.model)
            MotorcycleDetailLine("Año", motorcycle.year?.toString())
            MotorcycleDetailLine("Color", motorcycle.color)
            MotorcycleDetailLine("Cilindraje", motorcycle.engineCc?.let { "$it cc" })
            MotorcycleDetailLine("Propietario", owner?.name ?: if (motorcycle.clientId == null) "Sin propietario" else "No registrado")
            MotorcycleDetailLine("Notas", motorcycle.notes)
            MotorcycleDetailLine("Estado", motorcycle.status)
            Text(
                "El estado operativo se actualiza desde Órdenes de trabajo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(onClick = onEdit, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) { Text("Editar") }
        TextButton(onClick = onBack, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) { Text("← Volver") }
    }
}

@Composable
private fun MotorcycleDetailSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun MotorcycleDetailLine(label: String, value: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value?.takeIf(String::isNotBlank) ?: "No registrado")
    }
}
