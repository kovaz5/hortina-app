package com.alex.hortina.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.alex.hortina.R
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.ui.screens.cultivodetalle.CultivoDetalleScreen
import com.alex.hortina.ui.screens.cultivos.CultivoFormScreen
import com.alex.hortina.ui.screens.cultivos.CultivosScreen
import com.alex.hortina.ui.screens.dashboard.DashboardScreen
import com.alex.hortina.ui.screens.login.LoginScreen
import com.alex.hortina.ui.screens.perfil.PerfilScreen
import com.alex.hortina.ui.screens.perfil.PerfilViewModel
import com.alex.hortina.ui.screens.registro.RegistroScreen
import com.alex.hortina.ui.screens.splash.SplashScreen
import com.alex.hortina.ui.screens.tareas.TareasScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HortinaNavGraph(navController: NavHostController, startDestination: String = "login") {

    Scaffold(bottomBar = {
        val route = currentRoute(navController)
        if (route != null && route !in listOf("login", "registro")) {
            BottomBar(navController)
        }

    }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable("splash") {
                SplashScreen(navController)
            }

            composable("login") {
                LoginScreen(navController)
            }

            composable("registro") {
                RegistroScreen(navController)
            }

            composable("dashboard") {
                DashboardScreen(navController)
            }

            composable("cultivos") {
                CultivosScreen(navController)
            }

            composable(
                route = "cultivo_detalle/{cultivoId}",
                arguments = listOf(navArgument("cultivoId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("cultivoId") ?: 0
                CultivoDetalleScreen(cultivoId = id)
            }

            composable("cultivo_form") {
                CultivoFormScreen(navController = navController)
            }

            composable(
                route = "cultivo_editar/{cultivoId}",
                arguments = listOf(navArgument("cultivoId") { type = NavType.IntType })
            ) { backStackEntry ->
                val cultivoId = backStackEntry.arguments?.getInt("cultivoId")
                CultivoFormScreen(navController = navController, cultivoId = cultivoId)
            }

            composable("tareas") {
                TareasScreen(navController)
            }

            composable("perfil") {
                PerfilScreen(
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    })
            }
        }
    }
}

@Composable
internal fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
