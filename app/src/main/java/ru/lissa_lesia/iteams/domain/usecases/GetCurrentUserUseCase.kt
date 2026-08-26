package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository

class GetCurrentUserUseCase(
    private val authRepository: IAuthRepository
) {
    operator fun invoke(): User? = authRepository.getCurrentUser()
}