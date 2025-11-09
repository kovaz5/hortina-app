package com.alex.hortina.ui.screens.tareas

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alex.hortina.data.remote.dto.TareaDto
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.TareaRepository
import com.alex.hortina.ui.screens.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen() {
    val tareasViewModel: TareaViewModel = viewModel()
    val dashboardViewModel = remember {
        DashboardViewModel(
            CultivoRepository(),
            TareaRepository()
        )
    }
    val uiState by tareasViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tareas del huerto") }, actions = {
                IconButton(onClick = { tareasViewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                }
            })
        }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is TareaUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is TareaUiState.Error -> {
                    Text(
                        text = (uiState as TareaUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is TareaUiState.Success -> {
                    val data = (uiState as TareaUiState.Success)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        item {
                            Text(
                                "Pendientes (${data.pendientes.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        if (data.pendientes.isEmpty()) {
                            item { Text("No tienes tareas pendientes") }
                        } else {
                            items(data.pendientes) { tarea ->
                                TareaItem(
                                    tarea = tarea,
                                    onToggleEstado = { id, nuevoEstado ->
                                        tareasViewModel.cambiarEstado(
                                            id, nuevoEstado
                                        ) { completadas, pendientes ->
                                            dashboardViewModel.actualizarTareasLocalmente(
                                                completadas, pendientes
                                            )
                                        }
                                    },
                                    modifier = Modifier.animateContentSize(animationSpec = spring())
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "Completadas (${data.completadas.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        if (data.completadas.isEmpty()) {
                            item { Text("Aún no completaste ninguna tarea") }
                        } else {
                            items(data.completadas) { tarea ->
                                TareaItem(
                                    tarea = tarea, onToggleEstado = { id, nuevoEstado ->
                                        tareasViewModel.cambiarEstado(
                                            id, nuevoEstado
                                        ) { completadas, pendientes ->
                                            dashboardViewModel.actualizarTareasLocalmente(
                                                completadas, pendientes
                                            )
                                        }
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TareaItem(
    tarea: TareaDto, onToggleEstado: (Int, Boolean) -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tarea.nombre_tarea ?: "Sin nombre", fontWeight = FontWeight.SemiBold
                )
                tarea.descripcion?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "Fecha sugerida: ${tarea.fechaSugerida ?: "—"}",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Checkbox(
                checked = tarea.completada == true, onCheckedChange = { checked ->
                    tarea.id_tarea?.let { id -> onToggleEstado(id, checked) }
                })
        }
    }
}

