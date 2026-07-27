package ru.lissa_lesia.iteams.domain.models

data class Member(
    val userId: String,
    val role: String,
    val userName: String = ""
)