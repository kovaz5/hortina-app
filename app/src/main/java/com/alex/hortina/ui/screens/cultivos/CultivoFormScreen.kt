package com.alex.hortina.ui.screens.cultivos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoFormScreen(
    navController: NavHostController,
    viewModel: CultivoViewModel = viewModel(),
    cultivoId: Int? = null
) {
    val formState by viewModel.formState.collectAsState()
    val creationSuccess by viewModel.creationSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cultivoId) {
        cultivoId?.let { viewModel.loadCultivoById(it) }
    }

    LaunchedEffect(creationSuccess) {
        if (creationSuccess) {
            snackbarHostState.showSnackbar("Cultivo creado correctamente")
            navController.popBackStack()
            viewModel.resetCreationFlag()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo cultivo") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registrar nuevo cultivo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            var query by remember { mutableStateOf("") }
            var resultados by remember { mutableStateOf(listOf<com.alex.hortina.data.remote.dto.PlantProfileDto>()) }
            var buscando by remember { mutableStateOf(false) }

            val plantasRepo = remember { com.alex.hortina.data.repository.PlantasRepository() }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar planta") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LaunchedEffect(query) {
                if (query.length >= 3) {
                    buscando = true
                    try {
                        resultados = plantasRepo.searchPlants(query)
                    } catch (e: Exception) {
                        resultados = emptyList()
                    } finally {
                        buscando = false
                    }
                } else {
                    resultados = emptyList()
                }
            }

            if (buscando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (resultados.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    resultados.take(5).forEach { planta ->
                        TextButton(
                            onClick = {
                                viewModel.onFormChange(formState.copy(selectedPlant = planta))
                                query = planta.commonName ?: ""
                                resultados = emptyList()
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(planta.commonName ?: "Sin nombre")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = formState.nombre,
                onValueChange = { viewModel.onFormChange(formState.copy(nombre = it)) },
                label = { Text("Nombre del cultivo") },
                singleLine = true
            )

            OutlinedTextField(
                value = formState.tipo,
                onValueChange = { viewModel.onFormChange(formState.copy(tipo = it)) },
                label = { Text("Tipo de cultivo") },
                singleLine = true
            )

            OutlinedTextField(
                value = formState.fechaPlantacion,
                onValueChange = { viewModel.onFormChange(formState.copy(fechaPlantacion = it)) },
                label = { Text("Fecha de plantación (YYYY-MM-DD)") },
                singleLine = true
            )

            if (formState.error != null) {
                Text(
                    text = formState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(20.dp))

            formState.selectedPlant?.let { plant ->
                Text(
                    text = "Planta seleccionada: ${plant.commonName ?: "—"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    if (cultivoId != null) viewModel.updateCultivo(cultivoId)
                    else viewModel.createCultivo()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (cultivoId != null) "Actualizar" else "Guardar")
            }
        }
    }
}
