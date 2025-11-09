package com.alex.hortina.ui.screens.cultivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.PlantasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CultivoUiState {
    object Loading : CultivoUiState()
    data class Success(val cultivos: List<CultivoDto>) : CultivoUiState()
    data class Error(val message: String) : CultivoUiState()
    object Empty : CultivoUiState()
}

class CultivoViewModel(
    private val repository: CultivoRepository = CultivoRepository()
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

    fun loadCultivos() {
        _uiState.value = CultivoUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getCultivos()
                _uiState.value =
                    if (list.isEmpty()) CultivoUiState.Empty else CultivoUiState.Success(list)
            } catch (t: Throwable) {
                _uiState.value = CultivoUiState.Error(t.message ?: "Error desconocido")
            }
        }
    }

    fun loadCultivoById(id: Int) {
        viewModelScope.launch {
            try {
                val cultivo = repository.getCultivoById(id)
                var selectedPlant: com.alex.hortina.data.remote.dto.PlantProfileDto? = null

                cultivo.plantExternalId?.let { externalId ->
                    try {
                        selectedPlant = plantasRepository.getPlantById(externalId)
                    } catch (e: Exception) {
                        println("No se pudo cargar el perfil de planta para ID $externalId: ${e.message}")
                    }
                }

                _formState.value = CultivoFormState(
                    nombre = cultivo.nombre ?: "",
                    tipo = cultivo.tipo ?: "",
                    fechaPlantacion = cultivo.fecha_plantacion ?: "",
                    estado = cultivo.estado ?: "",
                    imagen = cultivo.imagen ?: "",
                    selectedPlant = selectedPlant
                )
            } catch (e: Exception) {
                _formState.value =
                    _formState.value.copy(error = "Error al cargar cultivo: ${e.message}")
            }
        }
    }


    fun updateCultivo(id: Int) {
        val form = _formState.value
        viewModelScope.launch {
            try {
                val dto = CultivoDto(
                    idCultivo = id,
                    id_usuario = null,
                    plantExternalId = form.selectedPlant?.externalId,
                    id_ubicacion = null,
                    nombre = form.nombre.ifBlank { form.selectedPlant?.commonName },
                    tipo = form.tipo.ifBlank { form.selectedPlant?.scientificName },
                    fecha_plantacion = form.fechaPlantacion.ifBlank { null },
                    estado = form.estado,
                    imagen = form.imagen ?: form.selectedPlant?.imageUrl,
                    fecha_estimada_cosecha = null
                )
                repository.updateCultivo(id, dto)
                _creationSuccess.value = true
                loadCultivos()
            } catch (t: Throwable) {
                _formState.value = form.copy(error = t.message ?: "Error al actualizar cultivo")
            }
        }
    }


    fun onFormChange(updated: CultivoFormState) {
        _formState.value = updated
    }

    fun createCultivo() {
        val form = _formState.value
        if (!form.isValid()) {
            _formState.value = form.copy(error = "Por favor completa los campos obligatorios")
            return
        }

        viewModelScope.launch {
            try {
                val cultivo = repository.createCultivo(
                    CultivoDto(
                        idCultivo = null,
                        id_usuario = null,
                        plantExternalId = form.selectedPlant?.externalId,
                        id_ubicacion = null,
                        nombre = form.nombre.ifBlank { form.selectedPlant?.commonName },
                        tipo = form.tipo.ifBlank { form.selectedPlant?.scientificName },
                        fecha_plantacion = form.fechaPlantacion.ifBlank { null },
                        estado = form.estado,
                        imagen = form.imagen ?: form.selectedPlant?.imageUrl,
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
