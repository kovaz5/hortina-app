package com.alex.hortina.ui.screens.cultivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.PlantasRepository
import com.alex.hortina.data.repository.TranslationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class CultivoUiState {
    object Loading : CultivoUiState()
    data class Success(val cultivos: List<CultivoDto>) : CultivoUiState()
    data class Error(val message: String) : CultivoUiState()
    object Empty : CultivoUiState()
}

class CultivoViewModel(
    private val repository: CultivoRepository,
    private val translator: TranslationRepository,
    private val dataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<CultivoUiState>(CultivoUiState.Loading)
    val uiState: StateFlow<CultivoUiState> = _uiState

    private val _formState = MutableStateFlow(CultivoFormState())
    val formState: StateFlow<CultivoFormState> = _formState

    private val _creationSuccess = MutableStateFlow(false)
    val creationSuccess: StateFlow<Boolean> = _creationSuccess

    private val plantasRepository = PlantasRepository()

    init {
        loadCultivos()
    }

    private suspend fun getLang(): String = dataStore.getLanguage()?.uppercase() ?: "ES"

    fun loadCultivos() {
        _uiState.value = CultivoUiState.Loading

        viewModelScope.launch {
            try {
                val rawList = repository.getCultivos()
                if (rawList.isEmpty()) {
                    _uiState.value = CultivoUiState.Empty
                    return@launch
                }

                val lang = getLang()

                val translated = rawList.map { c ->
                    c.copy(
                        nombre = c.nombre?.let { translator.translateAuto(it, lang) },
                        estado = c.estado?.let { translator.translateAuto(it, lang) })
                }

                _uiState.value = CultivoUiState.Success(translated)

            } catch (t: Throwable) {
                _uiState.value = CultivoUiState.Error(t.message ?: "Error desconocido")
            }
        }
    }

    fun loadCultivoById(id: Int) {
        viewModelScope.launch {
            try {
                val detalle = repository.getCultivoDetalle(id)
                val cultivo = detalle.cultivo
                val plantProfile = detalle.plantProfile

                _formState.value = CultivoFormState(
                    selectedPlant = plantProfile,
                    estado = cultivo.estado?.lowercase() ?: "semilla",
                    fechaPlantacion = cultivo.fecha_plantacion?.toString() ?: LocalDate.now()
                        .toString()
                )

            } catch (e: Exception) {
                _formState.value =
                    _formState.value.copy(error = "Error al cargar cultivo: ${e.message}")
            }
        }
    }


    fun onFormChange(updated: CultivoFormState) {
        _formState.value = updated
    }

    fun createCultivo() {
        val form = _formState.value
        if (!form.isValid()) {
            _formState.value = form.copy(error = "Selecciona una planta y un estado")
            return
        }

        viewModelScope.launch {
            try {
                repository.createCultivo(
                    CultivoDto(
                        idCultivo = null,
                        id_usuario = null,
                        plantExternalId = form.selectedPlant!!.externalId,
                        id_ubicacion = null,
                        nombre = form.selectedPlant.commonName,
                        tipo = form.selectedPlant.scientificName,
                        fecha_plantacion = form.fechaPlantacion,
                        estado = form.estado,
                        imagen = form.selectedPlant.imageUrl,
                        fecha_estimada_cosecha = null
                    )
                )
                _creationSuccess.value = true
                loadCultivos()

            } catch (t: Throwable) {
                _formState.value = form.copy(error = t.message ?: "Error al crear el cultivo")
            }
        }
    }

    fun updateCultivo(id: Int) {
        val form = _formState.value
        if (form.selectedPlant == null) {
            _formState.value = form.copy(error = "Selecciona una planta antes de actualizar")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateCultivo(
                    id, CultivoDto(
                        idCultivo = id,
                        id_usuario = null,
                        plantExternalId = form.selectedPlant.externalId,
                        id_ubicacion = null,
                        nombre = form.selectedPlant.commonName,
                        tipo = form.selectedPlant.scientificName,
                        fecha_plantacion = form.fechaPlantacion.ifBlank { null },
                        estado = form.estado,
                        imagen = form.selectedPlant.imageUrl,
                        fecha_estimada_cosecha = null
                    )
                )
                _creationSuccess.value = true
                loadCultivos()

            } catch (t: Throwable) {
                _formState.value = form.copy(error = t.message ?: "Error al actualizar cultivo")
            }
        }
    }

    fun resetCreationFlag() {
        _creationSuccess.value = false
    }

    fun deleteCultivo(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteCultivo(id)
                loadCultivos()
            } catch (e: Exception) {
                println("Error al eliminar cultivo: ${e.message}")
            }
        }
    }

    fun refresh() = loadCultivos()

}

