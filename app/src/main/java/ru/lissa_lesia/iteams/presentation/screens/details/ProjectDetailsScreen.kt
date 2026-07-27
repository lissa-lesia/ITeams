package ru.lissa_lesia.iteams.presentation.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    viewModel: ProjectDetailsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val topBarGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Детали проекта",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .background(brush = topBarGradient)
                    .shadow(4.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
        ) {
            when {
                uiState.isLoading -> {
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
                    }
                }
                uiState.project != null -> {
                    val project = uiState.project!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // Заголовок и статус
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = project.title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A237E),
                                        modifier = Modifier.weight(1f)
                                    )
                                    val statusColor = if (project.status == ProjectStatus.OPEN) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(statusColor)
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text(
                                            text = if (project.status == ProjectStatus.OPEN) "Открыт" else "Закрыт",
                                            color = statusColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Описание
                                Text(
                                    text = project.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF37474F)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Навыки
                                if (project.requiredSkills.isNotEmpty()) {
                                    Text(
                                        text = "Навыки:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1A237E)
                                    )
                                    Text(
                                        text = project.requiredSkills.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF546E7A)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Роли
                                if (project.requiredRoles.isNotEmpty()) {
                                    Text(
                                        text = "Роли:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1A237E)
                                    )
                                    Text(
                                        text = project.requiredRoles.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF546E7A)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Автор и дата
                                Text(
                                    text = "Автор: ${project.authorName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF78909C)
                                )
                                Text(
                                    text = "Создан: ${formatTimestamp(project.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF78909C)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Дополнительные кнопки (например, подать заявку)
                        Button(
                            onClick = { /* TODO: подать заявку */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2575FC)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Подать заявку", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}