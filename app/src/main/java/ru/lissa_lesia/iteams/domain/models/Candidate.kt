package ru.lissa_lesia.iteams.domain.models

enum class CandidateStatus {
    ACTIVE,   // активная заявка
    CLOSED    // закрыта (например, уже нашёл проект)
}

data class Candidate(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val desiredRoles: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val description: String = "",
    val status: CandidateStatus = CandidateStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
)