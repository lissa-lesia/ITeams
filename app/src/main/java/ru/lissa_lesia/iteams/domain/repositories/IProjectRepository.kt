package ru.lissa_lesia.iteams.domain.repositories

import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.utils.Result

interface IProjectRepository {
        suspend fun createProject(project: Project): Result<String>
        suspend fun getFeedProjects(): Result<List<Project>>
        suspend fun getProjectById(projectId: String): Result<Project>
        suspend fun applyToProject(projectId: String, userId: String, role: String): Result<Unit>
        suspend fun getProjectsByUserId(userId: String): Result<List<Project>>
        suspend fun acceptApplicant(projectId: String, applicantId: String): Result<Unit>
        suspend fun rejectApplicant(projectId: String, applicantId: String): Result<Unit>
        suspend fun updateProject(project: Project): Result<Unit>
}