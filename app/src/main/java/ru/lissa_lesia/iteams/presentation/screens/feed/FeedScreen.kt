package ru.lissa_lesia.iteams.presentation.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onLogout: () -> Unit,
    onProjectClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Градиент для верхней панели
    val topBarGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
    )

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Лента проектов",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .background(brush = topBarGradient)
                    .shadow(4.dp),
                actions = {
                    // Кнопка обновления (через верхнюю панель)
                    IconButton(
                        onClick = { viewModel.loadProjects() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Профиль",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateProject,
                containerColor = Color(0xFF2575FC),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать проект")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.projects.isEmpty() -> {
                    // Показываем индикатор только при первой загрузке
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2575FC)
                    )
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ошибка: ${uiState.errorMessage}",
                            color = Color.Red,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.loadProjects() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2575FC)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Повторить", color = Color.White)
                        }
                    }
                }
                uiState.projects.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Пока нет проектов",
                            fontSize = 20.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToCreateProject,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2575FC)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Создать первый проект", color = Color.White)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.projects) { project ->
                            ProjectCard(
                                project = project,
                                onProjectClick = { onProjectClick(project.id) }
                            )
                        }
                    }
                }
            }

            // Показываем индикатор обновления поверх списка, если идёт обновление
            if (uiState.isLoading && uiState.projects.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = Color(0xFF2575FC),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onProjectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onProjectClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с индикатором статуса
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Индикатор статуса (цветной кружок + текст)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    val statusColor = if (project.status == ProjectStatus.OPEN) Color(0xFF4CAF50) else Color(0xFFF44336)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (project.status == ProjectStatus.OPEN) "Открыт" else "Закрыт",
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Описание
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF546E7A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Автор: ${project.authorName}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF78909C)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Навыки и роли (если есть)
            if (project.requiredSkills.isNotEmpty() || project.requiredRoles.isNotEmpty()) {
                Column {
                    if (project.requiredSkills.isNotEmpty()) {
                        Text(
                            text = "Навыки: ${project.requiredSkills.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF455A64)
                        )
                    }
                    if (project.requiredRoles.isNotEmpty()) {
                        Text(
                            text = "Роли: ${project.requiredRoles.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF455A64)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Дата создания
            Text(
                text = "Создан: ${formatTimestamp(project.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF90A4AE)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}