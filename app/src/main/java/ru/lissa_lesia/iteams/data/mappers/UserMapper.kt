package ru.lissa_lesia.iteams.data.mappers

import ru.lissa_lesia.iteams.data.models.FirebaseUserDto
import ru.lissa_lesia.iteams.domain.models.User

object UserMapper {
    fun toDomain(dto: FirebaseUserDto): User {
        return User(
            id = dto.id,
            name = dto.name,
            email = dto.email,
            bio = dto.bio,
            avatarUrl = dto.avatarUrl,
            skills = dto.skills
        )
    }

    fun toDto(user: User): FirebaseUserDto {
        return FirebaseUserDto(
            id = user.id,
            name = user.name,
            email = user.email,
            bio = user.bio,
            avatarUrl = user.avatarUrl,
            skills = user.skills
        )
    }
}