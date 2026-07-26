package ru.lissa_lesia.iteams.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ru.lissa_lesia.iteams.data.mappers.UserMapper
import ru.lissa_lesia.iteams.data.models.FirebaseUserDto
import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IAuthRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun signUp(email: String, password: String, name: String): Result<User> {
        return try {
            // 1. Создаём пользователя в Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.Error("Ошибка создания пользователя")

            // 2. Обновляем имя в Auth (чтобы оно отображалось в профиле Google/Firebase)
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            firebaseUser.updateProfile(profileUpdates).await()
            // Обновляем email (хотя он и так есть)
            firebaseUser.reload().await()

            // 3. Создаём нашу доменную модель и DTO для Firestore
            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
                bio = "",
                avatarUrl = "",
                skills = emptyList()
            )
            val dto = UserMapper.toDto(user)

            // 4. Сохраняем профиль в Firestore в коллекции "users"
            usersCollection.document(firebaseUser.uid).set(dto).await()

            // 5. Возвращаем успех с данными пользователя
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка регистрации")
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            // 1. Вход в Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.Error("Пользователь не найден")

            // 2. Загружаем профиль из Firestore по UID
            val document = usersCollection.document(firebaseUser.uid).get().await()
            val dto = document.toObject(FirebaseUserDto::class.java)

            return if (dto != null) {
                // 3. Превращаем DTO в чистую модель и возвращаем
                val user = UserMapper.toDomain(dto)
                Result.Success(user)
            } else {
                // Если по какой-то причине профиля в Firestore нет, создаём заглушку
                val fallbackUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: email,
                    email = email
                )
                Result.Success(fallbackUser)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка входа. Проверьте email и пароль")
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUser(): User? {
        // Достаём текущего пользователя из Auth (но у нас нет полных данных из Firestore)
        // Чтобы не делать лишний запрос, возвращаем базовую информацию.
        // Для полного профиля используй отдельный метод загрузки.
        val firebaseUser = auth.currentUser ?: return null
        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: ""
        )
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            // Обновляем данные в Auth (имя)
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = user.name
                }
                firebaseUser.updateProfile(profileUpdates).await()
            }

            // Обновляем данные в Firestore
            val dto = UserMapper.toDto(user)
            usersCollection.document(user.id).set(dto).await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка обновления профиля")
        }
    }
}