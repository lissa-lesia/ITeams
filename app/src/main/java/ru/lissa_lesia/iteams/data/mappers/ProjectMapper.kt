package ru.lissa_lesia.iteams.data.mappers

import ru.lissa_lesia.iteams.data.models.FirebaseProjectDto
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.models.ProjectStatus

//этот объект превращает Firebase-модель в чистую доменную модель и обратно

object ProjectMapper {

    // Из Firebase -> в Domain (для показа на экранах)
    fun toDomain(dto: FirebaseProjectDto): Project {
        return Project(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            authorId = dto.authorId,
            requiredRoles = dto.requiredRoles,
            requiredSkills = dto.requiredSkills,
            createdAt = dto.createdAt,
            status = try {
                ProjectStatus.valueOf(dto.status) // Превращаем строку обратно в enum
            } catch (e: Exception) {
                ProjectStatus.OPEN
            },
            applicantsIds = dto.applicantsIds
        )
    }

    // Из Domain -> в Firebase (для сохранения)
    fun toDto(project: Project): FirebaseProjectDto {
        return FirebaseProjectDto(
            id = project.id,
            title = project.title,
            description = project.description,
            authorId = project.authorId,
            requiredRoles = project.requiredRoles,
            requiredSkills = project.requiredSkills,
            createdAt = project.createdAt,
            status = project.status.name, // Превращаем enum обратно в строку
            applicantsIds = project.applicantsIds
        )
    }
}