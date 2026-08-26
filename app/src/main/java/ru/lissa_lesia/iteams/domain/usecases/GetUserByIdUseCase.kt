package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class GetUserByIdUseCase(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return authRepository.getUserById(userId)
    }
}