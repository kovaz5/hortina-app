package com.alex.hortina.ui.screens.registro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alex.hortina.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(navController: NavHostController) {
    val viewModel: RegistroViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.user_registration)) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.mail)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                        viewModel.registrar(nombre, email, password) {
                            navController.navigate("login") {
                                popUpTo("registro") { inclusive = true }
                            }
                        }
                    }
                }, modifier = Modifier.fillMaxWidth(), enabled = uiState !is RegistroUiState.Loading
            ) {
                Text(
                    when (uiState) {
                        is RegistroUiState.Loading -> stringResource(R.string.signing_up)
                        else -> stringResource(R.string.sign_up)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (uiState) {
                is RegistroUiState.Error -> Text(
                    text = (uiState as RegistroUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )

                is RegistroUiState.Success -> Text(
                    text = stringResource(R.string.account_created),
                    color = MaterialTheme.colorScheme.primary
                )

                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("login") }) {
                Text(stringResource(R.string.already_account))
            }
        }
    }
}
