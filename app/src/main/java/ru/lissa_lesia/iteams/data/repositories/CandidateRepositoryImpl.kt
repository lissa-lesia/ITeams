package ru.lissa_lesia.iteams.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import ru.lissa_lesia.iteams.data.mappers.CandidateMapper
import ru.lissa_lesia.iteams.data.models.FirebaseCandidateDto
import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.models.CandidateStatus
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import java.util.UUID

class CandidateRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ICandidateRepository {

    private val candidatesCollection = firestore.collection("candidates")

    override suspend fun createCandidate(candidate: Candidate): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val dto = CandidateMapper.toDto(candidate).apply { this.id = id }
            candidatesCollection.document(id).set(dto).await()
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка создания заявки кандидата")
        }
    }

    override suspend fun updateCandidate(candidate: Candidate): Result<Unit> {
        return try {
            val dto = CandidateMapper.toDto(candidate)
            candidatesCollection.document(candidate.id).set(dto).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка обновления заявки кандидата")
        }
    }

    override suspend fun getCandidateById(candidateId: String): Result<Candidate> {
        return try {
            val document = candidatesCollection.document(candidateId).get().await()
            val dto = document.toObject<FirebaseCandidateDto>()
            if (dto != null) {
                Result.Success(CandidateMapper.toDomain(dto))
            } else {
                Result.Error("Заявка кандидата не найдена")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки заявки кандидата")
        }
    }

    override suspend fun getCandidates(): Result<List<Candidate>> {
        return try {
            // Возвращаем только активные заявки (можно изменить)
            val snapshot = candidatesCollection
                .whereEqualTo("status", CandidateStatus.ACTIVE.name)
                .get()
                .await()
            val candidates = snapshot.documents.mapNotNull { doc ->
                doc.toObject<FirebaseCandidateDto>()?.let { CandidateMapper.toDomain(it) }
            }
            Result.Success(candidates.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки кандидатов")
        }
    }

    override suspend fun getCandidatesByUserId(userId: String): Result<List<Candidate>> {
        return try {
            val snapshot = candidatesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val candidates = snapshot.documents.mapNotNull { doc ->
                doc.toObject<FirebaseCandidateDto>()?.let { CandidateMapper.toDomain(it) }
            }
            Result.Success(candidates.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки заявок пользователя")
        }
    }

    override suspend fun deleteCandidate(candidateId: String): Result<Unit> {
        return try {
            // Просто удаляем документ, или можно поменять статус на CLOSED
            candidatesCollection.document(candidateId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка удаления заявки")
        }
    }
}