package ru.lissa_lesia.iteams.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.domain.repositories.AuthState
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository

class AuthStateManager(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState?>(null)
    val authState: StateFlow<AuthState?> = _authState.asStateFlow()

    private var authStateJob: kotlinx.coroutines.Job? = null

    init {
        authStateJob = viewModelScope.launch {
            authRepository.getAuthState().collect { state ->
                _authState.value = state
            }
        }
    }

    fun logout() {
        authRepository.signOut()
    }

    fun getCurrentUser(): User? = _authState.value?.user

    override fun onCleared() {
        super.onCleared()
        authStateJob?.cancel()
    }
}
