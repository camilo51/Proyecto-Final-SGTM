package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserUpsertScreen(
    viewModel: UserViewModel,
    userId: String? = null,
    onBack: () -> Unit
) {
    val users by viewModel.users.collectAsState()

    val user = users.find {
        it.id == userId
    }

    var name by remember {
        mutableStateOf(user?.name ?: "")
    }

    var email by remember {
        mutableStateOf(user?.email ?: "")
    }

    Scaffold(

        topBar = {
            TopAppBar(

                title = {
                    Text(
                        if (userId == null) {
                            "Agregar usuario"
                        } else {
                            "Editar usuario"
                        }
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(

                value = name,

                onValueChange = {
                    name = it
                },

                label = {
                    Text("Nombre")
                },

                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = email,

                onValueChange = {
                    email = it
                },

                label = {
                    Text("Correo electrónico")
                },

                modifier = Modifier.fillMaxWidth()
            )

            Button(

                onClick = {

                    if (
                        name.isNotBlank() &&
                        email.isNotBlank()
                    ) {

                        if (userId == null) {

                            viewModel.addUser(
                                name = name,
                                email = email
                            )

                        } else {

                            viewModel.updateUser(
                                id = userId,
                                name = name,
                                email = email
                            )
                        }

                        onBack()
                    }
                },

                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}