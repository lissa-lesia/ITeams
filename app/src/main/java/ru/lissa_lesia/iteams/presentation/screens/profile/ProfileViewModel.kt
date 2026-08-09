package ru.lissa_lesia.iteams.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isEditing: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val resumeLinkInput: String = "",
    val isSavingResume: Boolean = false
)

class ProfileViewModel(
    private val authRepository: IAuthRepository,
    private val authStateManager: AuthStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var authStateJob: kotlinx.coroutines.Job? = null

    init {
        authStateJob = viewModelScope.launch {
            authStateManager.authState.collect { authState ->
                if (authState?.isAuthenticated == true) {
                    loadUser()
                } else {
                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        user = null,
                        errorMessage = "Пользователь не авторизован"
                    )
                }
            }
        }
    }

    fun loadUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val currentUser = authStateManager.getCurrentUser()
            if (currentUser != null) {
                when (val result = authRepository.getUserById(currentUser.id)) {
                    is Result.Success -> {
                        val user = result.data
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = user,
                            resumeLinkInput = user.resumeUrl ?: ""
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = currentUser,
                            errorMessage = "Не удалось загрузить полный профиль"
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Пользователь не авторизован"
                )
            }
        }
    }

    fun enableEditing() {
        _uiState.value = _uiState.value.copy(isEditing = true, saveSuccess = false)
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(isEditing = false, saveSuccess = false)
    }

    fun updateUser(updatedUser: User) {
        _uiState.value = _uiState.value.copy(user = updatedUser)
    }

    fun updateResumeLinkInput(input: String) {
        _uiState.value = _uiState.value.copy(resumeLinkInput = input)
    }

    fun saveResumeLink(onSuccess: () -> Unit) {
        val user = _uiState.value.user ?: return
        val link = _uiState.value.resumeLinkInput.trim()
        if (link.isEmpty()) {
            saveResumeUrl(user, null, onSuccess)
        } else {
            saveResumeUrl(user, link, onSuccess)
        }
    }

    private fun saveResumeUrl(user: User, url: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingResume = true, errorMessage = null)
            val updatedUser = user.copy(resumeUrl = url)
            when (val result = authRepository.updateProfile(updatedUser)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSavingResume = false,
                        user = updatedUser,
                        resumeLinkInput = url ?: ""
                    )
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSavingResume = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, saveSuccess = false)
            when (val result = authRepository.updateProfile(user)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditing = false,
                        saveSuccess = true
                    )
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        saveSuccess = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        authStateJob?.cancel()
    }

    companion object {
        fun provideFactory(
            authRepository: IAuthRepository,
            authStateManager: AuthStateManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(authRepository, authStateManager) as T
            }
        }
    }
}