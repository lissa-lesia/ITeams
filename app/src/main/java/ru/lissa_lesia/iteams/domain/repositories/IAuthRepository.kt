package ru.lissa_lesia.iteams.domain.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.domain.utils.Result

data class AuthState(
    val user: User,
    val isAuthenticated: Boolean
)

interface IAuthRepository {

    suspend fun signUp(email: String, password: String, name: String): Result<User>

    suspend fun signIn(email: String, password: String): Result<User>

    fun signOut()

    fun getCurrentUser(): User?

    suspend fun updateProfile(user: User): Result<Unit>

    suspend fun getUserById(userId: String): Result<User>

    fun getAuthState(): Flow<AuthState?>

    val authState: StateFlow<AuthState?>
}