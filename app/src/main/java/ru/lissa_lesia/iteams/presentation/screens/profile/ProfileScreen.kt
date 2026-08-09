package ru.lissa_lesia.iteams.presentation.screens.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Logout
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import ru.lissa_lesia.iteams.domain.models.User
import ru.lissa_lesia.iteams.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val topBarGradient = Brush.horizontalGradient(
        colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
    )

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.cancelEditing()
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Редактирование профиля" else "Профиль",
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
                actions = {
                    IconButton(
                        onClick = onLogout,
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Выйти",
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
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ActionPrimary
                    )
                }
                uiState.errorMessage != null && uiState.user == null -> {
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
                            onClick = { viewModel.loadUser() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActionPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Повторить", color = TextOnPrimary)
                        }
                    }
                }
                uiState.user != null -> {
                    val user = uiState.user!!
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
                                        .size(80.dp)
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
                                        modifier = Modifier.size(40.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.isEditing) {
                                    EditProfileContent(
                                        user = user,
                                        onUserUpdate = { updatedUser -> viewModel.updateUser(updatedUser) }
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = uiState.resumeLinkInput,
                                        onValueChange = viewModel::updateResumeLinkInput,
                                        label = { Text("Ссылка на резюме (Google Drive, Dropbox и т.д.)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Вставьте ссылку") },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedBorderColor = InputBorderFocused,
                                            unfocusedBorderColor = InputBorderUnfocused,
                                            focusedLabelColor = InputLabelFocused
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.saveResumeLink { }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ActionPrimary
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f),
                                            enabled = !uiState.isSavingResume
                                        ) {
                                            if (uiState.isSavingResume) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = TextOnPrimary
                                                )
                                            } else {
                                                Text("Сохранить ссылку", color = TextOnPrimary)
                                            }
                                        }
                                        if (user.resumeUrl != null) {
                                            Button(
                                                onClick = {
                                                    viewModel.updateResumeLinkInput("")
                                                    viewModel.saveResumeLink { }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ActionDanger
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f),
                                                enabled = !uiState.isSavingResume
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Удалить",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Удалить", color = TextOnPrimary)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.saveProfile { } },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ActionSuccess
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Сохранить профиль", color = TextOnPrimary)
                                        }
                                        Button(
                                            onClick = { viewModel.cancelEditing() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ActionDanger
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Отмена", color = TextOnPrimary)
                                        }
                                    }
                                } else {
                                    ViewProfileContent(user = user)

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { viewModel.enableEditing() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ActionPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Редактировать профиль", color = TextOnPrimary)
                                    }
                                }
                            }
                        }

                        if (uiState.errorMessage != null && uiState.isEditing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = ErrorText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViewProfileContent(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = TextLight
        )

        if (user.bio.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        if (user.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Навыки: ${user.skills.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        if (user.resumeUrl != null && user.resumeUrl.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Резюме:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = "Резюме",
                    tint = ActionSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ссылка на резюме доступна",
                    color = ActionSuccess,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun EditProfileContent(
    user: User,
    onUserUpdate: (User) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var bio by remember { mutableStateOf(user.bio) }
    var skillsInput by remember { mutableStateOf(user.skills.joinToString(", ")) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Редактирование профиля",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = ActionPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = name,
            onValueChange = {
                name = it
                onUserUpdate(user.copy(name = it))
            },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = InputBorderFocused,
                unfocusedIndicatorColor = InputBorderUnfocused,
                focusedLabelColor = InputLabelFocused
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = bio,
            onValueChange = {
                bio = it
                onUserUpdate(user.copy(bio = it))
            },
            label = { Text("О себе") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = InputBorderFocused,
                unfocusedIndicatorColor = InputBorderUnfocused,
                focusedLabelColor = InputLabelFocused
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = skillsInput,
            onValueChange = {
                skillsInput = it
                val skills = it.split(",")
                    .map { skill -> skill.trim() }
                    .filter { it.isNotEmpty() }
                onUserUpdate(user.copy(skills = skills))
            },
            label = { Text("Навыки (через запятую)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = InputBorderFocused,
                unfocusedIndicatorColor = InputBorderUnfocused,
                focusedLabelColor = InputLabelFocused
            )
        )
    }
}