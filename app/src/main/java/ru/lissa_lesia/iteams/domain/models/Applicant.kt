package ru.lissa_lesia.iteams.domain.models

data class Applicant(
    val userId: String,
    val role: String,
    val userName: String = ""
)