package ru.lissa_lesia.iteams.presentation.screens.candidatedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class CandidateDetailsUiState(
    val isLoading: Boolean = true,
    val candidate: Candidate? = null,
    val errorMessage: String? = null,
    val isOwner: Boolean = false,
    val currentUserId: String? = null
)

class CandidateDetailsViewModel(
    private val candidateRepository: ICandidateRepository,
    private val authStateManager: AuthStateManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CandidateDetailsUiState(isLoading = true))
    val uiState: StateFlow<CandidateDetailsUiState> = _uiState.asStateFlow()

    private val candidateId: String = savedStateHandle.get<String>("candidateId") ?: ""

    init {
        loadCandidate()
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

    fun refresh() {
        loadCandidate()
    }

    // Метод для приглашения (заглушка, будет реализован позже)
    fun inviteToProject(projectId: String) {
        // Здесь будет логика приглашения
        println("Приглашаем пользователя ${uiState.value.candidate?.userId} в проект $projectId")
    }
}