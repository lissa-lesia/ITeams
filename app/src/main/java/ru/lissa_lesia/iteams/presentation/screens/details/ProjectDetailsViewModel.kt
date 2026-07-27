package ru.lissa_lesia.iteams.presentation.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result

data class ProjectDetailsUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val errorMessage: String? = null
)

class ProjectDetailsViewModel(
    private val projectRepository: IProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: String = savedStateHandle.get<String>("projectId") ?: ""

    private val _uiState = MutableStateFlow(ProjectDetailsUiState(isLoading = true))
    val uiState: StateFlow<ProjectDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        if (projectId.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "ID проекта не указан"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = projectRepository.getProjectById(projectId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        project = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            projectRepository: IProjectRepository,
            savedStateHandle: SavedStateHandle
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProjectDetailsViewModel(projectRepository, savedStateHandle) as T
            }
        }
    }
}