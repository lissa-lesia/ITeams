package ru.lissa_lesia.iteams.data.mappers

import ru.lissa_lesia.iteams.data.models.FirebaseCandidateDto
import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.models.CandidateStatus

object CandidateMapper {

    fun toDomain(dto: FirebaseCandidateDto): Candidate {
        return Candidate(
            id = dto.id,
            userId = dto.userId,
            userName = dto.userName,
            userEmail = dto.userEmail,
            desiredRoles = dto.desiredRoles,
            skills = dto.skills,
            description = dto.description,
            status = try {
                CandidateStatus.valueOf(dto.status)
            } catch (e: Exception) {
                CandidateStatus.ACTIVE
            },
            createdAt = dto.createdAt
        )
    }

    fun toDto(candidate: Candidate): FirebaseCandidateDto {
        return FirebaseCandidateDto(
            id = candidate.id,
            userId = candidate.userId,
            userName = candidate.userName,
            userEmail = candidate.userEmail,
            desiredRoles = candidate.desiredRoles,
            skills = candidate.skills,
            description = candidate.description,
            status = candidate.status.name,
            createdAt = candidate.createdAt
        )
    }
}