package ru.lissa_lesia.iteams.data.models

import com.google.firebase.firestore.PropertyName
import ru.lissa_lesia.iteams.domain.models.CandidateStatus

data class FirebaseCandidateDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("userName") @set:PropertyName("userName")
    var userName: String = "",

    @get:PropertyName("userEmail") @set:PropertyName("userEmail")
    var userEmail: String = "",

    @get:PropertyName("desiredRoles") @set:PropertyName("desiredRoles")
    var desiredRoles: List<String> = emptyList(),

    @get:PropertyName("skills") @set:PropertyName("skills")
    var skills: List<String> = emptyList(),

    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = CandidateStatus.ACTIVE.name,

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis()
)