package ru.lissa_lesia.iteams.domain.models

data class Project (
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val authorId: String = "",
    val requiredRoles: List<String> = emptyList(),
    val requiredSkills: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val status: ProjectStatus = ProjectStatus.OPEN,
    val applicantsIds: List<String> = emptyList()
)
enum class ProjectStatus {
    OPEN, // набор в проект открыт, команда неполная
    CLOSED //набор в проект закрыт, команда полная
}


