package ru.lissa_lesia.iteams.domain.repositories

import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.domain.utils.Result

interface IAuthRepository {
    // Регистрация нового пользователя
    suspend fun signUp(email: String, password: String, name: String): Result<User>

    // Вход существующего пользователя
    suspend fun signIn(email: String, password: String): Result<User>

    // Выход
    fun signOut()

    // Получить текущего авторизованного пользователя (если есть)
    fun getCurrentUser(): User?

    // Обновить данные профиля (имя, био, навыки и т.д.)
    suspend fun updateProfile(user: User): Result<Unit>
}