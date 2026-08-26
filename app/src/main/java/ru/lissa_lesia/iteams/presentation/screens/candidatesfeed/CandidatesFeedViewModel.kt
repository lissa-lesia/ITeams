package ru.lissa_lesia.iteams.presentation.screens.candidatesfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.usecases.GetCandidatesUseCase
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class CandidatesFeedUiState(
    val isLoading: Boolean = true,
    val candidates: List<Candidate> = emptyList(),
    val filteredCandidates: List<Candidate> = emptyList(),
    val errorMessage: String? = null,
    val selectedTab: Int = 0, // 0 – Все, 1 – Мои
    val searchQuery: String = "",
    val selectedRole: String = ""
)

class CandidatesFeedViewModel(
    private val getCandidatesUseCase: GetCandidatesUseCase,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CandidatesFeedUiState(isLoading = true))
    val uiState: StateFlow<CandidatesFeedUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var allCandidates: List<Candidate> = emptyList()
    private var authStateJob: Job? = null

    init {
        authStateJob = viewModelScope.launch {
            authStateManager.authState.collect { authState ->
                if (authState?.isAuthenticated == true) {
                    currentUserId = authState.user.id
                    loadCandidates()
                } else {
                    currentUserId = null
                    _uiState.value = CandidatesFeedUiState(
                        isLoading = false,
                        candidates = emptyList(),
                        filteredCandidates = emptyList(),
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun loadCandidates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = getCandidatesUseCase()) {
                is Result.Success -> {
                    allCandidates = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        candidates = result.data
                    )
                    applyFilters()
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

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun updateSelectedRole(role: String) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
        applyFilters()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedRole = ""
        )
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value

        val baseList = when (state.selectedTab) {
            0 -> allCandidates
            1 -> {
                val userId = currentUserId
                if (userId == null) emptyList()
                else allCandidates.filter { it.userId == userId }
            }
            else -> allCandidates
        }

        val filtered = baseList.filter { candidate ->
            var matches = true

            if (state.searchQuery.isNotBlank()) {
                val query = state.searchQuery.lowercase()
                matches = matches && (
                        candidate.userName.lowercase().contains(query) ||
                                candidate.description.lowercase().contains(query) ||
                                candidate.desiredRoles.any { it.lowercase().contains(query) } ||
                                candidate.skills.any { it.lowercase().contains(query) }
                        )
            }

            if (matches && state.selectedRole.isNotBlank()) {
                matches = matches && candidate.desiredRoles.any { role ->
                    role.lowercase().contains(state.selectedRole.lowercase())
                }
            }

            matches
        }

        _uiState.value = _uiState.value.copy(filteredCandidates = filtered)
    }

    override fun onCleared() {
        super.onCleared()
        authStateJob?.cancel()
    }
}