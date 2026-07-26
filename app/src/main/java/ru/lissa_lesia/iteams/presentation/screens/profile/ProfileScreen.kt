package ru.lissa_lesia.iteams.presentation.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Edit, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (!uiState.isEditing && uiState.user != null) {
                        IconButton(onClick = { viewModel.toggleEditing() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                    }
                    if (uiState.isEditing) {
                        IconButton(onClick = {
                            // сохранить изменения
                            val currentUser = uiState.user
                            if (currentUser != null) {
                                viewModel.updateUser(currentUser)
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Сохранить")
                        }
                    }
                    IconButton(onClick = {
                        viewModel.logout {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Выйти")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = "Ошибка: ${uiState.errorMessage}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.toggleEditing() }) {
                        Text("Повторить")
                    }
                }
                uiState.user != null -> {
                    val user = uiState.user!!
                    if (uiState.isEditing) {
                        // Редактируемые поля
                        var name by remember { mutableStateOf(user.name) }
                        var bio by remember { mutableStateOf(user.bio) }
                        var skillsText by remember {
                            mutableStateOf(user.skills.joinToString(", "))
                        }

                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Имя") },
                            modifier = Modifier.fillMaxSize()
                        )
                        TextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("О себе") },
                            modifier = Modifier.fillMaxSize()
                        )
                        TextField(
                            value = skillsText,
                            onValueChange = { skillsText = it },
                            label = { Text("Навыки (через запятую)") },
                            modifier = Modifier.fillMaxSize()
                        )
                        Button(
                            onClick = {
                                val updatedUser = user.copy(
                                    name = name,
                                    bio = bio,
                                    skills = skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                )
                                viewModel.updateUser(updatedUser)
                            }
                        ) {
                            Text("Сохранить изменения")
                        }
                    } else {
                        // Отображение профиля
                        Text(text = "Имя: ${user.name}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "О себе: ${user.bio}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Навыки: ${user.skills.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.toggleEditing() }) {
                            Text("Редактировать профиль")
                        }
                    }
                }
                else -> {
                    Text("Пользователь не найден")
                }
            }
        }
    }
}