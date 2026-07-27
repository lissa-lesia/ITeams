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

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isEditing: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class ProfileViewModel(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                when (val result = authRepository.getUserById(currentUser.id)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = result.data
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


    fun logout(onSuccess: () -> Unit) {
        authRepository.signOut()
        onSuccess()
    }

    companion object {
        fun provideFactory(authRepository: IAuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(authRepository) as T
                }
            }
    }
}