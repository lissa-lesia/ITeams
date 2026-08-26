package ru.lissa_lesia.iteams.presentation.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.usecases.RegisterUseCase
import ru.lissa_lesia.iteams.domain.utils.Result

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, isError = false, isSuccess = false)
            when (val result = registerUseCase(email, password, name)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    onSuccess()
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.value = RegisterUiState()
    }
}