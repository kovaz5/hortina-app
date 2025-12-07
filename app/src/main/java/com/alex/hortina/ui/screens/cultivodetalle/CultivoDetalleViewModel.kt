package com.alex.hortina.ui.screens.cultivodetalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.dto.CultivoDetalleDto
import com.alex.hortina.data.remote.dto.PlantProfileDto
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.PlantasRepository
import com.alex.hortina.data.repository.TranslationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CultivoDetalleUiState {
    object Loading : CultivoDetalleUiState()
    data class Success(val detalle: CultivoDetalleDto) : CultivoDetalleUiState()
    data class Error(val message: String) : CultivoDetalleUiState()
}

class CultivoDetalleViewModel(
    private val repo: CultivoRepository,
    private val dataStore: UserPreferencesDataStore,
    private val plantasRepo: PlantasRepository = PlantasRepository(),
    private val translator: TranslationRepository = TranslationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CultivoDetalleUiState>(CultivoDetalleUiState.Loading)
    val uiState: StateFlow<CultivoDetalleUiState> = _uiState

    private var _cachedPlantProfile: PlantProfileDto? = null
    fun getCachedPlantProfile(): PlantProfileDto? = _cachedPlantProfile

    fun load(cultivoId: Int) {
        _uiState.value = CultivoDetalleUiState.Loading

        viewModelScope.launch {
            try {
                val detalleOriginal = repo.getCultivoDetalle(cultivoId)
                val lang = (dataStore.getLanguage() ?: "ES").uppercase()
                val cultivo = detalleOriginal.cultivo
                val cultivoTrad = cultivo.copy(
                    nombre = translator.translateAuto(cultivo.nombre ?: "", lang),
                    estado = translator.translateAuto(cultivo.estado ?: "", lang)
                )

                val profile = detalleOriginal.plantProfile
                val profileTrad = profile?.let { p ->
                    p.copy(
                        commonName = translator.translateAuto(p.commonName ?: "", lang),
                        watering = translator.translateAuto(p.watering ?: "", lang),
                        sunlight = translator.translateAuto(p.sunlight ?: "", lang),
                        careLevel = translator.translateAuto(p.careLevel ?: "", lang),
                        lifeCycle = translator.translateAuto(p.lifeCycle ?: "", lang),
                        edibleParts = translator.translateAuto(p.edibleParts ?: "", lang)
                    )
                }
                _cachedPlantProfile = profileTrad

                val tareasTrad = detalleOriginal.tareas.map { t ->
                    t.copy(
                        nombre_tarea = translator.translateAuto(t.nombre_tarea ?: "", lang),
                        descripcion = translator.translateAuto(t.descripcion ?: "", lang),
                        tipo_origen = translator.translateAuto(t.tipo_origen ?: "", lang)
                    )
                }

                val detalleFinal = CultivoDetalleDto(
                    cultivo = cultivoTrad, tareas = tareasTrad, plantProfile = profileTrad
                )

                _uiState.value = CultivoDetalleUiState.Success(detalleFinal)

            } catch (e: Exception) {
                _uiState.value = CultivoDetalleUiState.Error(
                    e.message ?: "Error al cargar detalle del cultivo"
                )
            }
        }
    }

    fun deleteCultivo(cultivoId: Int, onDeleted: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repo.deleteCultivo(cultivoId)
                onDeleted()
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar")
            }
        }
    }
}
