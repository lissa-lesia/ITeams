package ru.lissa_lesia.iteams.data.sources

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ru.lissa_lesia.iteams.data.models.FirebaseUserDto

class UserDataSource(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")

    suspend fun saveUser(user: FirebaseUserDto) {
        usersCollection.document(user.id).set(user).await()
    }

    suspend fun getUserById(userId: String): FirebaseUserDto? {
        val document = usersCollection.document(userId).get().await()
        return document.toObject(FirebaseUserDto::class.java)
    }

    suspend fun updateUser(user: FirebaseUserDto) {
        usersCollection.document(user.id).set(user).await()
    }
}