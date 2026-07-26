package ru.lissa_lesia.iteams.presentation.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Лента проектов") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateProject) {
                Icon(Icons.Default.Add, contentDescription = "Создать проект")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ошибка: ${uiState.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadProjects() }) {
                            Text("Повторить")
                        }
                    }
                }
                uiState.projects.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Пока нет проектов")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateToCreateProject) {
                            Text("Создать первый проект")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.projects) { project ->
                            ProjectCard(project = project)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCard(project: Project) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO: переход на детали проекта */ }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Статус: ${if (project.status == ProjectStatus.OPEN) "Открыт" else "Закрыт"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Навыки: ${project.requiredSkills.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Роли: ${project.requiredRoles.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Создан: ${formatTimestamp(project.createdAt)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}