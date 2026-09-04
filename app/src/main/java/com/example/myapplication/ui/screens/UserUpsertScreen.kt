package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.AppOutlinedTextFieldColors
import com.example.myapplication.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserUpsertScreen(
    viewModel: UserViewModel,
    userId: String? = null,
    onBack: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    val user = users.find { it.id == userId }

    var name by remember(userId) { mutableStateOf("") }
    var email by remember(userId) { mutableStateOf("") }
    var showValidationError by remember(userId) { mutableStateOf(false) }

    // Synchronizes the form when an existing user arrives asynchronously.
    LaunchedEffect(userId, user?.id) {
        name = user?.name.orEmpty()
        email = user?.email.orEmpty()
    }

    val isEditing = userId != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Editar usuario" else "Agregar usuario")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            showValidationError = false
                        },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppOutlinedTextFieldColors(),
                        isError = showValidationError && name.isBlank(),
                        supportingText = if (showValidationError && name.isBlank()) {
                            { Text("El nombre es obligatorio") }
                        } else {
                            null
                        },
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            showValidationError = false
                        },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppOutlinedTextFieldColors(),
                        isError = showValidationError && email.isBlank(),
                        supportingText = if (showValidationError && email.isBlank()) {
                            { Text("El correo electrónico es obligatorio") }
                        } else {
                            null
                        },
                    )

                    Button(
                        onClick = {
                            if (name.isBlank() || email.isBlank()) {
                                showValidationError = true
                            } else {
                                if (userId == null) {
                                    viewModel.addUser(name = name, email = email)
                                } else {
                                    viewModel.updateUser(
                                        id = userId,
                                        name = name,
                                        email = email,
                                    )
                                }
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
