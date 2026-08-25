package ru.lissa_lesia.iteams.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import ru.lissa_lesia.iteams.data.mappers.ProjectMapper
import ru.lissa_lesia.iteams.data.models.FirebaseProjectDto
import ru.lissa_lesia.iteams.domain.models.Invitation
import ru.lissa_lesia.iteams.domain.models.InvitationStatus
import ru.lissa_lesia.iteams.domain.models.Member
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import java.util.UUID

class ProjectRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: IAuthRepository,
    private val candidateRepository: ICandidateRepository
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
            if (dto.status != ProjectStatus.OPEN.name) {
                return Result.Error("Набор в проект закрыт")
            }
            if (dto.applicants.any { it["userId"] == userId }) {
                return Result.Error("Вы уже подали заявку")
            }
            // Получаем имя пользователя
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
                project.authorId == userId ||
                        project.applicants.any { it.userId == userId } ||
                        project.members.any { it.userId == userId }
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
            val userName = applicant["userName"] ?: applicantId

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

                if (allRolesFilled && updatedDto.status == ProjectStatus.OPEN.name) {
                    projectsCollection.document(projectId)
                        .update("status", ProjectStatus.CLOSED.name)
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

    override suspend fun withdrawApplication(projectId: String, userId: String): Result<Unit> {
        return try {
            val document = projectsCollection.document(projectId).get().await()
            val dto = document.toObject<FirebaseProjectDto>()
            if (dto == null) {
                return Result.Error("Проект не найден")
            }
            if (dto.applicants.none { it["userId"] == userId }) {
                return Result.Error("Заявка не найдена")
            }
            if (dto.members.any { it["userId"] == userId }) {
                return Result.Error("Вы уже приняты в команду, отозвать заявку нельзя")
            }
            val updatedApplicants = dto.applicants.filter { it["userId"] != userId }
            projectsCollection.document(projectId).update("applicants", updatedApplicants).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при отзыве заявки")
        }
    }

    override suspend fun inviteCandidate(
        projectId: String,
        candidateId: String,
        userId: String,
        role: String
    ): Result<Unit> {
        return try {
            val document = projectsCollection.document(projectId).get().await()
            val dto = document.toObject<FirebaseProjectDto>()
            if (dto == null) {
                return Result.Error("Проект не найден")
            }

            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                return Result.Error("Пользователь не авторизован")
            }
            if (dto.authorId != currentUser.id) {
                return Result.Error("Только автор проекта может приглашать кандидатов")
            }

            if (dto.status != ProjectStatus.OPEN.name) {
                return Result.Error("Набор в проект закрыт")
            }

            if (dto.invitations.any {
                    it["candidateId"] == candidateId && it["status"] == "PENDING"
                }) {
                return Result.Error("Этот кандидат уже приглашен")
            }

            if (dto.members.any { it["userId"] == userId }) {
                return Result.Error("Пользователь уже в команде")
            }

            if (dto.applicants.any { it["userId"] == userId }) {
                return Result.Error("Пользователь уже подал заявку")
            }

            val candidateResult = candidateRepository.getCandidateById(candidateId)
            val candidateName = if (candidateResult is Result.Success) {
                candidateResult.data.userName
            } else {
                userId
            }

            val invitation = mapOf(
                "candidateId" to candidateId,
                "userId" to userId,
                "userName" to candidateName,
                "role" to role,
                "status" to "PENDING",
                "invitedAt" to System.currentTimeMillis(),
                "invitedBy" to currentUser.id,
                "invitedByName" to currentUser.name
            )

            val newInvitations = dto.invitations + invitation
            projectsCollection.document(projectId).update("invitations", newInvitations).await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при приглашении кандидата")
        }
    }

    override suspend fun respondToInvitation(
        projectId: String,
        candidateId: String,
        accept: Boolean
    ): Result<Unit> {
        return try {
            val document = projectsCollection.document(projectId).get().await()
            val dto = document.toObject<FirebaseProjectDto>()
            if (dto == null) {
                return Result.Error("Проект не найден")
            }

            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                return Result.Error("Пользователь не авторизован")
            }

            // Находим приглашение
            val invitation = dto.invitations.find {
                it["candidateId"] == candidateId && it["userId"] == currentUser.id
            }
            if (invitation == null) {
                return Result.Error("Приглашение не найдено")
            }

            if (invitation["status"] != "PENDING") {
                return Result.Error("Приглашение уже обработано")
            }

            val updatedInvitations = dto.invitations.map { inv ->
                if (inv["candidateId"] == candidateId && inv["userId"] == currentUser.id) {
                    inv + ("status" to if (accept) "ACCEPTED" else "REJECTED")
                } else {
                    inv
                }
            }

            if (accept) {
                val role = invitation["role"] as? String ?: ""
                val userName = currentUser.name

                val newMember = mapOf(
                    "userId" to currentUser.id,
                    "role" to role,
                    "userName" to userName
                )
                val updatedMembers = dto.members + newMember

                projectsCollection.document(projectId).update(
                    mapOf(
                        "invitations" to updatedInvitations,
                        "members" to updatedMembers
                    )
                ).await()

                val updatedDto = projectsCollection.document(projectId).get().await()
                    .toObject<FirebaseProjectDto>()

                if (updatedDto != null) {
                    val requiredRoles = updatedDto.requiredRoles
                    val memberRoles = updatedMembers.map { it["role"] ?: "" }
                    val allRolesFilled = requiredRoles.all { role -> memberRoles.contains(role) }

                    if (allRolesFilled && updatedDto.status == ProjectStatus.OPEN.name) {
                        projectsCollection.document(projectId)
                            .update("status", ProjectStatus.CLOSED.name)
                            .await()
                    }
                }
            } else {
                projectsCollection.document(projectId).update("invitations", updatedInvitations).await()
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при обработке приглашения")
        }
    }

    override suspend fun getInvitationsForUser(userId: String): Result<List<Project>> {
        return try {
            val snapshot = projectsCollection.get().await()
            val allProjects = snapshot.documents.mapNotNull { doc ->
                doc.toObject<FirebaseProjectDto>()?.let { ProjectMapper.toDomain(it) }
            }

            val projectsWithInvitations = allProjects.filter { project ->
                project.invitations.any {
                    it.userId == userId && it.status == InvitationStatus.PENDING
                }
            }

            Result.Success(projectsWithInvitations.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки приглашений")
        }
    }
}