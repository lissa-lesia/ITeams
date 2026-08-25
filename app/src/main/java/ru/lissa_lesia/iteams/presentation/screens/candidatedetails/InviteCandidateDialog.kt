package ru.lissa_lesia.iteams.presentation.screens.candidatedetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.lissa_lesia.iteams.domain.models.Candidate
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.ui.theme.ActionPrimary
import ru.lissa_lesia.iteams.ui.theme.CardBackground
import ru.lissa_lesia.iteams.ui.theme.TextMuted
import ru.lissa_lesia.iteams.ui.theme.TextOnPrimary
import ru.lissa_lesia.iteams.ui.theme.TextPrimary
import ru.lissa_lesia.iteams.ui.theme.TextSecondary

@Composable
fun InviteCandidateDialog(
    candidate: Candidate,
    userProjects: List<Project>,
    isInviting: Boolean,
    onInvite: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Пригласить кандидата",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Выберите проект для приглашения ${candidate.userName}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Список проектов
                userProjects.forEach { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedProjectId = project.id
                                // Автоматически выбираем первую роль
                                selectedRole = project.requiredRoles.firstOrNull() ?: ""
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedProjectId == project.id)
                                ActionPrimary.copy(alpha = 0.1f)
                            else
                                CardBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = project.title,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (project.requiredRoles.isNotEmpty()) {
                                Text(
                                    text = "Роли: ${project.requiredRoles.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "Участников: ${project.members.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                if (selectedProjectId != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Выбор роли
                    val project = userProjects.find { it.id == selectedProjectId }
                    if (project != null && project.requiredRoles.isNotEmpty()) {
                        Text(
                            text = "Выберите роль для кандидата:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        project.requiredRoles.forEach { role ->
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRole = role }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRole == role,
                                    onClick = { selectedRole = role },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = ActionPrimary
                                    )
                                )
                                Text(
                                    text = role,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else if (project != null && project.requiredRoles.isEmpty()) {
                        Text(
                            text = "В проекте не указаны роли",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        // Если ролей нет, можно пригласить с пустой ролью
                        if (selectedRole.isEmpty()) {
                            selectedRole = "Участник"
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedProjectId?.let { projectId ->
                        if (selectedRole.isNotBlank()) {
                            onInvite(projectId, selectedRole)
                        }
                    }
                },
                enabled = selectedProjectId != null && selectedRole.isNotBlank() && !isInviting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActionPrimary
                )
            ) {
                if (isInviting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(20.dp)
                            .padding(horizontal = 8.dp),
                        color = TextOnPrimary
                    )
                } else {
                    Text("Пригласить", color = TextOnPrimary)
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                enabled = !isInviting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = TextMuted
                )
            ) {
                Text("Отмена")
            }
        }
    )
}