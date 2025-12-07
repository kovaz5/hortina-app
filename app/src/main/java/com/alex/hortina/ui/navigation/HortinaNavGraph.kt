package com.alex.hortina.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.ui.screens.cultivodetalle.CultivoDetalleScreen
import com.alex.hortina.ui.screens.cultivos.CultivoFormScreen
import com.alex.hortina.ui.screens.cultivos.CultivosScreen
import com.alex.hortina.ui.screens.dashboard.DashboardScreen
import com.alex.hortina.ui.screens.login.LoginScreen
import com.alex.hortina.ui.screens.onboarding.OnboardingScreen
import com.alex.hortina.ui.screens.perfil.PerfilScreen
import com.alex.hortina.ui.screens.perfil.PerfilViewModel
import com.alex.hortina.ui.screens.registro.RegistroScreen
import com.alex.hortina.ui.screens.splash.SplashScreen
import com.alex.hortina.ui.screens.tareas.CrearTareaScreen
import com.alex.hortina.ui.screens.tareas.TareasScreen
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun HortinaNavGraph(navController: NavHostController, startDestination: String = "splash") {

    NavHost(
        navController = navController, startDestination = startDestination
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
            ScreenWithScaffold(navController) {
                DashboardScreen(navController)
            }
        }

        composable("cultivos") { backStackEntry ->
            val parentEntry = remember { navController.getBackStackEntry("cultivos") }
            ScreenWithScaffold(navController) {
                LocalViewModelStoreOwnerProvider(parentEntry) {
                    CultivosScreen(navController)
                }
            }
        }

        composable(
            route = "cultivo_detalle/{cultivoId}",
            arguments = listOf(navArgument("cultivoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("cultivoId") ?: 0
            ScreenWithScaffold(navController) {
                CultivoDetalleScreen(
                    cultivoId = id, navController = navController
                )
            }
        }

        composable("cultivo_form") { backStackEntry ->
            val parentEntry = remember { navController.getBackStackEntry("cultivos") }
            ScreenWithScaffold(navController) {
                LocalViewModelStoreOwnerProvider(parentEntry) {
                    CultivoFormScreen(navController)
                }
            }
        }

        composable(
            "cultivo_editar/{cultivoId}",
            arguments = listOf(navArgument("cultivoId") { type = NavType.IntType })
        ) { backStackEntry ->

            val parentEntry = remember { navController.getBackStackEntry("cultivos") }
            val cultivoId = backStackEntry.arguments?.getInt("cultivoId")

            ScreenWithScaffold(navController) {
                LocalViewModelStoreOwnerProvider(parentEntry) {
                    CultivoFormScreen(navController, cultivoId)
                }
            }
        }

        composable("tareas") {
            ScreenWithScaffold(navController) {
                TareasScreen(navController)
            }
        }

        composable("perfil") {
            val context = LocalContext.current
            val dataStore = remember { UserPreferencesDataStore(context) }
            val vm = rememberPerfilViewModel(dataStore)

            ScreenWithScaffold(navController) {
                PerfilScreen(
                    onLogout = {
                        vm.viewModelScope.launch { vm.logout() }

                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    })
            }
        }

        composable("onboarding") {
            val context = LocalContext.current
            val dataStore = remember { UserPreferencesDataStore(context) }

            OnboardingScreen(
                navController = navController,
                dataStore = dataStore
            )
        }


        composable(
            route = "tarea_crear/{cultivoId}",
            arguments = listOf(navArgument("cultivoId") { type = NavType.IntType })
        ) { backStackEntry ->

            val cultivoId = backStackEntry.arguments!!.getInt("cultivoId")

            ScreenWithScaffold(navController) {
                CrearTareaScreen(cultivoId, navController)
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWithScaffold(
    navController: NavHostController, content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            val route = currentRoute(navController)
            if (route !in listOf("login", "registro", "splash")) {
                BottomBar(navController)
            }
        }) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}


@Composable
internal fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
private fun rememberPerfilViewModel(dataStore: UserPreferencesDataStore): PerfilViewModel {
    return viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PerfilViewModel(dataStore) as T
        }
    })
}

@Composable
fun LocalViewModelStoreOwnerProvider(
    owner: androidx.lifecycle.ViewModelStoreOwner, content: @Composable () -> Unit
) {
    val provider = androidx.compose.runtime.staticCompositionLocalOf {
        owner
    }

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner provides owner
    ) {
        content()
    }
}