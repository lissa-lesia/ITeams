package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class UpdateProjectUseCase(
    private val projectRepository: IProjectRepository
) {
    suspend operator fun invoke(project: Project): Result<Unit> {
        return projectRepository.updateProject(project)
    }
}