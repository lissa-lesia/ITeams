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

data class FeedUiState(
    val isLoading: Boolean = false,
    val projects: List<Project> = emptyList(),
    val errorMessage: String? = null
)

class FeedViewModel(
    private val projectRepository: IProjectRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
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

    fun logout(onSuccess: () -> Unit) {
        authRepository.signOut()
        onSuccess()
    }

    companion object {
        fun provideFactory(
            projectRepository: IProjectRepository,
            authRepository: IAuthRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(projectRepository, authRepository) as T
            }
        }
    }
}