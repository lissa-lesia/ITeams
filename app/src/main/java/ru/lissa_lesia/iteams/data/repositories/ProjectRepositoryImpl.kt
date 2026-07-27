package ru.lissa_lesia.iteams.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import ru.lissa_lesia.iteams.data.mappers.ProjectMapper
import ru.lissa_lesia.iteams.data.models.FirebaseProjectDto
import ru.lissa_lesia.iteams.domain.models.Member
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import java.util.UUID

class ProjectRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: IAuthRepository
) : IProjectRepository {

    private val projectsCollection = firestore.collection("projects")

    override suspend fun createProject(project: Project): Result<String> {
        return try {
            val projectId = UUID.randomUUID().toString()
            val dto = ProjectMapper.toDto(project).apply { id = projectId }
            projectsCollection.document(projectId).set(dto).await()
            Result.Success(projectId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка при создании проекта")
        }
    }

    override suspend fun getFeedProjects(): Result<List<Project>> {
        return try {
            val snapshot = projectsCollection.get().await()
            val projects = snapshot.documents.mapNotNull { document ->
                val dto = document.toObject<FirebaseProjectDto>()
                dto?.let { ProjectMapper.toDomain(it) }
            }
            Result.Success(projects.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки ленты проектов")
        }
    }

    override suspend fun getProjectById(projectId: String): Result<Project> {
        return try {
            val document = projectsCollection.document(projectId).get().await()
            val dto = document.toObject<FirebaseProjectDto>()
            if (dto != null) {
                Result.Success(ProjectMapper.toDomain(dto))
            } else {
                Result.Error("Проект не найден")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки проекта")
        }
    }

    override suspend fun applyToProject(projectId: String, userId: String, role: String): Result<Unit> {
        return try {
            val document = projectsCollection.document(projectId).get().await()
            val dto = document.toObject<FirebaseProjectDto>()
            if (dto == null) {
                return Result.Error("Проект не найден")
            }
            if (dto.status != ru.lissa_lesia.iteams.domain.models.ProjectStatus.OPEN.name) {
                return Result.Error("Набор в проект закрыт")
            }
            if (dto.applicants.any { it["userId"] == userId }) {
                return Result.Error("Вы уже подали заявку")
            }

            val userName = when (val userResult = authRepository.getUserById(userId)) {
                is Result.Success -> userResult.data.name
                else -> userId // fallback
            }

            val newApplicant = mapOf(
                "userId" to userId,
                "role" to role,
                "userName" to userName
            )
            val newApplicants = dto.applicants + newApplicant
            projectsCollection.document(projectId).update("applicants", newApplicants).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при подаче заявки")
        }
    }

    override suspend fun getProjectsByUserId(userId: String): Result<List<Project>> {
        return try {
            val snapshot = projectsCollection.get().await()
            val allProjects = snapshot.documents.mapNotNull { doc ->
                doc.toObject<FirebaseProjectDto>()?.let { ProjectMapper.toDomain(it) }
            }
            val filtered = allProjects.filter { project ->
                project.authorId == userId || project.applicants.any { it.userId == userId }
            }
            Result.Success(filtered.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки проектов пользователя")
        }
    }

    override suspend fun acceptApplicant(projectId: String, applicantId: String): Result<Unit> {
        return try {
            val document = projectsCollection.document(projectId).get().await()
            val dto = document.toObject<FirebaseProjectDto>()
            if (dto == null) {
                return Result.Error("Проект не найден")
            }

            val applicant = dto.applicants.find { it["userId"] == applicantId }
            if (applicant == null) {
                return Result.Error("Заявка не найдена")
            }

            val role = applicant["role"] ?: return Result.Error("Роль не указана")
            val userName = applicant["userName"] ?: applicantId // берём имя из заявки

            val updatedApplicants = dto.applicants.filter { it["userId"] != applicantId }
            val newMember = mapOf(
                "userId" to applicantId,
                "role" to role,
                "userName" to userName
            )
            val updatedMembers = dto.members + newMember

            projectsCollection.document(projectId).update(
                mapOf(
                    "applicants" to updatedApplicants,
                    "members" to updatedMembers
                )
            ).await()

            val updatedDto = projectsCollection.document(projectId).get().await()
                .toObject<FirebaseProjectDto>()

            if (updatedDto != null) {
                val requiredRoles = updatedDto.requiredRoles
                val memberRoles = updatedMembers.map { it["role"] ?: "" }
                val allRolesFilled = requiredRoles.all { role -> memberRoles.contains(role) }

                if (allRolesFilled && updatedDto.status == ru.lissa_lesia.iteams.domain.models.ProjectStatus.OPEN.name) {
                    projectsCollection.document(projectId)
                        .update("status", ru.lissa_lesia.iteams.domain.models.ProjectStatus.CLOSED.name)
                        .await()
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при принятии заявки")
        }
    }

    override suspend fun rejectApplicant(projectId: String, applicantId: String): Result<Unit> {
        return try {
            val document = projectsCollection.document(projectId).get().await()
            val dto = document.toObject<FirebaseProjectDto>()
            if (dto == null) {
                return Result.Error("Проект не найден")
            }

            val updatedApplicants = dto.applicants.filter { it["userId"] != applicantId }
            projectsCollection.document(projectId).update("applicants", updatedApplicants).await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при отклонении заявки")
        }
    }

    override suspend fun updateProject(project: Project): Result<Unit> {
        return try {
            val dto = ProjectMapper.toDto(project)
            projectsCollection.document(project.id).set(dto).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка обновления проекта")
        }
    }
}