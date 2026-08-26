package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class GetCandidateByIdUseCase(
    private val candidateRepository: ICandidateRepository
) {
    suspend operator fun invoke(candidateId: String): Result<Candidate> {
        return candidateRepository.getCandidateById(candidateId)
    }
}