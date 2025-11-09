package com.alex.hortina.ui.screens.cultivos

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.alex.hortina.data.remote.dto.CultivoDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivosScreen(navController: NavHostController) {
    val viewModel: CultivoViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = { Text("Mis cultivos") }, actions = {
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { navController.navigate("cultivo_form") }) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo cultivo")
        }
    }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is CultivoUiState.Loading -> CircularProgressIndicator()
                is CultivoUiState.Error -> Text(
                    text = (uiState as CultivoUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )

                is CultivoUiState.Empty -> Text("No tienes cultivos registrados todavía")
                is CultivoUiState.Success -> {
                    val cultivos = (uiState as CultivoUiState.Success).cultivos
                    CultivosList(cultivos, navController, viewModel)
                }
            }
        }
    }
}

@Composable
fun CultivosList(
    cultivos: List<CultivoDto>, navController: NavHostController, viewModel: CultivoViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(cultivos) { cultivo ->
            CultivoCard(cultivo, navController, viewModel)
        }
    }
}

@Composable
fun CultivoCard(
    cultivo: CultivoDto, navController: NavHostController, viewModel: CultivoViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clickable {
                println("Click en cultivo con id=${cultivo.idCultivo}")
                cultivo.idCultivo?.let {
                    navController.navigate("cultivo_detalle/$it")
                }
            }, elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .clickable {
                    println("Click en cultivo con id=${cultivo.idCultivo}")
                    cultivo.idCultivo?.let {
                        navController.navigate("cultivo_detalle/$it")
                    }
                },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (cultivo.imagen != null) {
                        Image(
                            painter = rememberAsyncImagePainter(cultivo.imagen),
                            contentDescription = cultivo.nombre,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(end = 12.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column {
                        Text(
                            text = cultivo.nombre ?: "Cultivo sin nombre",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(text = "Tipo: ${cultivo.tipo ?: "Desconocido"}")
                        Text(text = "Estado: ${cultivo.estado ?: "-"}")
                        cultivo.fecha_estimada_cosecha?.let {
                            Text(text = "Cosecha estimada: $it")
                        }
                    }
                }

                // 🔹 Botones debajo del contenido
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(onClick = {
                        cultivo.idCultivo?.let { id ->
                            navController.navigate("cultivo_editar/$id")
                        }
                    }) {
                        Text("Editar")
                    }

                    TextButton(onClick = {
                        cultivo.idCultivo?.let { id ->
                            viewModel.deleteCultivo(id)
                        }
                    }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
