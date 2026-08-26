package ru.lissa_lesia.iteams.domain.usecases

import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.utils.Result

class UpdateCandidateUseCase(
    private val candidateRepository: ICandidateRepository
) {
    suspend operator fun invoke(candidate: Candidate): Result<Unit> {
        return candidateRepository.updateCandidate(candidate)
    }
}