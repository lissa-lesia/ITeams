package ru.lissa_lesia.iteams.data.models

import com.google.firebase.firestore.PropertyName
import ru.lissa_lesia.iteams.domain.models.ProjectStatus

//это копия Project, но с аннотациями для Firestore, чтобы Firebase умел сохранять поля

data class FirebaseProjectDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("authorId") @set:PropertyName("authorId")
    var authorId: String = "",

    @get:PropertyName("requiredRoles") @set:PropertyName("requiredRoles")
    var requiredRoles: List<String> = emptyList(),

    @get:PropertyName("requiredSkills") @set:PropertyName("requiredSkills")
    var requiredSkills: List<String> = emptyList(),

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = ProjectStatus.OPEN.name, // Firebase хранит строки, а не enum напрямую

    @get:PropertyName("applicantsIds") @set:PropertyName("applicantsIds")
    var applicantsIds: List<String> = emptyList()
)