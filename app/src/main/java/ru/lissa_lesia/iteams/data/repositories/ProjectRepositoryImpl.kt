package ru.lissa_lesia.iteams.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import ru.lissa_lesia.iteams.data.mappers.ProjectMapper
import ru.lissa_lesia.iteams.data.models.FirebaseProjectDto
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import kotlinx.coroutines.tasks.await

class ProjectRepositoryImpl ( private val firestore: FirebaseFirestore ) : IProjectRepository {

    private val projectsCollection = firestore.collection("projects") // имя коллекции в Firebase

    override suspend fun createProject(project: Project): Result<String> {
        return try {
            val dto = ProjectMapper.toDto(project)

            // сохраняем в Firestore; await() приостанавливает корутину, пока не придёт ответ
            val documentRef = projectsCollection.add(dto).await()

            documentRef.update("id", documentRef.id).await()
            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка при создании проекта")
        }
    }

    override suspend fun getFeedProjects(): Result<List<Project>> {
        return try {
            val snapshot = projectsCollection.get().await()

            // превращаем каждый документ в DTO, потом в Domain модель
            val projects = snapshot.documents.mapNotNull { document ->
                val dto = document.toObject<FirebaseProjectDto>()
                dto?.let { ProjectMapper.toDomain(it) }
            }

            // сортируем по дате (новые сверху)
            Result.Success(projects.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки ленты проектов")
        }
    }
}