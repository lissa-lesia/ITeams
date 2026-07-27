package ru.lissa_lesia.iteams.presentation.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

data class FeedUiState(
    val isLoading: Boolean = true,
    val projects: List<Project> = emptyList(),
    val errorMessage: String? = null,
    val selectedTab: Int = 0 // 0 – Все проекты, 1 – Мои проекты
)

class FeedViewModel(
    private val projectRepository: IProjectRepository,
    private val authRepository: IAuthRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    private var authStateJob: kotlinx.coroutines.Job? = null

    init {
        authStateJob = viewModelScope.launch {
            authStateManager.authState.collect { authState ->
                if (authState?.isAuthenticated == true) {
                    currentUserId = authState.user.id
                    loadProjects()
                } else {
                    currentUserId = null
                    _uiState.value = FeedUiState(
                        isLoading = false,
                        projects = emptyList(),
                        errorMessage = null,
                        selectedTab = 0
                    )
                }
            }
        }
    }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            when (val result = projectRepository.getFeedProjects()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        projects = result.data
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

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun getFilteredProjects(): List<Project> {
        val state = _uiState.value
        return when (state.selectedTab) {
            0 -> state.projects
            1 -> {
                val userId = currentUserId
                if (userId == null) emptyList()
                else state.projects.filter { project ->
                    project.members.any { it.userId == userId }
                }
            }
            else -> state.projects
        }
    }

    override fun onCleared() {
        super.onCleared()
        authStateJob?.cancel()
    }

    companion object {
        fun provideFactory(
            projectRepository: IProjectRepository,
            authRepository: IAuthRepository,
            authStateManager: AuthStateManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(projectRepository, authRepository, authStateManager) as T
            }
        }
    }
}