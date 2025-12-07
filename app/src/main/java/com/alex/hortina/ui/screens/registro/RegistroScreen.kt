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
import com.alex.hortina.ui.components.PasswordTextField
import com.alex.hortina.ui.components.isValidEmail
import com.alex.hortina.ui.components.isValidPassword

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(navController: NavHostController) {
    val viewModel: RegistroViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.user_registration)) })
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val emailError = email.isNotBlank() && !isValidEmail(email)
            val passwordError = password.isNotBlank() && !isValidPassword(password)
            val matchError = password2.isNotBlank() && password != password2

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
                modifier = Modifier.fillMaxWidth(),
                isError = emailError
            )

            if (emailError) Text(stringResource(R.string.invalid_email), color = MaterialTheme.colorScheme.error)

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.password),
                isError = passwordError,
                errorMessage = stringResource(R.string.error_password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = password2,
                onValueChange = { password2 = it },
                label = stringResource(R.string.confirm_password),
                isError = matchError,
                errorMessage = stringResource(R.string.password_unmatch)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val canRegister =
                nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank() && password2.isNotBlank() && !emailError && !passwordError && !matchError

            Button(
                onClick = {
                    viewModel.registrar(nombre, email, password) {
                        navController.navigate("dashboard") {
                            popUpTo("registro") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                enabled = canRegister && uiState !is RegistroUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (uiState is RegistroUiState.Loading) stringResource(R.string.signing_up)
                    else stringResource(R.string.sign_up)
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
