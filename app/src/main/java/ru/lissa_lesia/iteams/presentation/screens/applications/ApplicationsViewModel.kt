package ru.lissa_lesia.iteams.presentation.screens.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.usecases.AcceptApplicantUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetCurrentUserUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetInvitationsForUserUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetProjectsByUserIdUseCase
import ru.lissa_lesia.iteams.domain.usecases.RejectApplicantUseCase
import ru.lissa_lesia.iteams.domain.usecases.RespondToInvitationUseCase
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class ApplicationsUiState(
    val isLoading: Boolean = true,
    val incomingApplications: List<Project> = emptyList(),
    val outgoingApplications: List<Project> = emptyList(),
    val invitations: List<Project> = emptyList(),
    val errorMessage: String? = null,
    val processingIds: Set<String> = emptySet(),
    val isProcessingInvitation: Boolean = false
)

class ApplicationsViewModel(
    private val getProjectsByUserIdUseCase: GetProjectsByUserIdUseCase,
    private val getInvitationsForUserUseCase: GetInvitationsForUserUseCase,
    private val acceptApplicantUseCase: AcceptApplicantUseCase,
    private val rejectApplicantUseCase: RejectApplicantUseCase,
    private val respondToInvitationUseCase: RespondToInvitationUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationsUiState(isLoading = true))
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    private var authStateJob: Job? = null

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
                        invitations = emptyList(),
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

            val currentUser = getCurrentUserUseCase()
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Пользователь не авторизован"
                )
                return@launch
            }

            when (val result = getProjectsByUserIdUseCase(currentUser.id)) {
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

            loadInvitations()
        }
    }

    private fun loadInvitations() {
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase()
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(invitations = emptyList())
                return@launch
            }

            when (val result = getInvitationsForUserUseCase(currentUser.id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        invitations = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(invitations = emptyList())
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

            when (val result = acceptApplicantUseCase(projectId, applicantId)) {
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

            when (val result = rejectApplicantUseCase(projectId, applicantId)) {
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

    fun acceptInvitation(projectId: String, candidateId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingInvitation = true)

            when (val result = respondToInvitationUseCase(projectId, candidateId, true)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isProcessingInvitation = false)
                    loadApplications()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessingInvitation = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun rejectInvitation(projectId: String, candidateId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingInvitation = true)

            when (val result = respondToInvitationUseCase(projectId, candidateId, false)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isProcessingInvitation = false)
                    loadApplications()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isProcessingInvitation = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        authStateJob?.cancel()
    }
}