package ru.lissa_lesia.iteams.presentation.screens.createproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Member
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class CreateProjectUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val title: String = "",
    val description: String = "",
    val requiredRolesInput: String = "",
    val requiredSkillsInput: String = "",
    val isOpen: Boolean = true,
    val authorRole: String = ""
)

class CreateProjectViewModel(
    private val projectRepository: IProjectRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    fun createProject(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSuccess = false, errorMessage = null)
            val currentState = _uiState.value

            val currentUser = authStateManager.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Пользователь не авторизован"
                )
                return@launch
            }

            // Валидация
            if (currentState.title.isBlank()) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Название проекта не может быть пустым"
                )
                return@launch
            }
            if (currentState.description.isBlank()) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Описание не может быть пустым"
                )
                return@launch
            }
            if (currentState.authorRole.isBlank()) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Укажите вашу роль в проекте"
                )
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

            val project = Project(
                title = currentState.title,
                description = currentState.description,
                authorId = currentUser.id,
                authorName = currentUser.name,
                authorRole = currentState.authorRole,
                requiredRoles = roles,
                requiredSkills = skills,
                status = if (currentState.isOpen) ProjectStatus.OPEN else ProjectStatus.CLOSED,
                createdAt = System.currentTimeMillis(),
                members = listOf(
                    Member(
                        userId = currentUser.id,
                        role = currentState.authorRole,
                        userName = currentUser.name   // добавляем имя
                    )
                )
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

    fun updateAuthorRole(role: String) {
        _uiState.value = _uiState.value.copy(authorRole = role)
    }

    fun updateIsOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isOpen = isOpen)
    }

    companion object {
        fun provideFactory(
            projectRepository: IProjectRepository,
            authStateManager: AuthStateManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateProjectViewModel(projectRepository, authStateManager) as T
            }
        }
    }
}