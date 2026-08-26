package ru.lissa_lesia.iteams.data.sources

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ru.lissa_lesia.iteams.data.models.FirebaseCandidateDto

class CandidateDataSource(private val firestore: FirebaseFirestore) {

    private val candidatesCollection = firestore.collection("candidates")

    suspend fun createCandidate(candidate: FirebaseCandidateDto) {
        candidatesCollection.document(candidate.id).set(candidate).await()
    }

    suspend fun updateCandidate(candidate: FirebaseCandidateDto) {
        candidatesCollection.document(candidate.id).set(candidate).await()
    }

    suspend fun getCandidateById(candidateId: String): FirebaseCandidateDto? {
        val document = candidatesCollection.document(candidateId).get().await()
        return document.toObject(FirebaseCandidateDto::class.java)
    }

    suspend fun getCandidatesByStatus(status: String): List<FirebaseCandidateDto> {
        val snapshot = candidatesCollection
            .whereEqualTo("status", status)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(FirebaseCandidateDto::class.java) }
    }

    suspend fun getCandidatesByUserId(userId: String): List<FirebaseCandidateDto> {
        val snapshot = candidatesCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(FirebaseCandidateDto::class.java) }
    }

    suspend fun deleteCandidate(candidateId: String) {
        candidatesCollection.document(candidateId).delete().await()
    }
}