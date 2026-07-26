package ru.lissa_lesia.iteams.presentation.screens.createproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result

data class CreateProjectUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val title: String = "",
    val description: String = "",
    val requiredRolesInput: String = "",
    val requiredSkillsInput: String = "",
    val isOpen: Boolean = true // true = OPEN, false = CLOSED
)

class CreateProjectViewModel(
    private val projectRepository: IProjectRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateRequiredRolesInput(input: String) {
        _uiState.value = _uiState.value.copy(requiredRolesInput = input)
    }

    fun updateRequiredSkillsInput(input: String) {
        _uiState.value = _uiState.value.copy(requiredSkillsInput = input)
    }

    fun updateIsOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isOpen = isOpen)
    }

    fun createProject(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Валидация
            if (currentState.title.isBlank()) {
                _uiState.value = currentState.copy(errorMessage = "Название проекта не может быть пустым")
                return@launch
            }
            if (currentState.description.isBlank()) {
                _uiState.value = currentState.copy(errorMessage = "Описание не может быть пустым")
                return@launch
            }

            val roles = currentState.requiredRolesInput
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val skills = currentState.requiredSkillsInput
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            val project = Project(
                title = currentState.title,
                description = currentState.description,
                authorId = currentUserId,
                requiredRoles = roles,
                requiredSkills = skills,
                status = if (currentState.isOpen) ProjectStatus.OPEN else ProjectStatus.CLOSED,
                createdAt = System.currentTimeMillis()
            )

            when (val result = projectRepository.createProject(project)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    onSuccess()
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

    fun resetState() {
        _uiState.value = CreateProjectUiState()
    }

    companion object {
        fun provideFactory(
            projectRepository: IProjectRepository,
            currentUserId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateProjectViewModel(projectRepository, currentUserId) as T
            }
        }
    }
}