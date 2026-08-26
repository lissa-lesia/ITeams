package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class RespondToInvitationUseCase(
    private val projectRepository: IProjectRepository
) {
    suspend operator fun invoke(
        projectId: String,
        candidateId: String,
        accept: Boolean
    ): Result<Unit> {
        return projectRepository.respondToInvitation(projectId, candidateId, accept)
    }
}