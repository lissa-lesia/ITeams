package ru.lissa_lesia.iteams.data.models

import com.google.firebase.firestore.PropertyName

data class FirebaseUserDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("email") @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("bio") @set:PropertyName("bio")
    var bio: String = "",

    @get:PropertyName("avatarUrl") @set:PropertyName("avatarUrl")
    var avatarUrl: String = "",

    @get:PropertyName("skills") @set:PropertyName("skills")
    var skills: List<String> = emptyList()
)