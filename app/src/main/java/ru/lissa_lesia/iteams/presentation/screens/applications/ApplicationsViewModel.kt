package ru.lissa_lesia.iteams.presentation.screens.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class ApplicationsUiState(
    val isLoading: Boolean = true,
    val incomingApplications: List<Project> = emptyList(),
    val outgoingApplications: List<Project> = emptyList(),
    val errorMessage: String? = null,
    val processingIds: Set<String> = emptySet()
)

class ApplicationsViewModel(
    private val projectRepository: IProjectRepository,
    private val authStateManager: AuthStateManager,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationsUiState(isLoading = true))
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    private var authStateJob: kotlinx.coroutines.Job? = null

    init {
        authStateJob = viewModelScope.launch {
            authStateManager.authState.collect { authState ->
                if (authState?.isAuthenticated == true) {
                    loadApplications()
                } else {
                    _uiState.value = ApplicationsUiState(
                        isLoading = false,
                        incomingApplications = emptyList(),
                        outgoingApplications = emptyList(),
                        errorMessage = "Пользователь не авторизован"
                    )
                }
            }
        }
    }

    fun loadApplications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val currentUser = authStateManager.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Пользователь не авторизован"
                )
                return@launch
            }

            when (val result = projectRepository.getProjectsByUserId(currentUser.id)) {
                is Result.Success -> {
                    val allProjects = result.data
                    val incoming = allProjects.filter { it.authorId == currentUser.id }
                    val outgoing = allProjects.filter { it.authorId != currentUser.id }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        incomingApplications = incoming,
                        outgoingApplications = outgoing,
                        errorMessage = null
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

    fun acceptApplicant(projectId: String, applicantId: String) {
        viewModelScope.launch {
            val processingKey = "$projectId:$applicantId"
            _uiState.value = _uiState.value.copy(
                processingIds = _uiState.value.processingIds + processingKey
            )

            when (val result = projectRepository.acceptApplicant(projectId, applicantId)) {
                is Result.Success -> {
                    loadApplications()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        processingIds = _uiState.value.processingIds - processingKey
                    )
                }
            }
        }
    }

    fun rejectApplicant(projectId: String, applicantId: String) {
        viewModelScope.launch {
            val processingKey = "$projectId:$applicantId"
            _uiState.value = _uiState.value.copy(
                processingIds = _uiState.value.processingIds + processingKey
            )

            when (val result = projectRepository.rejectApplicant(projectId, applicantId)) {
                is Result.Success -> {
                    loadApplications()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        processingIds = _uiState.value.processingIds - processingKey
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        authStateJob?.cancel()
    }
}