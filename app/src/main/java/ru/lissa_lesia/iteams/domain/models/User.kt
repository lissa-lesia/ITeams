package ru.lissa_lesia.iteams.domain.models

data class User (
    val id: String = "", //тип не Int потому что будут проблемы с firestore
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val skills: List<String> = emptyList()
)