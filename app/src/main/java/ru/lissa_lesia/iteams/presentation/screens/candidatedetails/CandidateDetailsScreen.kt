package ru.lissa_lesia.iteams.presentation.screens.candidatedetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf
import ru.lissa_lesia.iteams.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateDetailsScreen(
    candidateId: String,
    onNavigateBack: () -> Unit,
    onEditCandidate: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val viewModel: CandidateDetailsViewModel = getViewModel(
        parameters = { parametersOf(candidateId) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showInviteDialog by remember { mutableStateOf(false) }

    // Сбрасываем состояние успеха при закрытии диалога
    LaunchedEffect(showInviteDialog) {
        if (!showInviteDialog) {
            viewModel.clearInviteSuccess()
        }
    }

    val topBarGradient = Brush.horizontalGradient(
        colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Кандидат",
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActionPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Повторить", color = TextOnPrimary)
                        }
                    }
                }
                uiState.candidate != null -> {
                    val candidate = uiState.candidate!!
                    val isOwner = uiState.isOwner

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
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
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Аватар",
                                        tint = TextOnPrimary,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = candidate.userName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.clickable {
                                        if (candidate.userId.isNotBlank()) {
                                            onUserClick(candidate.userId)
                                        }
                                    }
                                )

                                if (candidate.desiredRoles.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Желаемые роли: ${candidate.desiredRoles.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }

                                if (candidate.skills.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Навыки: ${candidate.skills.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }

                                if (candidate.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "О себе:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = candidate.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Статус: ${if (candidate.status.name == "ACTIVE") "Активен" else "Закрыт"}",
                                    color = if (candidate.status.name == "ACTIVE") StatusOpen else StatusClosed,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Создан: ${formatTimestamp(candidate.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextVeryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Показываем кнопки в зависимости от владельца
                        if (isOwner) {
                            // Владелец видит кнопку редактирования
                            Button(
                                onClick = onEditCandidate,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ActionPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Редактировать заявку",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnPrimary
                                )
                            }
                        } else {
                            // Другие пользователи видят кнопку приглашения (если заявка активна)
                            if (candidate.status.name == "ACTIVE") {
                                // Проверяем, есть ли у пользователя проекты для приглашения
                                if (uiState.userProjects.isEmpty()) {
                                    Text(
                                        text = "У вас нет открытых проектов для приглашения",
                                        color = TextMuted,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Button(
                                        onClick = { showInviteDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ActionSuccess
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = !uiState.isInviting
                                    ) {
                                        if (uiState.isInviting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = TextOnPrimary
                                            )
                                        } else {
                                            Text(
                                                "Пригласить в проект",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextOnPrimary
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Заявка закрыта",
                                    color = StatusClosed,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Показываем ошибку приглашения если есть
                        if (uiState.inviteError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.inviteError!!,
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

    // Диалог приглашения
    if (showInviteDialog && uiState.candidate != null) {
        InviteCandidateDialog(
            candidate = uiState.candidate!!,
            userProjects = uiState.userProjects,
            isInviting = uiState.isInviting,
            onInvite = { projectId, role ->
                viewModel.inviteCandidate(projectId, role) { success ->
                    if (success) {
                        showInviteDialog = false
                    }
                }
            },
            onDismiss = {
                showInviteDialog = false
                viewModel.clearInviteError()
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}