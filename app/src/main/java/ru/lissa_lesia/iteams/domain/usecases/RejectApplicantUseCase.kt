package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class RejectApplicantUseCase(
    private val projectRepository: IProjectRepository
) {
    suspend operator fun invoke(projectId: String, applicantId: String): Result<Unit> {
        return projectRepository.rejectApplicant(projectId, applicantId)
    }
}