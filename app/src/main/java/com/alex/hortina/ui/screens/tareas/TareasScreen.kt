package com.alex.hortina.ui.screens.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(navController: NavController) {

    val viewModel: TareaViewModel = viewModel()
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

                Column(
                    Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {

                    MonthlyCalendar(
                        month = mesActual,
                        selectedDate = fechaSeleccionada,
                        tasksPerDay = tareasPorDia,
                        locale = locale,
                        onDayClick = { viewModel.seleccionarDia(it) },
                        onPreviousMonth = { mesActual = mesActual.minusMonths(1) },
                        onNextMonth = { mesActual = mesActual.plusMonths(1) })

                    Spacer(Modifier.height(20.dp))

                    // ---- Lista de tareas del día ----
                    val tareasDelDia = tareasPorDia[fechaSeleccionada] ?: emptyList()

                    Text(
                        stringResource(R.string.tasks_for) + " ${
                            fechaSeleccionada.let {
                                LocalDate.parse(
                                    it.toString()
                                ).format(
                                    DateTimeFormatter.ofPattern(
                                        "dd/MM/yyyy", Locale.getDefault()
                                    )
                                )
                            }
                        }", style = MaterialTheme.typography.titleMedium)

                    Spacer(Modifier.height(8.dp))

                    LazyColumn {
                        if (tareasDelDia.isEmpty()) {
                            item { Text(stringResource(R.string.no_tasks_for_day)) }
                        } else {
                            items(tareasDelDia) { tarea ->
                                TareaCard(tarea)
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
fun TareaCard(tarea: TareaDto) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                tarea.nombre_tarea
                    ?: (stringResource(R.string.unnamed) + " - " + tarea.cultivo?.nombre),
                style = MaterialTheme.typography.titleMedium
            )
            tarea.descripcion?.let { Text(it) }
            Text(
                stringResource(R.string.date) + ": ${
                    tarea.fechaSugerida.let {
                        LocalDate.parse(it)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))
                    }
                }")
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

        // Mes traducido correctamente
        val formattedMonth = remember(month, locale) {
            month.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
                .replaceFirstChar { it.titlecase(locale) }
        }

        // Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
            }

            Text(
                formattedMonth, style = MaterialTheme.typography.titleLarge
            )

            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }

        Spacer(Modifier.height(8.dp))


        // Días de la semana traducidos dinámicamente
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
                it.getDisplayName(TextStyle.SHORT, locale)
                    .replace(".", "") // algunas locales añaden punto
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
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                hasTasks -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(background)
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



