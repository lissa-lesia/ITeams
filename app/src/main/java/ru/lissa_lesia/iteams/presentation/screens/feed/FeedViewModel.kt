package ru.lissa_lesia.iteams.presentation.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class FeedUiState(
    val isLoading: Boolean = true,
    val projects: List<Project> = emptyList(),
    val filteredProjects: List<Project> = emptyList(),
    val errorMessage: String? = null,
    val selectedTab: Int = 0, // 0 – Все проекты, 1 – Мои проекты
    // Поля для поиска и фильтрации
    val searchQuery: String = "",
    val selectedStatus: ProjectStatus? = null,
    val selectedRole: String = "",
    val selectedSkill: String = "",
    val showOnlyMyProjects: Boolean = false
)

class FeedViewModel(
    private val projectRepository: IProjectRepository,
    private val authRepository: IAuthRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var allProjects: List<Project> = emptyList()
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
                        filteredProjects = emptyList(),
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
                    allProjects = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        projects = result.data,
                        filteredProjects = result.data
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

    fun updateSelectedStatus(status: ProjectStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        applyFilters()
    }

    fun updateSelectedRole(role: String) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
        applyFilters()
    }

    fun updateSelectedSkill(skill: String) {
        _uiState.value = _uiState.value.copy(selectedSkill = skill)
        applyFilters()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedStatus = null,
            selectedRole = "",
            selectedSkill = ""
        )
        applyFilters()
    }

    fun getFilteredProjects(): List<Project> {
        return _uiState.value.filteredProjects
    }

    private fun applyFilters() {
        val state = _uiState.value

        // 1. Сначала определяем базовый список проектов в зависимости от вкладки
        val baseProjects = when (state.selectedTab) {
            0 -> allProjects // Все проекты
            1 -> {
                val userId = currentUserId
                if (userId == null) emptyList()
                else allProjects.filter { project ->
                    project.members.any { it.userId == userId }
                }
            }
            else -> allProjects
        }

        val filtered = baseProjects.filter { project ->
            var matches = true

            // Поиск по названию и описанию
            if (state.searchQuery.isNotBlank()) {
                val query = state.searchQuery.lowercase()
                matches = matches && (
                        project.title.lowercase().contains(query) ||
                                project.description.lowercase().contains(query)
                        )
            }

            if (matches && state.selectedStatus != null) {
                matches = matches && project.status == state.selectedStatus
            }

            if (matches && state.selectedRole.isNotBlank()) {
                matches = matches && project.requiredRoles.any { role ->
                    role.lowercase().contains(state.selectedRole.lowercase())
                }
            }

            if (matches && state.selectedSkill.isNotBlank()) {
                matches = matches && project.requiredSkills.any { skill ->
                    skill.lowercase().contains(state.selectedSkill.lowercase())
                }
            }

            matches
        }

        _uiState.value = _uiState.value.copy(filteredProjects = filtered)
    }

    override fun onCleared() {
        super.onCleared()
        authStateJob?.cancel()
    }
}