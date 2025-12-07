package com.alex.hortina.ui.screens.cultivos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.alex.hortina.R
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.TranslationRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivosScreen(navController: NavHostController) {

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
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.your_crops)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        Icons.Default.Refresh, contentDescription = stringResource(R.string.update)
                    )
                }
            })
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

                is CultivoUiState.Empty -> Text(stringResource(R.string.no_crops))
                is CultivoUiState.Success -> {
                    val cultivos = (uiState as CultivoUiState.Success).cultivos
                    val sorted = cultivos.sortedByDescending { c ->
                        c.fecha_plantacion?.let { LocalDate.parse(it) } ?: LocalDate.MIN
                    }
                    CultivosList(sorted, navController, viewModel)
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
    val barColor = MaterialTheme.colorScheme.primary
    var showDeleteDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                cultivo.idCultivo?.let { navController.navigate("cultivo_detalle/$it") }
            }, tonalElevation = 2.dp, shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Imagen
                if (cultivo.imagen != null) {
                    Image(
                        painter = rememberAsyncImagePainter(cultivo.imagen),
                        contentDescription = cultivo.nombre,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌱")
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cultivo.nombre ?: stringResource(R.string.unnamed_crop),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = cultivo.tipo ?: stringResource(R.string.unknown_type),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    cultivo.fecha_plantacion?.let {
                        Text(
                            text = LocalDate.parse(it).format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {
                        cultivo.idCultivo?.let { id ->
                            navController.navigate("cultivo_editar/$id")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }

                }
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },

                        properties = DialogProperties(
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true,
                            usePlatformDefaultWidth = true
                        ),

                        shape = RoundedCornerShape(24.dp),

                        containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp,

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
                                    showDeleteDialog = false
                                    cultivo.idCultivo?.let { id ->
                                        viewModel.deleteCultivo(id)
                                    }
                                }, colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ), modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.delete),
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        },

                        dismissButton = {
                            OutlinedButton(onClick = { showDeleteDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        })
                }
            }
        }
    }
}

