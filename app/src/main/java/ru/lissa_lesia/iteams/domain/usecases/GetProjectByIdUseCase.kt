package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class GetProjectByIdUseCase(
    private val projectRepository: IProjectRepository
) {
    suspend operator fun invoke(projectId: String): Result<Project> {
        return projectRepository.getProjectById(projectId)
    }
}