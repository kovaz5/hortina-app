package com.alex.hortina.ui.screens.perfil

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.R
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var systemPermissionGranted by remember {
        mutableStateOf(hasNotificationPermission(context))
    }
    val activity = context as? Activity
    val dataStore = remember { UserPreferencesDataStore(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // Recalcular permiso cuando volvemos desde Ajustes
                systemPermissionGranted = hasNotificationPermission(context)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val viewModel: PerfilViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PerfilViewModel(dataStore) as T
        }
    })

    val uiState by viewModel.uiState.collectAsState()
    val shouldRecreate by viewModel.shouldRecreate.collectAsState()
    val notificationsEnabledUI = uiState.notificacionesEnabled && systemPermissionGranted


    LaunchedEffect(shouldRecreate) {
        if (shouldRecreate == true) {
            activity?.recreate()
            viewModel.resetRecreateFlag()
        }
    }


    if (uiState.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.nombre.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = uiState.nombre, style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = uiState.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            HorizontalDivider()

            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            IdiomaSelector(
                selected = uiState.idioma, onChange = { viewModel.cambiarIdioma(it) })

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.notis),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (notificationsEnabledUI) "Activadas" else "Desactivadas",
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = notificationsEnabledUI, onCheckedChange = { enabled ->

                        if (enabled && !systemPermissionGranted) {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Debes activar las notificaciones en Ajustes.",
                                    actionLabel = "Abrir"
                                )

                                if (result == SnackbarResult.ActionPerformed) {
                                    val intent = Intent(
                                        Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                    ).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                            return@Switch
                        }

                        viewModel.cambiarNotificaciones(enabled)

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (enabled) "Notificaciones activadas" else "Notificaciones desactivadas"
                            )
                        }
                    })

            }

            OutlinedButton(
                onClick = onLogout, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdiomaSelector(
    selected: String, onChange: (String) -> Unit
) {
    val idiomas = listOf(
        "es" to "Español", "gl" to "Galego", "en" to "English"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = idiomas.first { it.first == selected }.second,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(),
            label = { Text("Elige idioma") })

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            idiomas.forEach { (code, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    expanded = false
                    onChange(code)
                })
            }
        }
    }
}

fun hasNotificationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

