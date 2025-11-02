package com.alex.hortina.ui.screens.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.TareaRepository

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val cultivoRepo = CultivoRepository()
    val tareaRepo = TareaRepository()
    val viewModel = DashboardViewModel(cultivoRepo, tareaRepo)

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi huerto", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) })
        }) { paddingValues ->
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
                        text = "Error: $msg",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DashboardUiState.Success -> {
                    val data = (uiState as DashboardUiState.Success)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        DashboardHeader(
                            userName = "Alex", ultimaActualizacion = data.ultimaActualizacion
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
        Text(
            text = "¡Hola, $userName! 👋",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Aquí tienes un resumen de tu huerto hoy.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Última actualización: $ultimaActualizacion",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun DashboardStatsSection(cultivosActivos: Int, tareasPendientes: Int, tareasCompletadas: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DashboardStatCard("Cultivos", cultivosActivos.toString())
        DashboardStatCard("Pendientes", tareasPendientes.toString())
        DashboardStatCard("Completadas", tareasCompletadas.toString())
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
        text = "Próximas tareas",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (tareas.isEmpty()) {
        Text(
            text = "No tienes tareas pendientes por ahora",
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
                ListItem(
                    headlineContent = { Text(tarea.nombre_tarea ?: "Tarea sin nombre") },
                    supportingContent = {
                        Text(
                            text = "Fecha sugerida: ${
                                tarea.fecha_sugerida ?: "Sin fecha"
                            }"
                        )
                    },
                    leadingContent = { Text("🪴") })
            }
        }
    }
}


@Composable
fun DashboardCultivosSection(cultivos: List<CultivoDto>) {
    Text(
        text = "Tus cultivos",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
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
                        text = cultivo.nombre ?: "Cultivo sin nombre",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cultivo.tipo ?: "Tipo desconocido",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

