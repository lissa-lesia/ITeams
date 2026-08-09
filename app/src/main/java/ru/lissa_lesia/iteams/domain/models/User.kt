package ru.lissa_lesia.iteams.domain.models

data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val resumeUrl: String? = null
)