package ru.lissa_lesia.iteams.presentation.screens.candidatedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class CandidateDetailsUiState(
    val isLoading: Boolean = true,
    val candidate: Candidate? = null,
    val errorMessage: String? = null,
    val isOwner: Boolean = false,
    val currentUserId: String? = null,
    val userProjects: List<Project> = emptyList(),
    val isInviting: Boolean = false,
    val inviteSuccess: Boolean = false,
    val inviteError: String? = null
)

class CandidateDetailsViewModel(
    private val candidateRepository: ICandidateRepository,
    private val projectRepository: IProjectRepository,
    private val authStateManager: AuthStateManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CandidateDetailsUiState(isLoading = true))
    val uiState: StateFlow<CandidateDetailsUiState> = _uiState.asStateFlow()

    private val candidateId: String = savedStateHandle.get<String>("candidateId") ?: ""

    init {
        loadCandidate()
        loadUserProjects()
    }

    private fun loadCandidate() {
        if (candidateId.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "ID кандидата не указан"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val currentUser = authStateManager.getCurrentUser()

            when (val result = candidateRepository.getCandidateById(candidateId)) {
                is Result.Success -> {
                    val candidate = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        candidate = candidate,
                        currentUserId = currentUser?.id,
                        isOwner = currentUser?.id == candidate.userId
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

    private fun loadUserProjects() {
        viewModelScope.launch {
            val currentUser = authStateManager.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(userProjects = emptyList())
                return@launch
            }

            when (val result = projectRepository.getProjectsByUserId(currentUser.id)) {
                is Result.Success -> {
                    val ownOpenProjects = result.data.filter {
                        it.authorId == currentUser.id && it.status.name == "OPEN"
                    }
                    _uiState.value = _uiState.value.copy(
                        userProjects = ownOpenProjects
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(userProjects = emptyList())
                }
            }
        }
    }

    fun refresh() {
        loadCandidate()
        loadUserProjects()
    }

    fun inviteCandidate(projectId: String, role: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isInviting = true,
                inviteError = null,
                inviteSuccess = false
            )

            val candidate = _uiState.value.candidate
            if (candidate == null) {
                _uiState.value = _uiState.value.copy(
                    isInviting = false,
                    inviteError = "Кандидат не найден"
                )
                onResult(false)
                return@launch
            }

            val currentUser = authStateManager.getCurrentUser()
            if (currentUser?.id == candidate.userId) {
                _uiState.value = _uiState.value.copy(
                    isInviting = false,
                    inviteError = "Нельзя пригласить самого себя"
                )
                onResult(false)
                return@launch
            }

            when (val result = projectRepository.inviteCandidate(
                projectId,
                candidate.id,
                candidate.userId,
                role
            )) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isInviting = false,
                        inviteSuccess = true,
                        inviteError = null
                    )
                    onResult(true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isInviting = false,
                        inviteError = result.message,
                        inviteSuccess = false
                    )
                    onResult(false)
                }
            }
        }
    }

    fun clearInviteError() {
        _uiState.value = _uiState.value.copy(inviteError = null)
    }

    fun clearInviteSuccess() {
        _uiState.value = _uiState.value.copy(inviteSuccess = false)
    }

    override fun onCleared() {
        super.onCleared()
    }
}