package com.alex.hortina.ui.screens.cultivos

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alex.hortina.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoFormScreen(
    navController: NavHostController,
    cultivoId: Int? = null,
    viewModel: CultivoViewModel = viewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val creationSuccess by viewModel.creationSuccess.collectAsState()

    val plantasRepo = remember { com.alex.hortina.data.repository.PlantasRepository() }
    var query by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf(listOf<com.alex.hortina.data.remote.dto.PlantProfileDto>()) }
    var buscando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

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

    val titulo =
        if (cultivoId != null) stringResource(R.string.edit_crop) else stringResource(R.string.register_new_crop)

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(titulo) }, windowInsets = WindowInsets(0, 0, 0, 0)
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                delay(300)
                                try {
                                    resultados = plantasRepo.searchPlants(new)
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (buscando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                if (resultados.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        resultados.take(8).forEach { planta ->
                            TextButton(
                                onClick = {
                                    viewModel.onFormChange(formState.copy(selectedPlant = planta))
                                    query = planta.commonName ?: ""
                                    resultados = emptyList()
                                }, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(planta.commonName ?: stringResource(R.string.unnamed))
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("semilla", "planta").forEach { estado ->
                    FilterChip(
                        selected = formState.estado == estado,
                        onClick = { viewModel.onFormChange(formState.copy(estado = estado)) },
                        label = { Text(estado) })
                }
            }

            OutlinedTextField(
                value = formState.fechaPlantacion,
                onValueChange = { viewModel.onFormChange(formState.copy(fechaPlantacion = it)) },
                label = { Text(stringResource(R.string.planting_day) + " (YYYY-MM-DD)") },
                singleLine = true
            )

            formState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            formState.selectedPlant?.let { plant ->
                Text(
                    stringResource(R.string.selected_plant) + ": ${plant.commonName}",
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (cultivoId != null) viewModel.updateCultivo(cultivoId)
                    else viewModel.createCultivo()
                },
                enabled = formState.isValid(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    if (cultivoId != null) stringResource(R.string.update_crop) else stringResource(
                        R.string.delete_crop
                    )
                )
            }
        }
    }
}
