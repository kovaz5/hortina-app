package com.alex.hortina.ui.screens.tareas

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alex.hortina.R
import com.alex.hortina.data.remote.dto.TareaDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.repository.TareaRepository

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TareasScreen(navController: NavController) {

    val context = LocalContext.current
    val dataStore = remember { UserPreferencesDataStore(context) }

    val viewModel: TareaViewModel =
        viewModel(factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TareaViewModel(
                    repository = TareaRepository(),
                    dataStore = dataStore
                ) as T
            }
        })

    val uiState by viewModel.uiState.collectAsState()
    val locale = LocalConfiguration.current.locales[0]

    when (uiState) {

        is TareaUiState.Loading -> Box(
            Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is TareaUiState.Error -> Box(
            Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) { Text((uiState as TareaUiState.Error).message) }

        is TareaUiState.Success -> {

            val data = uiState as TareaUiState.Success

            val tareasPorDia = data.tareasPorDia
            val fechaSeleccionada = data.fechaSeleccionada
            var mesActual by remember { mutableStateOf(YearMonth.from(fechaSeleccionada)) }
            val noCropsTexto = stringResource(R.string.no_crops)
            val tasksForTitle = stringResource(R.string.tasks_for)

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.garden_tasks)) },
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        actions = {
                            IconButton(onClick = { viewModel.loadTareas() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            }
                        })
                }) { padding ->

                val tareasDelDia =
                    tareasPorDia[fechaSeleccionada]?.sortedBy { it.cultivo?.nombre ?: "" }
                        ?: emptyList()

                val tareasAgrupadas =
                    tareasDelDia.groupBy { it.cultivo?.nombre ?: noCropsTexto }.toSortedMap()

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {

                    item {
                        MonthlyCalendar(
                            month = mesActual,
                            selectedDate = fechaSeleccionada,
                            tasksPerDay = tareasPorDia,
                            locale = locale,
                            onDayClick = { viewModel.seleccionarDia(it) },
                            onPreviousMonth = { mesActual = mesActual.minusMonths(1) },
                            onNextMonth = { mesActual = mesActual.plusMonths(1) })

                        Spacer(Modifier.height(20.dp))

                        val fechaFormateada = fechaSeleccionada.format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                        )

                        Text(
                            "$tasksForTitle $fechaFormateada",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(8.dp))
                    }

                    if (tareasDelDia.isEmpty()) {
                        item {
                            Text(stringResource(R.string.no_tasks_for_day))
                        }
                    } else {

                        tareasAgrupadas.forEach { (cultivo, tareas) ->

                            stickyHeader {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = cultivo,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            items(tareas) { tarea ->
                                TareaCard(tarea, onClick = {
                                    cultivoId -> navController.navigate("cultivo_detalle/$cultivoId")
                                })
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TareaCard(tarea: TareaDto, onClick: (Int) -> Unit) {

    val cultivoId = tarea.cultivo?.idCultivo
    val tipo = when (tarea.tipo_origen?.lowercase()) {
        "automática_api" -> "🔁 " + stringResource(R.string.automatic_task)
        "manual" -> "🛠 " + stringResource(R.string.manual_task)
        else -> ""
    }

    val esPasada = tarea.fechaSugerida?.let {
        try {
            LocalDate.parse(it).isBefore(LocalDate.now())
        } catch (_: Exception) {
            false
        }
    } ?: false

    val decoration = if (esPasada) TextDecoration.LineThrough else TextDecoration.None
    val textColor = if (esPasada) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                cultivoId?.let { onClick(it) }
            }, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(12.dp)) {

            Text(
                tarea.nombre_tarea ?: stringResource(R.string.unnamed),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = decoration, color = textColor
                )
            )

            tarea.descripcion?.let { desc ->
                Spacer(Modifier.height(4.dp))
                Text(
                    desc, style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = decoration, color = textColor.copy(alpha = 0.8f)
                    )
                )
            }

            if (tipo.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tipo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
fun MonthlyCalendar(
    month: YearMonth,
    selectedDate: LocalDate,
    tasksPerDay: Map<LocalDate, List<TareaDto>>,
    locale: Locale,
    onDayClick: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {

    Column {

        val formattedMonth = remember(month, locale) {
            month.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
                .replaceFirstChar { it.titlecase(locale) }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
            }

            Text(
                formattedMonth, style = MaterialTheme.typography.titleLarge
            )

            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        Spacer(Modifier.height(8.dp))

        val dayNames = remember(locale) {
            listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            ).map {
                it.getDisplayName(TextStyle.SHORT, locale).replace(".", "")
                    .replaceFirstChar { char -> char.titlecase(locale) }
            }
        }

        Row(Modifier.fillMaxWidth()) {
            dayNames.forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(Modifier.height(4.dp))


        val firstOfMonth = month.atDay(1)
        val lastOfMonth = month.atEndOfMonth()

        val startOffset = (firstOfMonth.dayOfWeek.value + 6) % 7
        val totalCells = startOffset + lastOfMonth.lengthOfMonth()
        val rows = (totalCells + 6) / 7

        val today = LocalDate.now()

        Column {
            repeat(rows) { row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { col ->

                        val cellIndex = row * 7 + col
                        val dayOfMonth = cellIndex - startOffset + 1

                        if (dayOfMonth < 1 || dayOfMonth > lastOfMonth.lengthOfMonth()) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        } else {

                            val date = month.atDay(dayOfMonth)
                            val hasTasks = tasksPerDay.containsKey(date)
                            val isSelected = date == selectedDate

                            val background = when {
                                date == today -> MaterialTheme.colorScheme.tertiaryContainer
                                hasTasks && date.isBefore(today) -> MaterialTheme.colorScheme.secondaryContainer
                                hasTasks && date.isAfter(today) -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val borderWidth = if (isSelected || date == today) 2.dp else 0.dp
                            val borderColor = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                date == today -> MaterialTheme.colorScheme.tertiary
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(background)
                                    .border(
                                        width = borderWidth,
                                        color = borderColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDayClick(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$dayOfMonth")
                            }
                        }
                    }
                }
            }
        }
    }
}
