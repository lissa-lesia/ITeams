package ru.lissa_lesia.iteams.presentation.screens.createcandidate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.models.CandidateStatus
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class CreateCandidateUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val desiredRolesInput: String = "",
    val skillsInput: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val existingCandidateId: String? = null // для редактирования
)

class CreateCandidateViewModel(
    private val candidateRepository: ICandidateRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCandidateUiState())
    val uiState: StateFlow<CreateCandidateUiState> = _uiState.asStateFlow()

    // Загружаем существующую заявку, если есть
    fun loadExistingCandidate() {
        viewModelScope.launch {
            val currentUser = authStateManager.getCurrentUser()
            if (currentUser == null) return@launch

            when (val result = candidateRepository.getCandidatesByUserId(currentUser.id)) {
                is Result.Success -> {
                    val active = result.data.find { it.status == CandidateStatus.ACTIVE }
                    if (active != null) {
                        _uiState.value = _uiState.value.copy(
                            existingCandidateId = active.id,
                            desiredRolesInput = active.desiredRoles.joinToString(", "),
                            skillsInput = active.skills.joinToString(", "),
                            description = active.description,
                            isActive = active.status == CandidateStatus.ACTIVE
                        )
                    }
                }
                is Result.Error -> {
                    // Игнорируем, просто нет заявки
                }
            }
        }
    }

    fun updateDesiredRolesInput(input: String) {
        _uiState.value = _uiState.value.copy(desiredRolesInput = input)
    }

    fun updateSkillsInput(input: String) {
        _uiState.value = _uiState.value.copy(skillsInput = input)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateIsActive(isActive: Boolean) {
        _uiState.value = _uiState.value.copy(isActive = isActive)
    }

    fun saveCandidate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, isSuccess = false)
            val state = _uiState.value

            val currentUser = authStateManager.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Пользователь не авторизован"
                )
                return@launch
            }

            if (state.desiredRolesInput.isBlank()) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Укажите хотя бы одну желаемую роль"
                )
                return@launch
            }

            val roles = state.desiredRolesInput
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val skills = state.skillsInput
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val candidate = Candidate(
                id = state.existingCandidateId ?: "",
                userId = currentUser.id,
                userName = currentUser.name,
                userEmail = currentUser.email,
                desiredRoles = roles,
                skills = skills,
                description = state.description,
                status = if (state.isActive) CandidateStatus.ACTIVE else CandidateStatus.CLOSED,
                createdAt = System.currentTimeMillis()
            )

            val result = if (state.existingCandidateId != null) {
                candidateRepository.updateCandidate(candidate)
            } else {
                candidateRepository.createCandidate(candidate)
            }

            when (result) {
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
        _uiState.value = CreateCandidateUiState()
    }
}