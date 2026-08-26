package ru.lissa_lesia.iteams.data.repositories

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.lissa_lesia.iteams.data.mappers.UserMapper
import ru.lissa_lesia.iteams.data.sources.AuthDataSource
import ru.lissa_lesia.iteams.data.sources.UserDataSource
import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.domain.repositories.AuthState
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource,
    private val userDataSource: UserDataSource
) : IAuthRepository {

    // Скоуп для запуска корутин внутри колбэков, не привязанных к жизненному циклу UI
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _authState = MutableStateFlow<AuthState?>(null)
    override val authState: StateFlow<AuthState?> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            // Запускаем корутину для асинхронной загрузки данных из Firestore
            repositoryScope.launch {
                val user = try {
                    val dto = userDataSource.getUserById(firebaseUser.uid)
                    dto?.let { UserMapper.toDomain(it) }
                } catch (e: Exception) {
                    null
                }
                _authState.value = AuthState(
                    user = user ?: User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: ""
                    ),
                    isAuthenticated = true
                )
            }
        } else {
            _authState.value = null
        }
    }

    init {
        authDataSource.addAuthStateListener(authStateListener)
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.signUp(email, password)
                ?: return Result.Error("Ошибка создания пользователя")

            authDataSource.updateProfile(firebaseUser, name)

            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
                bio = "",
                avatarUrl = "",
                skills = emptyList()
            )
            val dto = UserMapper.toDto(user)
            userDataSource.saveUser(dto)

            _authState.value = AuthState(user = user, isAuthenticated = true)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка регистрации")
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.signIn(email, password)
                ?: return Result.Error("Пользователь не найден")

            val dto = userDataSource.getUserById(firebaseUser.uid)
            if (dto != null) {
                val user = UserMapper.toDomain(dto)
                _authState.value = AuthState(user = user, isAuthenticated = true)
                Result.Success(user)
            } else {
                val fallbackUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: email,
                    email = email
                )
                _authState.value = AuthState(user = fallbackUser, isAuthenticated = true)
                Result.Success(fallbackUser)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка входа. Проверьте email и пароль")
        }
    }

    override fun signOut() {
        authDataSource.signOut()
        _authState.value = null
    }

    override fun getCurrentUser(): User? = _authState.value?.user

    override fun getAuthState(): Flow<AuthState?> = authState

    override suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            val firebaseUser = authDataSource.getCurrentUser()
            if (firebaseUser != null) {
                authDataSource.updateProfile(firebaseUser, user.name)
            }

            val dto = UserMapper.toDto(user)
            userDataSource.updateUser(dto)

            _authState.value = AuthState(user = user, isAuthenticated = true)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка обновления профиля")
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val dto = userDataSource.getUserById(userId)
            if (dto != null) {
                Result.Success(UserMapper.toDomain(dto))
            } else {
                Result.Error("Пользователь не найден")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки пользователя")
        }
    }
}