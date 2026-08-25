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
        suspend fun withdrawApplication(projectId: String, userId: String): Result<Unit>

        suspend fun inviteCandidate(
                projectId: String,
                candidateId: String,
                userId: String,
                role: String
        ): Result<Unit>

        suspend fun respondToInvitation(
                projectId: String,
                candidateId: String,
                accept: Boolean
        ): Result<Unit>

        suspend fun getInvitationsForUser(userId: String): Result<List<Project>>
}