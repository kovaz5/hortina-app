package com.alex.hortina.ui.screens.cultivodetalle

import com.alex.hortina.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.remote.dto.TareaDto
import com.alex.hortina.data.remote.dto.PlantProfileDto
import com.alex.hortina.data.repository.CultivoRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Stars
import androidx.compose.foundation.layout.Arrangement
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.window.DialogProperties


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoDetalleScreen(cultivoId: Int, navController: NavHostController) {
    val context = LocalContext.current
    val dataStore = remember { UserPreferencesDataStore(context) }

    val viewModel: CultivoDetalleViewModel =
        viewModel(factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CultivoDetalleViewModel(CultivoRepository(), dataStore) as T
            }
        })

    LaunchedEffect(cultivoId) {
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
                is CultivoDetalleUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is CultivoDetalleUiState.Error -> {
                    val msg = (uiState as CultivoDetalleUiState.Error).message
                    Text(
                        text = "${stringResource(R.string.error)}: $msg",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is CultivoDetalleUiState.Success -> {
                    val detalle = (uiState as CultivoDetalleUiState.Success).detalle
                    val plantProfile = viewModel.getCachedPlantProfile()

                    CultivoDetalleContent(
                        cultivo = detalle.cultivo,
                        tareas = detalle.tareas,
                        plantProfile = plantProfile,
                        navController = navController,
                        onEdit = {
                            detalle.cultivo.idCultivo?.let { id ->
                                navController.navigate("cultivo_editar/$id")
                            }
                        },
                        onDelete = {
                            detalle.cultivo.idCultivo?.let { id ->
                                viewModel.deleteCultivo(id, onDeleted = {
                                    navController.popBackStack()
                                }, onError = { msg ->
                                    println("Error al borrar: $msg")
                                })
                            }
                        })

                }
            }
        }
    }
}

@Composable
private fun CultivoDetalleContent(
    cultivo: CultivoDto,
    tareas: List<TareaDto>,
    plantProfile: PlantProfileDto?,
    navController: NavHostController,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (pendientes, completadas) = tareas.partition { it.completada != true }

    val showDeleteDialog = remember { mutableStateOf(false) }

    val pendientesOrdenadas =
        pendientes.sortedWith(compareBy { safeParseToLocalDate(it.fechaSugerida) ?: LocalDate.MAX })

    val completadasOrdenadas = completadas.sortedWith(compareByDescending<TareaDto> {
        safeParseToLocalDate(it.fechaSugerida) ?: LocalDate.MIN
    })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val imageUrl = plantProfile?.imageUrl ?: cultivo.imagen
            if (!imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = cultivo.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cultivo.nombre ?: stringResource(R.string.unnamed),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    if (!cultivo.tipo.isNullOrBlank()) {
                        Text(
                            text = cultivo.tipo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        plantProfile?.sunlight?.takeIf { it.isNotBlank() }?.let {
                            AssistChip(
                                onClick = {},
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.WbSunny, contentDescription = null
                                    )
                                },
                                label = { Text(it, maxLines = 2) },
                                modifier = Modifier
                                    .padding(end = 8.dp, bottom = 8.dp)
                                    .widthIn(max = 200.dp)
                            )
                        }

                        plantProfile?.watering?.takeIf { it.isNotBlank() }?.let {
                            AssistChip(
                                onClick = {},
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.WaterDrop, contentDescription = null
                                    )
                                },
                                label = { Text(it, maxLines = 2) },
                                modifier = Modifier
                                    .padding(end = 8.dp, bottom = 8.dp)
                                    .widthIn(max = 200.dp)
                            )
                        }

                        plantProfile?.careLevel?.takeIf { it.isNotBlank() }?.let {
                            AssistChip(
                                onClick = {},
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Stars, contentDescription = null
                                    )
                                },
                                label = { Text(it, maxLines = 2) },
                                modifier = Modifier
                                    .padding(end = 8.dp, bottom = 8.dp)
                                    .widthIn(max = 200.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    val heightText = formatHeight(plantProfile?.height)
                    if (heightText.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.height_label) + ": $heightText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    plantProfile?.edibleParts?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${stringResource(R.string.edible_parts)}: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = { showDeleteDialog.value = true }) {
                        Icon(
                            Icons.Default.Delete,
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow(stringResource(R.string.planted_as) + ":", cultivo.estado)
                DetailRow(
                    stringResource(R.string.planting_date) + ":",
                    formatDateSafely(cultivo.fecha_plantacion)
                )
                DetailRow(
                    stringResource(R.string.estimated_harvest) + ":",
                    formatDateSafely(cultivo.fecha_estimada_cosecha)
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider()
        }

        item {
            val ctx = LocalContext.current
            Button(
                onClick = {
                    val id = cultivo.idCultivo

                    if (id != null) {
                        val route = "tarea_crear/${id}"

                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.add_task))
            }
        }

        item {
            Text(
                text = stringResource(R.string.pending_tasks),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
        }
        items(pendientesOrdenadas) { tarea ->
            TareaListItem(tarea, navController)
        }

        item {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.completed_tasks),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
        }
        items(completadasOrdenadas) { tarea ->
            TareaListItem(tarea, navController, completed = true)
        }
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },

            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = true
            ),

            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,

            title = {
                Text(
                    text = stringResource(R.string.confirm_delete_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    text = stringResource(R.string.confirm_delete_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog.value = false
                        onDelete()
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            },

            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            })
    }

}

@Composable
private fun DetailRow(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontWeight = FontWeight.Medium)
        Text(text = value ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun safeParseToLocalDate(s: String?): LocalDate? {
    if (s.isNullOrBlank()) return null
    return try {
        LocalDate.parse(s)
    } catch (e: Exception) {
        try {
            val dt = LocalDateTime.parse(s)
            dt.toLocalDate()
        } catch (_: Exception) {
            null
        }
    }
}

private fun formatDateSafely(s: String?): String {
    val parsed = safeParseToLocalDate(s) ?: return "—"
    return parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))
}

private fun formatHeight(heightStr: String?): String {
    if (heightStr.isNullOrBlank()) return ""
    val v = heightStr.toDoubleOrNull() ?: return heightStr
    return if (v < 1.0) {
        val cm = (v * 100).toInt()
        "$cm cm"
    } else {
        val one = (Math.round(v * 10).toInt()) / 10.0
        "${one} m"
    }
}

@Composable
private fun formatRelativeDateAndColor(fechaSugerida: String?): Pair<String, Color> {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val colorHoy = MaterialTheme.colorScheme.primary
    val colorManana = MaterialTheme.colorScheme.secondary
    val colorOtro = MaterialTheme.colorScheme.onSurfaceVariant

    val parsed = safeParseToLocalDate(fechaSugerida)

    return when {
        parsed == null -> Pair(stringResource(R.string.no_date), colorOtro)
        parsed.isEqual(today) -> Pair(stringResource(R.string.today), colorHoy)
        parsed.isEqual(tomorrow) -> Pair(stringResource(R.string.tomorrow), colorManana)
        else -> Pair(
            parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())), colorOtro
        )
    }
}

@Composable
private fun TareaListItem(
    t: TareaDto, navController: NavHostController, completed: Boolean = false
) {
    val isPast = safeParseToLocalDate(t.fechaSugerida)?.isBefore(LocalDate.now()) == true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                t.cultivo?.let { id ->
                    navController.navigate("cultivo_detalle/$id")
                }
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = t.nombre_tarea ?: "—",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (completed || isPast) TextDecoration.LineThrough
                        else TextDecoration.None
                    )

                    Spacer(Modifier.height(6.dp))

                    if (!t.descripcion.isNullOrBlank()) {
                        Text(
                            text = t.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val (dateText, accentColor) = formatRelativeDateAndColor(t.fechaSugerida)

                Column(horizontalAlignment = Alignment.End) {

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = dateText, fontSize = 12.sp, color = accentColor
                    )
                }
            }
        }
    }
}

