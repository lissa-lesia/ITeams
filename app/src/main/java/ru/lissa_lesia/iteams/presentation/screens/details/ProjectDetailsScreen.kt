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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import ru.lissa_lesia.iteams.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    viewModel: ProjectDetailsViewModel,
    onNavigateBack: () -> Unit,
    onEditProject: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val topBarGradient = Brush.horizontalGradient(
        colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Детали проекта",
                        color = TextOnPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = TextOnPrimary
                        )
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
                .background(BackgroundLight)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ActionPrimary
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
                            color = ErrorText,
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
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
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
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val statusColor = if (isOpen) StatusOpen else StatusClosed
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
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (project.requiredSkills.isNotEmpty()) {
                                    Text(
                                        text = "Навыки:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = project.requiredSkills.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (project.requiredRoles.isNotEmpty()) {
                                    Text(
                                        text = "Требуемые роли:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = project.requiredRoles.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Измененный блок с автором - теперь кликабельный
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Автор: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextLight
                                    )
                                    Text(
                                        text = "${project.authorName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ActionPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable { onUserClick(project.authorId) }
                                    )
                                    Text(
                                        text = " (${project.authorRole})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextLight
                                    )
                                }

                                // Измененный блок с командой - теперь имена участников кликабельны
                                if (project.members.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Команда:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    project.members.forEach { member ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = member.userName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ActionPrimary,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.clickable { onUserClick(member.userId) }
                                            )
                                            Text(
                                                text = " — ${member.role}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                if (project.applicants.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Заявок: ${project.applicants.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Создан: ${formatTimestamp(project.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextLight
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
                                        containerColor = ActionPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Редактировать проект",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextOnPrimary
                                    )
                                }
                            }
                            !isOpen -> {
                                Text(
                                    text = "Набор в проект закрыт",
                                    color = StatusClosed,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                            isApplicant -> {
                                Button(
                                    onClick = { viewModel.withdrawApplication { } },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ActionWarning
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !uiState.isWithdrawing
                                ) {
                                    if (uiState.isWithdrawing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = TextOnPrimary
                                        )
                                    } else {
                                        Text(
                                            "Отозвать заявку",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextOnPrimary
                                        )
                                    }
                                }
                                if (uiState.withdrawError != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.withdrawError!!,
                                        color = ErrorText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            else -> {
                                if (project.requiredRoles.isNotEmpty()) {
                                    Column {
                                        Text(
                                            text = "Выберите вашу роль в проекте:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
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
                                                    onClick = { viewModel.updateSelectedRole(role) },
                                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                                        selectedColor = ActionPrimary,
                                                        unselectedColor = InputBorderUnfocused
                                                    )
                                                )
                                                Text(
                                                    text = role,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                } else {
                                    Text(
                                        text = "Роли не указаны, подача заявки без роли",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { viewModel.applyForProject { } },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ActionPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !uiState.isApplying &&
                                            (project.requiredRoles.isEmpty() || uiState.selectedRole.isNotBlank())
                                ) {
                                    if (uiState.isApplying) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = TextOnPrimary
                                        )
                                    } else {
                                        Text(
                                            "Подать заявку",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextOnPrimary
                                        )
                                    }
                                }

                                if (uiState.applyError != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.applyError!!,
                                        color = ErrorText,
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