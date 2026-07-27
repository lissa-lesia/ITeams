package ru.lissa_lesia.iteams.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import ru.lissa_lesia.iteams.data.mappers.UserMapper
import ru.lissa_lesia.iteams.data.models.FirebaseUserDto
import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.domain.repositories.AuthState
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IAuthRepository {

    private val usersCollection = firestore.collection("users")

    private val _authState = MutableStateFlow<AuthState?>(null)
    override val authState: StateFlow<AuthState?> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            // Пользователь авторизован - загружаем данные из Firestore
            loadUserFromFirestore(firebaseUser.uid) { user ->
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
        auth.addAuthStateListener(authStateListener)
    }

    private fun loadUserFromFirestore(userId: String, onResult: (User?) -> Unit) {
        try {
            usersCollection.document(userId).get()
                .addOnSuccessListener { document ->
                    val dto = document.toObject(FirebaseUserDto::class.java)
                    val user = dto?.let { UserMapper.toDomain(it) }
                    onResult(user)
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } catch (e: Exception) {
            onResult(null)
        }
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.Error("Ошибка создания пользователя")

            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            firebaseUser.updateProfile(profileUpdates).await()
            firebaseUser.reload().await()

            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
                bio = "",
                avatarUrl = "",
                skills = emptyList()
            )
            val dto = UserMapper.toDto(user)

            usersCollection.document(firebaseUser.uid).set(dto).await()

            _authState.value = AuthState(
                user = user,
                isAuthenticated = true
            )

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка регистрации")
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.Error("Пользователь не найден")

            val document = usersCollection.document(firebaseUser.uid).get().await()
            val dto = document.toObject(FirebaseUserDto::class.java)

            return if (dto != null) {
                val user = UserMapper.toDomain(dto)
                _authState.value = AuthState(
                    user = user,
                    isAuthenticated = true
                )
                Result.Success(user)
            } else {
                val fallbackUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: email,
                    email = email
                )
                _authState.value = AuthState(
                    user = fallbackUser,
                    isAuthenticated = true
                )
                Result.Success(fallbackUser)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка входа. Проверьте email и пароль")
        }
    }

    override fun signOut() {
        auth.signOut()
        _authState.value = null
    }

    override fun getCurrentUser(): User? {
        return _authState.value?.user
    }

    override fun getAuthState(): Flow<AuthState?> = authState

    override suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = user.name
                }
                firebaseUser.updateProfile(profileUpdates).await()
            }

            val dto = UserMapper.toDto(user)
            usersCollection.document(user.id).set(dto).await()

            _authState.value = AuthState(
                user = user,
                isAuthenticated = true
            )

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка обновления профиля")
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val dto = document.toObject(FirebaseUserDto::class.java)
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