package com.alex.hortina.ui.screens.cultivos

import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.alex.hortina.R
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.TranslationRepository
import com.alex.hortina.ui.components.CalendarDateField
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoFormScreen(
    navController: NavHostController, cultivoId: Int? = null
) {
    val context = LocalContext.current
    val dataStore = remember { UserPreferencesDataStore(context) }

    val viewModel: CultivoViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CultivoViewModel(
                repository = CultivoRepository(),
                translator = TranslationRepository(),
                dataStore = dataStore
            ) as T
        }
    })

    val formState by viewModel.formState.collectAsState()
    val creationSuccess by viewModel.creationSuccess.collectAsState()

    val plantasRepo = remember { com.alex.hortina.data.repository.PlantasRepository() }
    var query by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf(listOf<com.alex.hortina.data.remote.dto.PlantProfileDto>()) }
    var buscando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var uiLang by remember { mutableStateOf("ES") }

    LaunchedEffect(Unit) {
        uiLang = dataStore.getLanguage()?.uppercase() ?: "ES"
    }

    LaunchedEffect(cultivoId) {
        cultivoId?.let {
            viewModel.loadCultivoById(it)
        }
    }

    LaunchedEffect(creationSuccess) {
        if (creationSuccess) {
            navController.popBackStack()
            viewModel.resetCreationFlag()
        }
    }

    val titulo = if (cultivoId != null) stringResource(R.string.edit_crop)
    else stringResource(R.string.register_new_crop)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) }, windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            if (cultivoId == null) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { new ->
                        query = new
                        searchJob?.cancel()

                        if (new.length >= 3) {
                            buscando = true
                            searchJob = scope.launch {
                                delay(150)
                                try {
                                    val plants = plantasRepo.searchPlants(new, uiLang)
                                    val translator = TranslationRepository()

                                    resultados = plants.map { p ->
                                        val translated = p.commonName?.let {
                                            translator.translateAuto(it, uiLang)
                                        }
                                        p.copy(commonName = translated)
                                    }
                                } catch (e: Exception) {
                                    resultados = emptyList()
                                } finally {
                                    buscando = false
                                }
                            }
                        } else {
                            resultados = emptyList()
                        }
                    },
                    label = { Text(stringResource(R.string.search_plant)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (buscando) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                resultados.take(8).forEach { planta ->
                    PlantSearchItem(
                        name = planta.commonName ?: stringResource(R.string.unnamed),
                        scientific = planta.scientificName ?: "",
                        imageUrl = planta.imageUrl,
                        onClick = {
                            viewModel.onFormChange(formState.copy(selectedPlant = planta))
                            query = planta.commonName ?: ""
                            resultados = emptyList()
                        })
                }
            }

            formState.selectedPlant?.let { plant ->
                SelectedPlantRow(
                    name = plant.commonName ?: stringResource(R.string.unnamed),
                    scientific = plant.scientificName ?: "",
                    imageUrl = plant.imageUrl,
                    onChange = {
                        if (cultivoId == null) resultados = emptyList()
                        query = ""
                        viewModel.onFormChange(formState.copy(selectedPlant = null))
                    })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("semilla", "planta").forEach { estado ->
                    FilterChip(
                        selected = formState.estado == estado,
                        onClick = { viewModel.onFormChange(formState.copy(estado = estado)) },
                        label = { Text(estado) })
                }
            }

            var selectedDate by remember { mutableStateOf(LocalDate.now()) }

            LaunchedEffect(formState.fechaPlantacion) {
                selectedDate = formState.fechaPlantacion?.takeIf { it.isNotBlank() }
                    ?.let { parseIsoToDate(it) } ?: LocalDate.now()
            }

            CalendarDateField(
                label = stringResource(R.string.planting_day),
                date = selectedDate,
                onDateSelected = { newDate ->
                    selectedDate = newDate
                    viewModel.onFormChange(
                        formState.copy(
                            fechaPlantacion = formatToIso(newDate)
                        )
                    )
                })

            formState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    if (cultivoId != null) viewModel.updateCultivo(cultivoId)
                    else viewModel.createCultivo()
                },
                enabled = formState.isValid(),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
            ) {
                Text(
                    if (cultivoId != null) stringResource(R.string.update_crop)
                    else stringResource(R.string.create_crop)
                )
            }
        }
    }
}

@Composable
private fun PlantSearchItem(
    name: String, scientific: String, imageUrl: String?, onClick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically
        ) {

            PlantImageSmall(imageUrl)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (scientific.isNotBlank()) {
                    Text(
                        text = scientific,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


@Composable
private fun SelectedPlantRow(
    name: String, scientific: String, imageUrl: String?, onChange: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically
        ) {

            PlantImageSmall(imageUrl)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold)
                if (scientific.isNotBlank()) {
                    Text(
                        text = scientific,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(onClick = onChange) {
                Text(stringResource(R.string.change))
            }
        }
    }
}

@Composable
private fun PlantImageSmall(imageUrl: String?) {
    val shape = CircleShape

    if (!imageUrl.isNullOrBlank()) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(shape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatToIso(date: LocalDate): String =
    date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)

private fun parseIsoToDate(s: String): LocalDate =
    LocalDate.parse(s, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)

private fun formatToDisplay(date: LocalDate): String =
    date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
