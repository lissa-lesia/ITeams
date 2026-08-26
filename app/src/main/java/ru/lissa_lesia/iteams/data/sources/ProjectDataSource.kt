package ru.lissa_lesia.iteams.data.sources

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import ru.lissa_lesia.iteams.data.models.FirebaseProjectDto

class ProjectDataSource(private val firestore: FirebaseFirestore) {

    private val projectsCollection = firestore.collection("projects")

    suspend fun createProject(project: FirebaseProjectDto) {
        projectsCollection.document(project.id).set(project).await()
    }

    suspend fun getAllProjects(): List<FirebaseProjectDto> {
        val snapshot = projectsCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(FirebaseProjectDto::class.java) }
    }

    suspend fun getProjectById(projectId: String): FirebaseProjectDto? {
        val document = projectsCollection.document(projectId).get().await()
        return document.toObject(FirebaseProjectDto::class.java)
    }

    suspend fun updateProject(project: FirebaseProjectDto) {
        projectsCollection.document(project.id).set(project).await()
    }

    suspend fun updateProjectFields(projectId: String, fields: Map<String, Any>) {
        projectsCollection.document(projectId).update(fields).await()
    }
}