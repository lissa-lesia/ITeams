package ru.lissa_lesia.iteams.presentation.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateBack: () -> Unit,
    onEditProject: (String) -> Unit
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
                            textAlign = TextAlign.Center
                        )
                    }
                }
                uiState.project != null -> {
                    val project = uiState.project!!
                    val isAuthor = uiState.isAuthor
                    val isApplicant = uiState.isApplicant
                    val isOpen = project.status == ProjectStatus.OPEN

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
                                    val statusColor = if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
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
                                            text = if (isOpen) "Открыт" else "Закрыт",
                                            color = statusColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = project.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF37474F)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

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

                                if (project.requiredRoles.isNotEmpty()) {
                                    Text(
                                        text = "Требуемые роли:",
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

                                Text(
                                    text = "Автор: ${project.authorName} (${project.authorRole})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF78909C)
                                )

                                if (project.members.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Команда:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1A237E)
                                    )
                                    project.members.forEach { member ->
                                        Text(
                                            text = "${member.userName} — ${member.role}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF546E7A)
                                        )
                                    }
                                }

                                if (project.applicants.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Заявок: ${project.applicants.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF78909C)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Создан: ${formatTimestamp(project.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF78909C)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when {
                            isAuthor -> {
                                Button(
                                    onClick = { onEditProject(project.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2575FC)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Редактировать проект", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            !isOpen -> {
                                Text(
                                    text = "Набор в проект закрыт",
                                    color = Color(0xFFF44336),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                            isApplicant -> {
                                Button(
                                    onClick = { /* TODO: отозвать заявку */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = false
                                ) {
                                    Text("Заявка подана", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {
                                if (project.requiredRoles.isNotEmpty()) {
                                    Column {
                                        Text(
                                            text = "Выберите вашу роль в проекте:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1A237E)
                                        )
                                        project.requiredRoles.forEach { role ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { viewModel.updateSelectedRole(role) }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = uiState.selectedRole == role,
                                                    onClick = { viewModel.updateSelectedRole(role) }
                                                )
                                                Text(
                                                    text = role,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFF37474F)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                } else {
                                    Text(
                                        text = "Роли не указаны, подача заявки без роли",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF78909C)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { viewModel.applyForProject { } },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2575FC)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !uiState.isApplying &&
                                            (project.requiredRoles.isEmpty() || uiState.selectedRole.isNotBlank())
                                ) {
                                    if (uiState.isApplying) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White
                                        )
                                    } else {
                                        Text("Подать заявку", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (uiState.applyError != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.applyError!!,
                                        color = Color.Red,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
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