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
    val isEditing: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isSaveSuccess: Boolean = false
)

class ProfileViewModel(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                user = currentUser
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Пользователь не авторизован"
            )
        }
    }

    fun toggleEditing() {
        _uiState.value = _uiState.value.copy(
            isEditing = !_uiState.value.isEditing,
            isSaveSuccess = false
        )
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isSaveSuccess = false
            )
            when (val result = authRepository.updateProfile(updatedUser)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = updatedUser,
                        isEditing = false,
                        isSaveSuccess = true
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
        fun provideFactory(authRepository: IAuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(authRepository) as T
                }
            }
    }
}