package ru.lissa_lesia.iteams.domain.repositories
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.utils.Result

// ViewModel будет вызывать эти функции, и ей будет всё равно, откуда пришли данные

interface IProjectRepository {
        suspend fun createProject(project: Project): Result<String> // Возвращаем ID созданного проекта
        suspend fun getFeedProjects(): Result<List<Project>>


}