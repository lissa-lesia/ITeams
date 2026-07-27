package ru.lissa_lesia.iteams.domain.repositories
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.utils.Result

interface IProjectRepository {
        suspend fun createProject(project: Project): Result<String>
        suspend fun getFeedProjects(): Result<List<Project>>

        suspend fun getProjectById(projectId: String): Result<Project>

}