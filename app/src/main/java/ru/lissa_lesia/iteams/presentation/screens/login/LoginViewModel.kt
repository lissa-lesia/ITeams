package ru.lissa_lesia.iteams.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.utils.Result

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,  // добавлено
    val isError: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false, isSuccess = false)
            when (val result = authRepository.signIn(email, password)) {
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
                        isError = true,
                        errorMessage = result.message,
                        isSuccess = false
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }

    companion object {
        fun provideFactory(authRepository: IAuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(authRepository) as T
                }
            }
    }
}