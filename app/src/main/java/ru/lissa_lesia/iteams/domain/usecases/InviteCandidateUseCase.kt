package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class InviteCandidateUseCase(
    private val projectRepository: IProjectRepository
) {
    suspend operator fun invoke(
        projectId: String,
        candidateId: String,
        userId: String,
        role: String
    ): Result<Unit> {
        return projectRepository.inviteCandidate(projectId, candidateId, userId, role)
    }
}