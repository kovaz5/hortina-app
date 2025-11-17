package com.alex.hortina.ui.screens.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alex.hortina.R
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.TareaRepository
import com.alex.hortina.data.repository.UserRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {

    val context = LocalContext.current
    val dataStore = remember { UserPreferencesDataStore(context) }

    val cultivoRepo = CultivoRepository()
    val tareaRepo = TareaRepository()
    val userRepo = UserRepository()

    val viewModel = DashboardViewModel(cultivoRepo, tareaRepo, userRepo, dataStore)

    val uiState by viewModel.uiState.collectAsState()

    val refreshFlag = navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean>(
        "refreshDashboard", false
    )?.collectAsState()

    LaunchedEffect(refreshFlag?.value) {
        if (refreshFlag?.value == true) {
            viewModel.refresh()

            navController.currentBackStackEntry?.savedStateHandle?.set("refreshDashboard", false)
        }
    }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {

                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is DashboardUiState.Error -> {
                    val msg = (uiState as DashboardUiState.Error).message
                    Text(
                        text = stringResource(R.string.error) + ": $msg",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DashboardUiState.Success -> {
                    val data = uiState as DashboardUiState.Success

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        DashboardHeader(
                            userName = data.userName, ultimaActualizacion = data.ultimaActualizacion
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        DashboardStatsSection(
                            cultivosActivos = data.cultivos.size,
                            tareasPendientes = data.tareasPendientes,
                            tareasCompletadas = data.tareasCompletadas
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardUpcomingTasksList(tareas = data.tareasProximas)

                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardCultivosSection(cultivos = data.cultivos)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(userName: String, ultimaActualizacion: String) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.hello) + ", $userName 👋",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.summary),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.last_update) + ": $ultimaActualizacion",
            style = MaterialTheme.typography.labelMedium,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DashboardStatsSection(cultivosActivos: Int, tareasPendientes: Int, tareasCompletadas: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DashboardStatCard(stringResource(R.string.crops), cultivosActivos.toString())
        DashboardStatCard(stringResource(R.string.pending), tareasPendientes.toString())
        DashboardStatCard(stringResource(R.string.completed), tareasCompletadas.toString())
    }
}

@Composable
fun DashboardStatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DashboardUpcomingTasksList(tareas: List<com.alex.hortina.data.remote.dto.TareaDto>) {
    Text(
        text = stringResource(R.string.next_tasks),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (tareas.isEmpty()) {
        Text(
            text = stringResource(R.string.no_pending_tasks),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        tareas.forEach { tarea ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                ListItem(headlineContent = {
                    Text(
                        text = "${tarea.nombre_tarea ?: stringResource(R.string.unnamed)} " + "- ${tarea.cultivo?.nombre}"
                    )
                }, supportingContent = {
                    Text(
                        text = stringResource(R.string.suggested_date) + ": ${
                            tarea.fechaSugerida.let {
                                LocalDate.parse(it).format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                                )
                            } ?: stringResource(R.string.no_date)
                        }")
                }, leadingContent = { Text("🪴") })
            }
        }
    }
}


@Composable
fun DashboardCultivosSection(cultivos: List<CultivoDto>) {
    Text(
        text = stringResource(R.string.your_crops),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (cultivos.isEmpty()) {
        Text(
            text = stringResource(R.string.no_crops),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cultivos) { cultivo ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .height(120.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = cultivo.nombre ?: stringResource(R.string.unnamed_crop),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cultivo.tipo ?: stringResource(R.string.unknown_type),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

