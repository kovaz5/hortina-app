package com.alex.hortina.ui.screens.splash

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alex.hortina.data.local.UserPreferencesDataStore
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dataStore = remember { UserPreferencesDataStore(context) }

    val viewModel: SplashViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SplashViewModel(dataStore) as T
        }
    })

    val sessionValid by viewModel.isSessionValid.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkLogin()
    }

    LaunchedEffect(sessionValid) {
        if (sessionValid != null) {
            delay(800)
            if (sessionValid == true) {
                navController.navigate("dashboard") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
