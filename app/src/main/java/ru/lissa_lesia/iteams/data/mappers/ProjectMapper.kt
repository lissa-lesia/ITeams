package ru.lissa_lesia.iteams.data.mappers

import ru.lissa_lesia.iteams.data.models.FirebaseProjectDto
import ru.lissa_lesia.iteams.domain.models.Applicant
import ru.lissa_lesia.iteams.domain.models.Member
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.models.ProjectStatus

object ProjectMapper {

    fun toDomain(dto: FirebaseProjectDto): Project {
        return Project(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            authorId = dto.authorId,
            authorName = dto.authorName,
            authorRole = dto.authorRole,
            requiredRoles = dto.requiredRoles,
            requiredSkills = dto.requiredSkills,
            createdAt = dto.createdAt,
            status = try {
                ProjectStatus.valueOf(dto.status)
            } catch (e: Exception) {
                ProjectStatus.OPEN
            },
            applicants = dto.applicants.map { map ->
                Applicant(
                    userId = map["userId"] ?: "",
                    role = map["role"] ?: "",
                    userName = map["userName"] ?: map["userId"] ?: ""
                )
            },
            members = dto.members.map { map ->
                Member(
                    userId = map["userId"] ?: "",
                    role = map["role"] ?: "",
                    userName = map["userName"] ?: map["userId"] ?: ""
                )
            }
        )
    }

    fun toDto(project: Project): FirebaseProjectDto {
        return FirebaseProjectDto(
            id = project.id,
            title = project.title,
            description = project.description,
            authorId = project.authorId,
            authorName = project.authorName,
            authorRole = project.authorRole,
            requiredRoles = project.requiredRoles,
            requiredSkills = project.requiredSkills,
            createdAt = project.createdAt,
            status = project.status.name,
            applicants = project.applicants.map { applicant ->
                mapOf(
                    "userId" to applicant.userId,
                    "role" to applicant.role,
                    "userName" to applicant.userName
                )
            },
            members = project.members.map { member ->
                mapOf(
                    "userId" to member.userId,
                    "role" to member.role,
                    "userName" to member.userName
                )
            }
        )
    }
}