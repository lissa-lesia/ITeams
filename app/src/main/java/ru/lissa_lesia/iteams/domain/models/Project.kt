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
    val members: List<Member> = emptyList()
)
enum class ProjectStatus {
    OPEN,
    CLOSED
}


