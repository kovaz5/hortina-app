package com.alex.hortina.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alex.hortina.R
import com.alex.hortina.ui.components.PasswordTextField
import com.alex.hortina.ui.components.isValidEmail
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)
    val googleIdOption =
        GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false)
            .setServerClientId("228270332646-10am94au55vg8nd7o19n3hepjkkocts3.apps.googleusercontent.com")
            .setNonce(null)
            .build()
    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()



    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.login)) })
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

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.mail)) },
                modifier = Modifier.fillMaxWidth(),
                isError = emailError
            )

            if (emailError) {
                Text(
                    text = stringResource(R.string.invalid_email),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.password)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val canLogin = !emailError && password.isNotBlank() && password.isNotBlank()

            Button(
                onClick = {
                    viewModel.login(email, password) {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }, modifier = Modifier.fillMaxWidth(), enabled = canLogin
            ) {
                Text(stringResource(R.string.enter))
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (uiState) {
                is LoginUiState.Error -> Text(
                    (uiState as LoginUiState.Error).message, color = MaterialTheme.colorScheme.error
                )

                is LoginUiState.Loading -> CircularProgressIndicator()
                else -> {}
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.signInWithGoogle(
                        context = context, credentialManager = credentialManager, request = request
                    ) {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.signin_google))
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("registro") }) {
                Text(stringResource(R.string.no_account))
            }
        }
    }
}

