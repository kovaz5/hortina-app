package com.alex.hortina.ui.screens.tareas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.res.stringResource
import com.alex.hortina.R
import com.alex.hortina.ui.components.CalendarDateField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTareaScreen(cultivoId: Int, navController: NavHostController) {

    val viewModel: CrearTareaViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CrearTareaViewModel(cultivoId) as T
        }
    })

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.success) {
        LaunchedEffect(Unit) { navController.popBackStack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_task)) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = stringResource(R.string.task_details),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = viewModel::updateNombre,
                        label = { Text(stringResource(R.string.task_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.descripcion,
                        onValueChange = viewModel::updateDescripcion,
                        label = { Text(stringResource(R.string.description_opt)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    CalendarDateField(
                        label = stringResource(R.string.date),
                        date = uiState.fecha,
                        onDateSelected = viewModel::updateFecha
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.recurrente,
                            onCheckedChange = viewModel::updateRecurrente
                        )
                        Text(stringResource(R.string.repeated))
                    }

                    if (uiState.recurrente) {
                        OutlinedTextField(
                            value = uiState.frecuenciaDias,
                            onValueChange = viewModel::updateFrecuencia,
                            label = { Text(stringResource(R.string.frecuency)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if (uiState.error != null) {
                        Text(
                            uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = viewModel::crearTarea,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.loading
                    ) {
                        Text(
                            stringResource(R.string.create_task),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}