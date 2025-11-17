package com.alex.hortina.ui.screens.cultivodetalle

import com.alex.hortina.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.remote.dto.TareaDto
import com.alex.hortina.data.repository.CultivoRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoDetalleScreen(cultivoId: Int) {
    val repository = CultivoRepository()
    val viewModel = remember(cultivoId) { CultivoDetalleViewModel(repository) }

    LaunchedEffect(Unit) {
        println("Lanzando carga de detalle para id=$cultivoId")
        viewModel.load(cultivoId)
    }


    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_crop)) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is CultivoDetalleUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )

                is CultivoDetalleUiState.Error -> {
                    val msg = (uiState as CultivoDetalleUiState.Error).message
                    Text(
                        stringResource(R.string.error) + ": $msg",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is CultivoDetalleUiState.Success -> {
                    val detalle = (uiState as CultivoDetalleUiState.Success).detalle
                    CultivoDetalleContent(detalle.cultivo, detalle.tareas)
                }
            }
        }
    }
}

@Composable
private fun CultivoDetalleContent(cultivo: CultivoDto, tareas: List<TareaDto>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = cultivo.nombre ?: stringResource(R.string.unnamed),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(stringResource(R.string.scientific_name)+":", cultivo.tipo)
            DetailRow(stringResource(R.string.planted_as)+":", cultivo.estado)
            DetailRow(stringResource(R.string.planting_date)+":", cultivo.fecha_plantacion.let {
                LocalDate.parse(it).format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                )
            })
            DetailRow(stringResource(R.string.estimated_harvest)+":", cultivo.fecha_estimada_cosecha.let {
                LocalDate.parse(it).format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                )
            })
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.related_tasks),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(tareas) { tarea ->
            TareaListItem(tarea)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontWeight = FontWeight.Medium)
        Text(text = value ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
private fun TareaListItem(t: TareaDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = t.nombre_tarea ?: stringResource(R.string.task),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = t.descripcion ?: "",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = t.fechaSugerida ?: "—",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (t.completada == true) stringResource(R.string.completed) else stringResource(
                        R.string.pending
                    ),
                    fontSize = 12.sp,
                    color = if (t.completada == true) Color(0xFF2E7D32) else Color(0xFFB00020)
                )
            }
        }
    }
}
