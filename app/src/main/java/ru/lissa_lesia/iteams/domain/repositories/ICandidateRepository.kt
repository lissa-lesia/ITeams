package ru.lissa_lesia.iteams.domain.repositories

import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.utils.Result

interface ICandidateRepository {

    suspend fun createCandidate(candidate: Candidate): Result<String>

    suspend fun updateCandidate(candidate: Candidate): Result<Unit>

    suspend fun getCandidateById(candidateId: String): Result<Candidate>

    suspend fun getCandidates(): Result<List<Candidate>>

    suspend fun getCandidatesByUserId(userId: String): Result<List<Candidate>>

    suspend fun deleteCandidate(candidateId: String): Result<Unit>
}