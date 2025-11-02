package com.alex.hortina.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alex.hortina.ui.screens.cultivos.CultivosScreen
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import com.alex.hortina.ui.screens.dashboard.DashboardScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HortinaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "dashboard"
) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable("login") {
                Scaffold(
                    topBar = { CenterAlignedTopAppBar(title = { Text("Pantalla de login") }) }) { padding ->
                    Text(
                        text = "Aquí irá el login",
                        modifier = androidx.compose.ui.Modifier.padding(padding)
                    )
                }
            }

            composable("dashboard") {
                DashboardScreen()
            }

            composable("cultivos") {
                CultivosScreen()
            }

            composable("tareas") {
                Scaffold(
                    topBar = { CenterAlignedTopAppBar(title = { Text("Tareas pendientes") }) }) { padding ->
                    Text(
                        text = "Aquí estarán las tareas del usuario",
                        modifier = androidx.compose.ui.Modifier.padding(padding)
                    )
                }
            }

            composable("perfil") {
                Scaffold(
                    topBar = { CenterAlignedTopAppBar(title = { Text("Perfil") }) }) { innerPadding ->
                    Text(
                        text = "Datos del usuario y configuración (pendiente",
                        modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
