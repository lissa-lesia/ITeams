package ru.lissa_lesia.iteams.domain.models

data class Project(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val requiredRoles: List<String> = emptyList(),
    val requiredSkills: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val status: ProjectStatus = ProjectStatus.OPEN,
    val applicants: List<Applicant> = emptyList(),
    val members: List<Member> = emptyList(),
    val invitations: List<Invitation> = emptyList()
)
enum class ProjectStatus {
    OPEN,
    CLOSED
}

data class Invitation(
    val candidateId: String,
    val userId: String,
    val userName: String,
    val role: String,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val invitedAt: Long = System.currentTimeMillis(),
    val invitedBy: String,
    val invitedByName: String
)

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}


