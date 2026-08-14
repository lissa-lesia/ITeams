package ru.lissa_lesia.iteams.presentation.screens.editproject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class EditProjectUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val title: String = "",
    val description: String = "",
    val requiredRolesInput: String = "",
    val requiredSkillsInput: String = "",
    val isOpen: Boolean = true,
    val authorRole: String = "",
    val projectId: String = ""
)

class EditProjectViewModel(
    private val projectRepository: IProjectRepository,
    private val authStateManager: AuthStateManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: String = savedStateHandle.get<String>("projectId") ?: ""

    private val _uiState = MutableStateFlow(EditProjectUiState(isLoading = true, projectId = projectId))
    val uiState: StateFlow<EditProjectUiState> = _uiState.asStateFlow()

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
                    val project = result.data
                    val currentUser = authStateManager.getCurrentUser()
                    if (currentUser == null || project.authorId != currentUser.id) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "У вас нет прав на редактирование этого проекта"
                        )
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = project.title,
                        description = project.description,
                        requiredRolesInput = project.requiredRoles.joinToString(", "),
                        requiredSkillsInput = project.requiredSkills.joinToString(", "),
                        isOpen = project.status == ProjectStatus.OPEN,
                        authorRole = project.authorRole
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

    fun updateAuthorRole(role: String) {
        _uiState.value = _uiState.value.copy(authorRole = role)
    }

    fun saveProject(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, isSuccess = false)
            val currentState = _uiState.value

            if (currentState.title.isBlank()) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    errorMessage = "Название проекта не может быть пустым"
                )
                return@launch
            }
            if (currentState.description.isBlank()) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    errorMessage = "Описание не может быть пустым"
                )
                return@launch
            }
            if (currentState.authorRole.isBlank()) {
                _uiState.value = currentState.copy(
                    isSaving = false,
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

            val projectResult = projectRepository.getProjectById(projectId)
            if (projectResult is Result.Error) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    errorMessage = "Не удалось загрузить проект для обновления"
                )
                return@launch
            }
            val existingProject = (projectResult as Result.Success).data
            val currentUser = authStateManager.getCurrentUser()
            if (currentUser == null || existingProject.authorId != currentUser.id) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    errorMessage = "У вас нет прав на редактирование этого проекта"
                )
                return@launch
            }

            val updatedProject = existingProject.copy(
                title = currentState.title,
                description = currentState.description,
                requiredRoles = roles,
                requiredSkills = skills,
                status = if (currentState.isOpen) ProjectStatus.OPEN else ProjectStatus.CLOSED,
                authorRole = currentState.authorRole,
                members = existingProject.members.map { member ->
                    if (member.userId == currentUser.id) {
                        member.copy(
                            role = currentState.authorRole,
                            userName = currentUser.name
                        )
                    } else {
                        member
                    }
                }
            )

            when (val result = projectRepository.updateProject(updatedProject)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSuccess = true
                    )
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}