package ru.lissa_lesia.iteams.data.mappers

import ru.lissa_lesia.iteams.data.models.FirebaseProjectDto
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
            requiredRoles = dto.requiredRoles,
            requiredSkills = dto.requiredSkills,
            createdAt = dto.createdAt,
            status = try {
                ProjectStatus.valueOf(dto.status)
            } catch (e: Exception) {
                ProjectStatus.OPEN
            },
            applicantsIds = dto.applicantsIds
        )
    }

    fun toDto(project: Project): FirebaseProjectDto {
        return FirebaseProjectDto(
            id = project.id,
            title = project.title,
            description = project.description,
            authorId = project.authorId,
            authorName = project.authorName,
            requiredRoles = project.requiredRoles,
            requiredSkills = project.requiredSkills,
            createdAt = project.createdAt,
            status = project.status.name,
            applicantsIds = project.applicantsIds
        )
    }
}