package ru.lissa_lesia.iteams.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import ru.lissa_lesia.iteams.data.mappers.ProjectMapper
import ru.lissa_lesia.iteams.data.models.FirebaseProjectDto
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.utils.Result
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProjectRepositoryImpl(private val firestore: FirebaseFirestore) : IProjectRepository {

    private val projectsCollection = firestore.collection("projects")

    override suspend fun createProject(project: Project): Result<String> {
        return try {
            // Генерируем уникальный ID
            val projectId = UUID.randomUUID().toString()
            // Преобразуем в DTO и устанавливаем этот ID
            val dto = ProjectMapper.toDto(project).apply { id = projectId }
            // Сохраняем документ с заданным ID
            projectsCollection.document(projectId).set(dto).await()
            Result.Success(projectId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка при создании проекта")
        }
    }

    override suspend fun getFeedProjects(): Result<List<Project>> {
        return try {
            val snapshot = projectsCollection.get().await()
            val projects = snapshot.documents.mapNotNull { document ->
                val dto = document.toObject<FirebaseProjectDto>()
                dto?.let { ProjectMapper.toDomain(it) }
            }
            Result.Success(projects.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки ленты проектов")
        }
    }

    override suspend fun getProjectById(projectId: String): Result<Project> {
        return try {
            val document = projectsCollection.document(projectId).get().await()
            val dto = document.toObject<FirebaseProjectDto>()
            if (dto != null) {
                Result.Success(ProjectMapper.toDomain(dto))
            } else {
                Result.Error("Проект не найден")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка загрузки проекта")
        }
    }
}