package ru.lissa_lesia.iteams.data.repositories

import ru.lissa_lesia.iteams.data.mappers.CandidateMapper
import ru.lissa_lesia.iteams.data.sources.CandidateDataSource
import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.models.CandidateStatus
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import java.util.UUID

class CandidateRepositoryImpl(
    private val candidateDataSource: CandidateDataSource
) : ICandidateRepository {

    override suspend fun createCandidate(candidate: Candidate): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val dto = CandidateMapper.toDto(candidate).apply { this.id = id }
            candidateDataSource.createCandidate(dto)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка создания заявки кандидата")
        }
    }

    override suspend fun updateCandidate(candidate: Candidate): Result<Unit> {
        return try {
            val dto = CandidateMapper.toDto(candidate)
            candidateDataSource.updateCandidate(dto)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка обновления заявки кандидата")
        }
    }

    override suspend fun getCandidateById(candidateId: String): Result<Candidate> {
        return try {
            val dto = candidateDataSource.getCandidateById(candidateId)
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
            val dtos = candidateDataSource.getCandidatesByStatus(CandidateStatus.ACTIVE.name)
            val candidates = dtos.map { CandidateMapper.toDomain(it) }
                .sortedByDescending { it.createdAt }
            Result.Success(candidates)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки кандидатов")
        }
    }

    override suspend fun getCandidatesByUserId(userId: String): Result<List<Candidate>> {
        return try {
            val dtos = candidateDataSource.getCandidatesByUserId(userId)
            val candidates = dtos.map { CandidateMapper.toDomain(it) }
                .sortedByDescending { it.createdAt }
            Result.Success(candidates)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки заявок пользователя")
        }
    }

    override suspend fun deleteCandidate(candidateId: String): Result<Unit> {
        return try {
            candidateDataSource.deleteCandidate(candidateId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка удаления заявки")
        }
    }
}