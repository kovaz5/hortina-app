package com.alex.hortina.ui.screens.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alex.hortina.R
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.remote.dto.TareaDto
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.TareaRepository
import com.alex.hortina.data.repository.UserRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {

    val context = LocalContext.current
    val dataStore = remember { UserPreferencesDataStore(context) }
    val userData by dataStore.user.collectAsState(initial = null)

    val userId = userData?.id

    val cultivoRepo = CultivoRepository()
    val tareaRepo = TareaRepository()
    val userRepo = UserRepository()
    val viewModel = DashboardViewModel(cultivoRepo, tareaRepo, userRepo, dataStore)

    val uiState by viewModel.uiState.collectAsState()

    var hasSeenOnboarding by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(userId) {
        if (userId != null) {
            hasSeenOnboarding = dataStore.hasSeenOnboarding(userId)
        }
    }

    if (hasSeenOnboarding == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (hasSeenOnboarding == false) {
        LaunchedEffect(Unit) {
            navController.navigate("onboarding") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
        return
    }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.hello) + ", ${data.userName} 👋",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))

                            val lastUpdateText = formatLastUpdateText(data.ultimaActualizacion)
                            Text(
                                text = stringResource(R.string.last_update) + ": $lastUpdateText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = stringResource(R.string.your_garden_in_numbers),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SmallStatCard(
                                icon = Icons.Default.LocalFlorist,
                                label = stringResource(R.string.active_crops),
                                value = data.cultivos.size.toString()
                            )
                            Spacer(Modifier.width(12.dp))
                            SmallStatCard(
                                icon = Icons.Default.PendingActions,
                                label = stringResource(R.string.active_tasks),
                                value = data.tareasPendientes.toString()
                            )
                            Spacer(Modifier.width(12.dp))
                            SmallStatCard(
                                icon = Icons.Default.CheckCircle,
                                label = stringResource(R.string.completed_tasks),
                                value = data.tareasCompletadas.toString()
                            )
                        }


                        Spacer(Modifier.height(20.dp))

                        DashboardUpcomingTasksSection(
                            tareas = data.tareasProximas, navController = navController
                        )

                        Spacer(Modifier.height(20.dp))

                        DashboardCultivosSection(
                            cultivos = data.cultivos, navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmallStatCard(
    icon: ImageVector, label: String, value: String
) {
    ElevatedCard(
        modifier = Modifier
            .width(110.dp)
            .height(110.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DashboardUpcomingTasksSection(tareas: List<TareaDto>, navController: NavController) {
    Text(
        text = stringResource(R.string.next_tasks),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(12.dp))

    if (tareas.isEmpty()) {
        Text(
            text = stringResource(R.string.no_pending_tasks),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        tareas.forEach { tarea ->
            UpcomingTaskItem(tarea = tarea, navController = navController)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun UpcomingTaskItem(tarea: TareaDto, navController: NavController) {
    val (dateText, accentColor) = formatRelativeDateAndColor(tarea.fechaSugerida)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val cultivoId = tarea.cultivo?.let { c ->
                    val field = try {
                        c::class.java.getDeclaredField("idCultivo")
                    } catch (_: Exception) {
                        null
                    }
                    field?.let {
                        it.isAccessible = true
                        (it.get(c) as? Number)?.toInt()
                    }
                }
                cultivoId?.let { id -> navController.navigate("cultivo_detalle/$id") }
            }) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(accentColor)
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = tarea.nombre_tarea ?: stringResource(R.string.unnamed),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = tarea.cultivo?.nombre ?: stringResource(R.string.no_crops),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = dateText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

@Composable
fun DashboardCultivosSection(cultivos: List<CultivoDto>, navController: NavController) {
    Text(
        text = stringResource(R.string.your_crops),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(12.dp))

    if (cultivos.isEmpty()) {
        Text(
            text = stringResource(R.string.no_crops),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(cultivos) { cultivo ->
                CultivoCard(cultivo = cultivo, navController = navController)
            }
        }
    }
}

@Composable
fun CultivoCard(cultivo: CultivoDto, navController: NavController) {
    val accentColor = colorFromString(cultivo.nombre ?: "")
    Row(
        modifier = Modifier
            .width(150.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                val cultivoId = cultivo.let { c ->
                    val field = try {
                        c::class.java.getDeclaredField("idCultivo")
                    } catch (_: Exception) {
                        null
                    }
                    field?.let {
                        it.isAccessible = true
                        (it.get(c) as? Number)?.toInt()
                    }
                }
                cultivoId?.let { id -> navController.navigate("cultivo_detalle/$id") }
            }) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )

        Spacer(Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = cultivo.nombre ?: stringResource(R.string.unnamed_crop),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = cultivo.tipo ?: stringResource(R.string.unknown_type),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun formatRelativeDateAndColor(fechaSugerida: String?): Pair<String, Color> {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    val colorHoy = MaterialTheme.colorScheme.primary
    val colorManana = MaterialTheme.colorScheme.secondary
    val colorOtro = MaterialTheme.colorScheme.onSurfaceVariant

    val parsedDate: LocalDate? = try {
        fechaSugerida?.let { LocalDate.parse(it) }
    } catch (_: Exception) {
        try {
            fechaSugerida?.let { LocalDateTime.parse(it).toLocalDate() }
        } catch (_: Exception) {
            null
        }
    }

    return when {
        parsedDate == null -> stringResource(R.string.no_date) to colorOtro

        parsedDate.isEqual(today) -> stringResource(R.string.today) to colorHoy

        parsedDate.isEqual(tomorrow) -> stringResource(R.string.tomorrow) to colorManana

        else -> parsedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) to colorOtro
    }
}


@Composable
fun formatLastUpdateText(ultimaActualizacion: String?): String {
    if (ultimaActualizacion.isNullOrBlank()) {
        return stringResource(R.string.no_date)
    }

    val parsedDateTime: LocalDateTime? = try {
        LocalDateTime.parse(ultimaActualizacion)
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(ultimaActualizacion, DateTimeFormatter.ISO_DATE_TIME)
        } catch (_: Exception) {
            null
        }
    }

    if (parsedDateTime == null) {
        return ultimaActualizacion
    }

    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    val date = parsedDateTime.toLocalDate()
    val timePart = parsedDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

    return when {
        date.isEqual(today) -> "${stringResource(R.string.today)}, $timePart"

        date.isEqual(yesterday) -> "${stringResource(R.string.yesterday)}, $timePart"

        else -> parsedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }
}

@Composable
fun colorFromString(key: String): Color {
    if (key.isBlank()) return MaterialTheme.colorScheme.primary

    val hash = abs(key.hashCode())
    val hue = (hash % 360).toFloat()
    val saturation = 0.45f
    val lightness = 0.58f

    return hslToColor(hue, saturation, lightness)
}

fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - abs(2 * l - 1f)) * s
    val hh = h / 60f
    val x = c * (1f - abs(hh % 2f - 1f))
    var (r1, g1, b1) = when {
        hh < 1f -> Triple(c, x, 0f)
        hh < 2f -> Triple(x, c, 0f)
        hh < 3f -> Triple(0f, c, x)
        hh < 4f -> Triple(0f, x, c)
        hh < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = l - c / 2f
    val r = ((r1 + m) * 255f).roundToInt()
    val g = ((g1 + m) * 255f).roundToInt()
    val b = ((b1 + m) * 255f).roundToInt()
    return Color(r, g, b)
}
