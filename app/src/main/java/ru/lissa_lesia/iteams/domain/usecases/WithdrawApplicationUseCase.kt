package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class WithdrawApplicationUseCase(
    private val projectRepository: IProjectRepository
) {
    suspend operator fun invoke(projectId: String, userId: String): Result<Unit> {
        return projectRepository.withdrawApplication(projectId, userId)
    }
}