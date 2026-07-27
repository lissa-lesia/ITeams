package ru.lissa_lesia.iteams.presentation.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Applicant
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result

data class ProjectDetailsUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val errorMessage: String? = null,
    val isApplying: Boolean = false,
    val applySuccess: Boolean = false,
    val applyError: String? = null,
    val currentUserId: String? = null,
    val isAuthor: Boolean = false,
    val isApplicant: Boolean = false,
    val selectedRole: String = ""
    // membersNames удалено
)

class ProjectDetailsViewModel(
    private val projectRepository: IProjectRepository,
    private val authRepository: IAuthRepository,
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
                    val project = result.data
                    val currentUserId = authRepository.getCurrentUser()?.id
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        project = project,
                        currentUserId = currentUserId,
                        isAuthor = project.authorId == currentUserId,
                        isApplicant = project.applicants.any { it.userId == currentUserId }
                        // membersNames больше не нужны
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

    fun updateSelectedRole(role: String) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun applyForProject(onSuccess: () -> Unit) {
        val state = _uiState.value
        val project = state.project ?: return
        val userId = state.currentUserId ?: return
        val role = state.selectedRole

        if (state.isAuthor) {
            _uiState.value = state.copy(applyError = "Вы автор проекта")
            return
        }
        if (state.isApplicant) {
            _uiState.value = state.copy(applyError = "Вы уже подали заявку")
            return
        }
        if (project.status != ProjectStatus.OPEN) {
            _uiState.value = state.copy(applyError = "Набор в проект закрыт")
            return
        }
        if (role.isBlank()) {
            _uiState.value = state.copy(applyError = "Выберите роль")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isApplying = true, applySuccess = false, applyError = null)
            when (val result = projectRepository.applyToProject(project.id, userId, role)) {
                is Result.Success -> {
                    val updatedApplicants = project.applicants + Applicant(userId, role)
                    val updatedProject = project.copy(applicants = updatedApplicants)
                    _uiState.value = _uiState.value.copy(
                        isApplying = false,
                        project = updatedProject,
                        isApplicant = true,
                        applySuccess = true
                    )
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isApplying = false,
                        applyError = result.message
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            projectRepository: IProjectRepository,
            authRepository: IAuthRepository,
            savedStateHandle: SavedStateHandle
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProjectDetailsViewModel(projectRepository, authRepository, savedStateHandle) as T
            }
        }
    }
}